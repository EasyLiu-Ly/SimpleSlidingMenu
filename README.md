# SimpleSlidingMenu
一个简单的Android侧滑菜单，支持left, right,left_right三种菜单模式，支持edge,all两种滑动模式，支持设置菜单的宽度，支持滑动动画
# 效果如下

每个Fragment里面是一个RecyclerView，解决了滑动冲突问题，包含滑动动画

![演示效果图](https://github.com/EasyLiu-Ly/SimpleSlidingMenu/blob/master/simpleSlidingMenu.gif)

# 使用方式如下所示：


```java
  private SlidingMenuLayout mSlideMenuLayout;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSlideMenuLayout = new SlidingMenuLayout(this);
    setContentView(mSlideMenuLayout);
    initSlideMenuLayout();
  }

  private void initSlideMenuLayout() {
        getSupportFragmentManager().beginTransaction()
                .replace(SlidingMenuLayout.LEFT_TAG, ItemFragment.newInstance(1))
                .commit();
        getSupportFragmentManager().beginTransaction()
                .replace(SlidingMenuLayout.MIDDLE_TAG, ItemFragment.newInstance(1))
                .commit();
        getSupportFragmentManager().beginTransaction()
                .replace(SlidingMenuLayout.RIGHT_TAG, ItemFragment.newInstance(1))
                .commit();
        mSlideMenuLayout.setMenuMode(SlidingMenuLayout.MenuMode.LEFT_RIGHT);
        mSlideMenuLayout.setSlidingMode(SlidingMenuLayout.SlidingMode.ALL);
        mSlideMenuLayout.setSlideEnable(true);
        mSlideMenuLayout.setMenuContentWidthRation(0.75f);
        mSlideMenuLayout.setSlideAnimationEnable(true);
  }
```
* 把SlidingMenuLayout作为根布局
* 左侧，中间，以及右侧菜单布局都会有一个tag，通过给每一个布局设置一个fragment即可！
* 具体参考代码中的：MainActivity.java

# 关于滑动动画
关于滑动动画主要看SlidingMenuLayout类当中的onScrollChanged方法，如下所示，可以对这个方法进行修改,来实现各种类型的滑动效果

```java
 @Override protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    if (mSlideAnimationEnable) {
      float scale = 0;
      float translationXScale = 0;
      View currentAnimateMenuView = mLeftLayout;
      if (l < 0) {
        scale = -l * 1.0f / mLeftMenuWidth;//范围是0到1
        translationXScale = (1 - scale) * 0.6f;//范围是0.6到0
        currentAnimateMenuView = mLeftLayout;
      } else if (l > 0) {
        scale = l * 1.0f / mRightMenuWidth;//范围是0到1
        translationXScale = -(1 - scale) * 0.6f;//范围是-0.6到0
        currentAnimateMenuView = mRightLayout;
      }
      Log.d(TAG, scale + "");
      float menuScale = (float) (0.8 + 0.2 * scale);//0.8到1
      float contentScale = (float) (1 - 0.2 * scale);//1到0.8
      currentAnimateMenuView.setScaleX(menuScale);
      currentAnimateMenuView.setScaleY(menuScale);
      currentAnimateMenuView.setAlpha(scale);
      currentAnimateMenuView.setTranslationX(currentAnimateMenuView.getWidth() * translationXScale);
      mMiddleLayout.setScaleX(contentScale);
      mMiddleLayout.setScaleY(contentScale);
    }
  }
```


