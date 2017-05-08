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
        mSlideMenuLayout.setBackgroundColor(Color.parseColor("#4876FF"));
        mSlideMenuLayout.setMenuMode(SlidingMenuLayout.MenuMode.LEFT_RIGHT);
        mSlideMenuLayout.setSlidingMode(SlidingMenuLayout.SlidingMode.ALL);
        mSlideMenuLayout.setSlideEnable(true);
        mSlideMenuLayout.setMenuContentWidthRation(0.75f);
  }
```
* 把SlidingMenuLayout作为根布局
* 左侧，中间，以及右侧菜单布局都会有一个tag，通过给每一个布局设置一个fragment即可！
* 具体参考代码中的：MainActivity.java

# 关于滑动动画
滑动动画主要是给SlideMenuLayout设置IOnMenuOpenListener接口，在接口里面对菜单以及中间视图进行一些缩放、透明度以及平移操作，从而达到动画效果，如下所示：
```java
  mSlideMenuLayout.setOnMenuOpenListener(new SlidingMenuLayout.IOnMenuOpenListener() {
            @Override
            public void menuOpen(View menuView, View middleView, float openPercent, boolean isLeftMenu) {
                float menuScale = (float) (0.8 + 0.2 * openPercent);//0.8到1
                float contentScale = (float) (1 - 0.2 * openPercent);//1到0.8
                float translationXScale = 0;
                if (isLeftMenu) {
                    translationXScale = (1 - openPercent) * 0.6f;//范围是0.6到0
                } else {
                    translationXScale = -(1 - openPercent) * 0.6f;//范围是-0.6到0
                }
                menuView.setScaleX(menuScale);
                menuView.setScaleY(menuScale);
                menuView.setAlpha(openPercent);
                menuView.setTranslationX(menuView.getWidth() * translationXScale);
                middleView.setScaleX(contentScale);
                middleView.setScaleY(contentScale);
            }
        });
```


