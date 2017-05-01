package com.easyliu.simpleslidingmenu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.easyliu.simpleslidingmenu.dummy.DummyContent;

public class MainActivity extends AppCompatActivity
    implements ItemFragment.OnListFragmentInteractionListener {
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
    mSlideMenuLayout.setSlidingMode(SlidingMenuLayout.SlidingMode.EDGE);
    mSlideMenuLayout.setSlideEnable(true);
  }

  @Override public void onBackPressed() {
    if (mSlideMenuLayout.isMenuShowing()) {
      mSlideMenuLayout.closeMenu();
    } else {
      super.onBackPressed();
    }
  }

  @Override public void onListFragmentInteraction(DummyContent.DummyItem item) {

  }
}
