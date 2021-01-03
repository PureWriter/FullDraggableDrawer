/*
 * Copyright (c) 2021. Drakeet Xu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drakeet.drawer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static java.lang.Math.abs;
import static java.util.Objects.requireNonNull;

/**
 * TODO: Add support for the right drawer
 * TODO: Add support for other kinds of drawer
 *
 * @author Drakeet Xu
 */
public class FullDraggableContainer extends FrameLayout {

  private float initialMotionX;
  private float initialMotionY;
  private float lastMotionX;
  private float lastMotionY;
  private final int touchSlop;
  private final int swipeSlop;
  private final int distanceThreshold;
  private final int xVelocityThreshold;

  private boolean isDraggingDrawer = false;
  private boolean shouldOpenDrawer = false;

  @Nullable
  private VelocityTracker velocityTracker = null;
  private DrawerLayout drawerLayout;
  private int gravity = Gravity.NO_GRAVITY;

  public FullDraggableContainer(@NonNull Context context) {
    this(context, null);
  }

  public FullDraggableContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FullDraggableContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    swipeSlop = dipsToPixels(8);
    distanceThreshold = dipsToPixels(80);
    xVelocityThreshold = dipsToPixels(150);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    ensureDrawerLayout();
  }

  private void ensureDrawerLayout() {
    ViewParent parent = getParent();
    if (!(parent instanceof DrawerLayout)) {
      throw new IllegalStateException("This " + this + " must be added to a DrawerLayout");
    }
    drawerLayout = (DrawerLayout) parent;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    boolean intercepted = false;
    int action = event.getActionMasked();
    float x = event.getX();
    float y = event.getY();
    if (action == MotionEvent.ACTION_DOWN) {
      lastMotionX = initialMotionX = x;
      lastMotionY = initialMotionY = y;
      return false;
    } else if (action == MotionEvent.ACTION_MOVE) {
      if (canNestedViewScroll(this, false, (int) (x - lastMotionX), (int) x, (int) y)) {
        return false;
      }
      lastMotionX = x;
      float diffX = x - initialMotionX;
      intercepted = abs(diffX) > touchSlop
        && abs(diffX) > abs(y - initialMotionY)
        && isDrawerEnabled(diffX);
    }
    return intercepted;
  }

  private boolean canNestedViewScroll(View view, boolean checkSelf, int dx, int x, int y) {
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      int scrollX = view.getScrollX();
      int scrollY = view.getScrollY();
      int count = group.getChildCount();
      for (int i = count - 1; i >= 0; i--) {
        View child = group.getChildAt(i);
        if (child.getVisibility() != View.VISIBLE) continue;
        if (x + scrollX >= child.getLeft()
          && x + scrollX < child.getRight()
          && y + scrollY >= child.getTop()
          && y + scrollY < child.getBottom()
          && canNestedViewScroll(child, true, dx, x + scrollX - child.getLeft(), y + scrollY - child.getTop())) {
          return true;
        }
      }
    }
    return checkSelf && view.canScrollHorizontally(-dx);
  }

  @Override
  @SuppressLint({ "RtlHardcoded", "ClickableViewAccessibility" })
  public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    int action = event.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_MOVE: {
        float diffX = x - initialMotionX;
        if (isDrawerOpen() || !isDrawerEnabled(diffX)) {
          return false;
        }
        float absDiffX = abs(diffX);
        if (absDiffX > swipeSlop || isDraggingDrawer) {
          if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
          }
          velocityTracker.addMovement(event);
          boolean lastDraggingDrawer = isDraggingDrawer;
          isDraggingDrawer = true;
          shouldOpenDrawer = absDiffX > distanceThreshold;

          gravity = diffX > 0 ? Gravity.LEFT : Gravity.RIGHT;
          offsetDrawer(gravity, absDiffX - swipeSlop);

          if (!lastDraggingDrawer) {
            notifyDrawerDragging();
          }
        }
        return isDraggingDrawer;
      }
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP: {
        if (isDraggingDrawer) {
          if (velocityTracker != null) {
            velocityTracker.computeCurrentVelocity(1000);
            float xVelocity = velocityTracker.getXVelocity();
            boolean fromLeft = (gravity == Gravity.LEFT);
            if (xVelocity > xVelocityThreshold) {
              shouldOpenDrawer = fromLeft;
            } else if (xVelocity < -xVelocityThreshold) {
              shouldOpenDrawer = !fromLeft;
            }
          }
          if (shouldOpenDrawer) {
            openDrawer(gravity);
          } else {
            dismissDrawer(gravity);
          }
        }
        shouldOpenDrawer = false;
        isDraggingDrawer = false;
        gravity = Gravity.NO_GRAVITY;
        if (velocityTracker != null) {
          velocityTracker.recycle();
          velocityTracker = null;
        }
      }
    }
    return true;
  }

  @SuppressLint("RtlHardcoded")
  private boolean isDrawerOpen() {
    return drawerLayout.isDrawerOpen(Gravity.LEFT) || drawerLayout.isDrawerOpen(Gravity.RIGHT);
  }

  private void openDrawer(int gravity) {
    drawerLayout.openDrawer(gravity, true);
  }

  private void dismissDrawer(int gravity) {
    drawerLayout.closeDrawer(gravity, true);
  }

  private void offsetDrawer(int gravity, float dx) {
    setDrawerToOffset(gravity, dx);
    drawerLayout.invalidate();
  }

  private void notifyDrawerDragging() {
    List<DrawerLayout.DrawerListener> drawerListeners = getDrawerListeners();
    if (drawerListeners != null) {
      int listenerCount = drawerListeners.size();
      for (int i = listenerCount - 1; i >= 0; --i) {
        ((DrawerLayout.DrawerListener) drawerListeners.get(i)).onDrawerStateChanged(DrawerLayout.STATE_DRAGGING);
      }
    }
  }

  @Nullable
  protected List<DrawerLayout.DrawerListener> getDrawerListeners() {
    try {
      Field field = DrawerLayout.class.getDeclaredField("mListeners");
      field.setAccessible(true);
      // noinspection unchecked
      return (List<DrawerLayout.DrawerListener>) field.get(drawerLayout);
    } catch (Exception e) {
      // throw to let developer know the api is changed
      throw new RuntimeException(e);
    }
  }

  protected void setDrawerToOffset(int gravity, float dx) {
    View drawerView = findDrawerWithGravity(gravity);
    float slideOffset = dx / requireNonNull(drawerView).getWidth();
    try {
      Method method = DrawerLayout.class.getDeclaredMethod("moveDrawerToOffset", View.class, float.class);
      method.setAccessible(true);
      method.invoke(drawerLayout, drawerView, slideOffset);
      drawerView.setVisibility(VISIBLE);
    } catch (Exception e) {
      // throw to let developer know the api is changed
      throw new RuntimeException(e);
    }
  }

  // Copied from DrawerLayout
  @Nullable
  private View findDrawerWithGravity(int gravity) {
    final int absHorizontalGravity = GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(drawerLayout)) & Gravity.HORIZONTAL_GRAVITY_MASK;
    final int childCount = drawerLayout.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = drawerLayout.getChildAt(i);
      final int childAbsGravity = getDrawerViewAbsoluteGravity(child);
      if ((childAbsGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == absHorizontalGravity) {
        return child;
      }
    }
    return null;
  }

  // Copied from DrawerLayout
  private int getDrawerViewAbsoluteGravity(View drawerView) {
    final int gravity = ((DrawerLayout.LayoutParams) drawerView.getLayoutParams()).gravity;
    return GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(drawerLayout));
  }

  @SuppressLint("RtlHardcoded")
  private boolean isDrawerEnabled(float diffX) {
    return diffX > 0 && hasUnlockedDrawer(Gravity.LEFT)
      || diffX < 0 && hasUnlockedDrawer(Gravity.RIGHT);
  }

  private boolean hasUnlockedDrawer(int gravity) {
    return drawerLayout.getDrawerLockMode(gravity) == DrawerLayout.LOCK_MODE_UNLOCKED
      && findDrawerWithGravity(gravity) != null;
  }

  private int dipsToPixels(int dips) {
    float scale = getContext().getResources().getDisplayMetrics().density;
    return (int) (dips * scale + 0.5f);
  }
}
