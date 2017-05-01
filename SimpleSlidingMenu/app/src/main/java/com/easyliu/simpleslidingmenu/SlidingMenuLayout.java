package com.easyliu.simpleslidingmenu;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by EasyLiu on 30/04/2017.
 */

public class SlidingMenuLayout extends RelativeLayout {
  private static final String TAG = SlidingMenuLayout.class.getSimpleName();
  private static final boolean DEBUG = true;
  private FrameLayout mLeftLayout;// 作菜单的布局
  private FrameLayout mMiddleLayout;// 中间布局
  private FrameLayout mRightLayout;// 右菜单的布局
  private View mMask;//蒙版
  private Scroller mScroller;
  public static final int DEFAULT_TAG = 0xff;
  public static final int LEFT_TAG = DEFAULT_TAG;
  public static final int MIDDLE_TAG = LEFT_TAG + 1;
  public static final int RIGHT_TAG = MIDDLE_TAG + 1;
  private static final int DEFAULT_MASK_COLOR = 0x88000000;
  private int mMaskColor;// 蒙版的颜色
  private SlidingMode mSlidingMode;//滑动模式
  private MenuMode mMenuMode;//菜单模式
  private int mLastX;
  private int mLastY;
  private int mDownX;
  private Context mContext;
  private static final float MENU_CONTENT_WIDTH_RATION = (float) 0.8;
  private int mSlideEdgeLeftXMax; //边界滑动的时候，LEFT模式时，落点X轴坐标的最大值
  private int mSlideEdgeRightXMin; //边界滑动的时候，RIGHT模式时，落点X轴坐标的最小值
  private boolean mIsBeingDraging;//是否正在拖动，用于事件拦截
  private boolean mIsSlideEnable; //是否使能滑动
  private int mActivePointerId = INVALID_POINTER;
  private static final int INVALID_POINTER = -1;
  private VelocityTracker mVelocityTracker;// 速度追踪器
  private int mSlideVelocity;//
  private int mMaximumVelocity;//最大速度
  private int mTouchSlop;//最小滑动距离
  private static final float EDGE_DOWN_X_MAX_PARTITION = (float) 1 / 10;
  private static final int DEFAULT_MINIMUM_SLIDE_FINISH_VELOCITY = 5000;//最小的滑动finish速度,单位为pix/s
  private boolean mIsMenuShowing;//菜单是否显示
  private boolean mIsFastSlideEnable;  //快速滑动标志是否使能

  //滑动模式
  public enum SlidingMode {
    EDGE, ALL
  }

  //菜单模式
  public enum MenuMode {
    LEFT, RIGHT, LEFT_RIGHT
  }

  public SlidingMenuLayout(Context context) {
    this(context, null);
  }

