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

package com.drakeet.drawer.sample

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.RecyclerView.LayoutParams.MATCH_PARENT
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.drakeet.multitype.MultiTypeAdapter
import com.drakeet.multitype.ViewDelegate

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // Make the status and navigation bars transparent
    window.setFlags(FLAG_LAYOUT_NO_LIMITS, FLAG_LAYOUT_NO_LIMITS)

    val viewPager = findViewById<ViewPager>(R.id.viewPager)
    viewPager.adapter = PregnantPagerAdapter(viewPager)

    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
    val items = ArrayList<String>()
    val adapter = MultiTypeAdapter(items)
    adapter.register(String::class, StringViewDelegate())
    recyclerView.adapter = adapter

    val drawer = findViewById<DrawerLayout>(R.id.drawer)
    drawer.addDrawerListener(object : DrawerLayout.DrawerListener {
      override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        // items.add("onDrawerSlide($slideOffset)")
        // adapter.notifyDataSetChanged()
      }

      override fun onDrawerOpened(drawerView: View) {
        items.add("onDrawerOpened()")
        adapter.notifyDataSetChanged()
      }

      override fun onDrawerClosed(drawerView: View) {
        items.add("onDrawerClosed()")
        adapter.notifyDataSetChanged()
      }

      override fun onDrawerStateChanged(newState: Int) {
        items.add("onDrawerStateChanged($newState)")
        adapter.notifyDataSetChanged()
      }
    })
  }

  class StringViewDelegate : ViewDelegate<String, TextView>() {

    override fun onCreateView(context: Context): TextView {
      return TextView(context).apply {
        setTextColor(Color.BLACK)
        gravity = Gravity.CENTER
        updatePadding(left = 24.dp, right = 24.dp)
        layoutParams = RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
      }
    }

    override fun onBindView(view: TextView, item: String) {
      view.text = item
    }
  }

  class PregnantPagerAdapter(private val viewPager: ViewPager) : PagerAdapter() {

    init {
      viewPager.offscreenPageLimit = count
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any = container.getChildAt(position)

    override fun getCount(): Int = viewPager.childCount

    override fun isViewFromObject(view: View, any: Any): Boolean = (view === any)

    override fun destroyItem(parent: ViewGroup, position: Int, any: Any) = parent.removeView(any as View)

    override fun getItemPosition(any: Any): Int {
      val index = viewPager.indexOfChild(any as View)
      return if (index == -1) POSITION_NONE else index
    }
  }
}

internal val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
