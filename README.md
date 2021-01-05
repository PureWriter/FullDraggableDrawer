# FullDraggableDrawer

Make the `DrawerLayout` can be dragged/pulled out in real-time within the range of fullscreen, like [Pure Writer](https://play.google.com/store/apps/details?id=com.drakeet.purewriter)：

<img src="snapshot.jpg" width=360></img>

<b>_* Full demo video: https://t.me/PureWriter/549_</b>

## Getting started

In your `build.gradle`:

```groovy
dependencies {
  implementation 'com.drakeet.drawer:drawer:1.0.0'
  // Optional: No need if you just use the FullDraggableHelper
  implementation 'androidx.drawerlayout:drawerlayout:1.1.1'
}
```

## Usage

Replace the main Layout of `DrawerLayout` with the `FullDraggableContainer` (or you can just add it as a new wrapper/layer):

```xml
<androidx.drawerlayout.widget.DrawerLayout
  xmlns:android="http://schemas.android.com/apk/res/android"
  android:id="@+id/drawer"
  android:layout_width="match_parent"
  android:layout_height="match_parent">

  <!-- here 👇 -->
  <com.drakeet.drawer.FullDraggableContainer
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- ... -->

  </com.drakeet.drawer.FullDraggableContainer>

  <FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_gravity="left">

    <!-- ... -->

  </FrameLayout>

</androidx.drawerlayout.widget.DrawerLayout>
```

**That's all, you're good to go!**

### Advanced usage

See `com.drakeet.drawer.FullDraggableHelper`

## TODO

- [x] Add support for the right drawer / RTL
- [x] Add support for other kinds of drawers

License
-------

    Copyright (c) 2021. Drakeet Xu

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