  public SlidingMenuLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    init();
  }

  private void init() {
    mLeftLayout = new FrameLayout(mContext);
    mMiddleLayout = new FrameLayout(mContext);
    mRightLayout = new FrameLayout(mContext);
    mLeftLayout.setId(LEFT_TAG);
    mMiddleLayout.setId(MIDDLE_TAG);
    mRightLayout.setId(RIGHT_TAG);
    mMask = new View(mContext);
    mMaskColor = DEFAULT_MASK_COLOR;
    mMask.setBackgroundColor(mMaskColor);
    mMask.setAlpha((float) 0.0);
    mScroller = new Scroller(mContext, new DecelerateInterpolator());
    final ViewConfiguration configuration = ViewConfiguration.get(mContext);
    mTouchSlop = configuration.getScaledTouchSlop();
    mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    mSlideVelocity = DEFAULT_MINIMUM_SLIDE_FINISH_VELOCITY;
    mSlideEdgeLeftXMax =
        (int) (mContext.getResources().getDisplayMetrics().widthPixels * EDGE_DOWN_X_MAX_PARTITION);
    mSlideEdgeRightXMin = (int) (mContext.getResources().getDisplayMetrics().widthPixels * (1
        - EDGE_DOWN_X_MAX_PARTITION));
    mMenuMode = MenuMode.LEFT;
    mSlidingMode = SlidingMode.EDGE;
    mIsMenuShowing = false;
    mIsBeingDraging = false;
    mIsSlideEnable = true;
    mIsFastSlideEnable = false;
    addView(mLeftLayout);
    addView(mMiddleLayout);
    addView(mMask);
    addView(mRightLayout);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    mMiddleLayout.measure(widthMeasureSpec, heightMeasureSpec);
    mMask.measure(widthMeasureSpec, heightMeasureSpec);
    int widthSize = mMiddleLayout.getMeasuredWidth();
    int tempWidthMeasureSpec =
        MeasureSpec.makeMeasureSpec((int) (widthSize * MENU_CONTENT_WIDTH_RATION),
            MeasureSpec.EXACTLY);
    mLeftLayout.measure(tempWidthMeasureSpec, heightMeasureSpec);
    mRightLayout.measure(tempWidthMeasureSpec, heightMeasureSpec);
  }

  /**
   * 这段代码来自于ViewPager，用于判断视图V的子视图是否可以滑动，从而进行事件拦截
   * Tests scrollability within child views of v given a delta of dx.
   */
  protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
    if (v instanceof ViewGroup) {
      final ViewGroup group = (ViewGroup) v;
      final int scrollX = v.getScrollX();
      final int scrollY = v.getScrollY();
      final int count = group.getChildCount();
      // Count backwards - let topmost views consume scroll distance first.
      for (int i = count - 1; i >= 0; i--) {
        // This will not work for transformed views in Honeycomb+
        final View child = group.getChildAt(i);
        if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child
            .getTop() && y + scrollY < child.getBottom() && canScroll(child, true, dx,
            x + scrollX - child.getLeft(), y + scrollY - child.getTop())) {
          return true;
        }
      }
    }

    return checkV && ViewCompat.canScrollHorizontally(v, -dx);
  }

  private void resetTouchState() {
    releaseVelocityTracker();
    mIsBeingDraging = false;
  }

  /**
   * 释放速度追踪器
   */
  private void releaseVelocityTracker() {
    if (null != mVelocityTracker) {
      mVelocityTracker.clear();
      mVelocityTracker.recycle();
      mVelocityTracker = null;
    }
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    mMiddleLayout.layout(l, t, r, b);
    mMask.layout(l, t, r, b);
    mLeftLayout.layout(l - mLeftLayout.getMeasuredWidth(), t, l, b);
    mRightLayout.layout(l + mMiddleLayout.getMeasuredWidth(), t,
        l + mMiddleLayout.getMeasuredWidth() + mRightLayout.getMeasuredWidth(), b);
  }

  /*
   菜单是否显示
   */
  public boolean isMenuShowing() {
    return mIsMenuShowing;
  }

  /**
   * 关闭菜单
   */
  public void closeMenu() {
    if (mIsMenuShowing) {
      mIsMenuShowing = false;
      if (getScrollX() != 0) {
        scrollToOriginal();
      }
    }
  }

  /**
   * 设置蒙版透明度
   */
  private void setMaskAlpha(int curScrollX) {
    float alpha = (float) (Math.abs(curScrollX) * 1.0 / mLeftLayout.getWidth());
    mMask.setAlpha(alpha);
  }

  /**
   * 开始平滑滚动
   */
  private void startSmoothScroll(int startX, int startY, int dx, int dy) {
    mScroller.startScroll(startX, startY, dx, dy);
    invalidate();
  }

  /**
   * 滚动到初始位置
   */
  private void scrollToOriginal() {
    startSmoothScroll(getScrollX(), 0, -getScrollX(), 0);
  }

  /**
   * 滚出右边菜单
   */
  private void scrollToRightMenu() {
    int curScrollX = getScrollX();
    startSmoothScroll(curScrollX, 0, mRightLayout.getWidth() - curScrollX, 0);
  }

  /**
   * 滚出左边菜单
   */
  private void scrollToLeftMenu() {
    int curScrollX = getScrollX();
    startSmoothScroll(curScrollX, 0, -mLeftLayout.getWidth() - curScrollX, 0);
  }

  /**
   * 设置滑动模式
   */
  public void setSlidingMode(SlidingMode slidingMode) {
    mSlidingMode = slidingMode;
  }

  /**
   * 设置菜单模式
   */
  public void setMenuMode(MenuMode menuMode) {
    mMenuMode = menuMode;
  }

  /**
   * 设置滑动是否使能
   */
  public void setSlideEnable(boolean slideEnable) {
    mIsSlideEnable = slideEnable;
  }

  /**
   * 判断是否正在进行有效拖动，用于事件拦截以及判断拖动是否合法
   */
  private boolean getIsBeingDraging(int deltaX, int moveX, int moveY) {
    boolean isBeingDraging = true;
    if (getScrollX() == 0) {//初始位置
      if (deltaX > 0) {
        if (mMenuMode == MenuMode.RIGHT) {//初始位置，只能往左滑
          isBeingDraging = false;
        }
      } else {
        if (mMenuMode == MenuMode.LEFT) {// 初始位置，只能往右滑
          isBeingDraging = false;
        }
      }
    }
    if (isBeingDraging) {
      if (mSlidingMode == SlidingMode.EDGE && getScrollX() == 0) {//边界模式且为初始位置，需要判断开始触摸点是否合法
        if (mMenuMode == MenuMode.LEFT) {
          if (mDownX > mSlideEdgeLeftXMax) {
            isBeingDraging = false;
          }
        } else if (mMenuMode == MenuMode.RIGHT) {
          if (mDownX < mSlideEdgeRightXMin) {
            isBeingDraging = false;
          }
        } else {
          if (mDownX > mSlideEdgeLeftXMax && mDownX < mSlideEdgeRightXMin) {
            isBeingDraging = false;
          }
        }
      } else {//不是边界滑动的时候，需要考虑到滑动冲突
        if (deltaX != 0 && canScroll(this, false, deltaX, moveX, moveY)) {
          if (DEBUG) Log.d(TAG, "child view can scroll,not intercept event");
          isBeingDraging = false;
        }
      }
    }
    return isBeingDraging;
  }

  @Override public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      setMaskAlpha(mScroller.getCurrX());
      scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
      invalidate();
    }
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
    //如果不使能就不拦截
    if (!mIsSlideEnable) {
      return false;
    }
    if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
      // 如果这个触摸被取消了，或者手指抬起来了就不拦截
      resetTouchState();
      return false;
    }
    //正在拖动且不是ACTION_DOWN事件，就拦截
    if (action != MotionEvent.ACTION_DOWN && mIsBeingDraging) {
      return true;
    }
    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mDownX = (int) ev.getRawX();
        mLastX = mDownX;
        mLastY = (int) ev.getRawY();
        mIsBeingDraging = false;//在Action_down的时候不拦截，如果拦截的话，接下来的up和cancel事件都是交给自身处理，
        // 子View就收不到事件了,且接下来onIntercept方法都不会再调用了
        mIsFastSlideEnable = false;
        mActivePointerId = ev.getPointerId(0);
        if (DEBUG) Log.d(TAG, "Intercept Action_Down mLastX=:" + mLastX + " mLastY=:" + mLastY);
        //开始事件分发的时候，判断动画是否结束
        if ((mScroller != null) && !mScroller.isFinished()) {
          mScroller.abortAnimation();
          mIsBeingDraging = true;
        }
        break;
      case MotionEvent.ACTION_MOVE:
        int moveX = (int) ev.getRawX();
        int moveY = (int) ev.getRawY();
        if (DEBUG) Log.d(TAG, "Intercept Action_MOVE moveX=:" + moveX + " moveY=:" + moveY);
        int deltaX = moveX - mLastX;
        int deltaY = moveY - mLastY;
        if (Math.abs(deltaX) > mTouchSlop && Math.abs(deltaY) < Math.abs(deltaX)) {
          mIsBeingDraging = getIsBeingDraging(deltaX, moveX, moveY);//初始化为拦截
        }
        break;
    }
    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(ev);
    return mIsBeingDraging;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    //如果不使能就不响应
    if (!mIsSlideEnable) {
      return false;
    }
    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(event);
    switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN: {
        mLastX = mDownX = (int) event.getRawX();
        mLastY = (int) event.getRawY();
        mActivePointerId = event.getPointerId(0);
        if (DEBUG) Log.d(TAG, "Action_Down mLastX=:" + mLastX + " mLastY=:" + mLastY);
        break;
      }
      case MotionEvent.ACTION_MOVE:
        if (!mIsFastSlideEnable) {
          int moveX = (int) event.getRawX();
          int moveY = (int) event.getRawY();
          if (DEBUG) Log.d(TAG, "Action_MOVE moveX=:" + moveX + " moveY=:" + moveY);
          int deltaX = moveX - mLastX;
          int deltaY = moveY - mLastY;
          mLastX = moveX;
          mLastY = moveY;
          //特殊情况：当没有子类或者有子类但是没有响应事件的时候，
          //onIntercept只会在Action_Down的时候才会调用，此时mIsBeingDraging就为false
          if (!mIsBeingDraging) {
            if (Math.abs(deltaX) > mTouchSlop && Math.abs(deltaY) < Math.abs(deltaX)) {
              mIsBeingDraging = getIsBeingDraging(deltaX, moveX, moveY);
            }
          }
          if (mIsBeingDraging) {
            int expectX = getScrollX() - deltaX;
            int finalX = 0;
            final VelocityTracker velocityTracker = mVelocityTracker;
            velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
            float xVelocity =
                VelocityTrackerCompat.getXVelocity(velocityTracker, mActivePointerId);//得到x方向的速度
            if (deltaX > 0) {//往右
              if (xVelocity >= mSlideVelocity) {
                mIsBeingDraging = false;
                mIsFastSlideEnable = true;//快速滑动使能
                if (mMenuMode == MenuMode.RIGHT || getScrollX() > 0) {
                  scrollToOriginal();
                  mIsMenuShowing = false;
                } else {
                  scrollToLeftMenu();
                  mIsMenuShowing = true;
                }
                return true;
              } else {
                finalX = Math.max(expectX, -mLeftLayout.getWidth());//最多把作菜单全部滑出
              }
            } else {//往左
              if (xVelocity <= -mSlideVelocity) {
                mIsBeingDraging = false;
                mIsFastSlideEnable = true;//快速滑动使能
                if (mMenuMode == MenuMode.LEFT || getScrollX() < 0) {
                  scrollToOriginal();
                  mIsMenuShowing = false;
                } else {
                  scrollToRightMenu();
                  mIsMenuShowing = true;
                }
                return true;
              } else {
                finalX = Math.min(expectX, mRightLayout.getWidth());//最多把右菜单全部滑出
              }
            }
            setMaskAlpha(finalX);
            scrollTo(finalX, 0);
          }
        }
        break;
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        if (mIsBeingDraging) {
          mIsBeingDraging = false;
          if (DEBUG) Log.d(TAG, "Action_UP upX=:" + event.getRawX() + " upY=:" + event.getRawY());
          int curScrollX = getScrollX();
          if (Math.abs(curScrollX) > mLeftLayout.getWidth() >> 1) {
            mIsMenuShowing = true;
            if (curScrollX > 0) {//往左边滚动
              scrollToRightMenu();
            } else {             //往右边滚动
              scrollToLeftMenu();
            }
          } else {//回到起点
            scrollToOriginal();
            mIsMenuShowing = false;
          }
        }
        break;
    }
    return true;
  }

  @Override protected void onDetachedFromWindow() {
    if ((mScroller != null) && !mScroller.isFinished()) {
      mScroller.abortAnimation();
    }
    super.onDetachedFromWindow();
  }
}
