# SimpleSlidingMenu
一个简单的Android侧滑菜单，支持left, right,left_right三种菜单模式，支持edge,all两种滑动模式，支持设置菜单的宽度，遮罩的颜色
# 效果如下

每个Fragment里面是一个RecyclerView，解决了滑动冲突问题

![演示效果图](https://github.com/EasyLiu-Ly/SimpleSlidingMenu/blob/master/simpleSlidingMenu.gif)

## 使用方式如下所示：


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
        mSlideMenuLayout.setMaskColor(Color.GRAY);
        mSlideMenuLayout.setMenuContentWidthRation(0.75f);
  }
```
* 把SlidingMenuLayout作为根布局
* 左侧，中间，以及右侧菜单布局都会有一个tag，通过给每一个布局设置一个fragment即可！
* 具体参考代码中的：MainActivity.java
