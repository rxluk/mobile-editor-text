package app.vercel.lucasgabrielcosta.mindra.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementação de um listener para detectar gestos de swipe em ListView
 * com suporte para diferentes ações baseadas na direção do swipe.
 */
public class SwipeActionsTouchListener implements View.OnTouchListener {
    private static final int DIRECTION_LEFT = -1;
    private static final int DIRECTION_RIGHT = 1;

    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    private ListView mListView;
    private SwipeActionsCallback mCallback;
    private int mViewWidth = 1;
    private boolean mPaused;

    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private View mDownView;
    private boolean mLongPressPerformed;
    private long mDownTime;
    private int mSwipeDirection;

    public interface SwipeActionsCallback {
        boolean canSwipe(int position);

        void onSwipeRight(int position);

        void onSwipeLeft(int position, Context context);
    }

    public SwipeActionsTouchListener(ListView listView, SwipeActionsCallback callback) {
        ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = listView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mListView = listView;
        mCallback = callback;
    }

    public void setPaused(boolean paused) {
        mPaused = paused;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mViewWidth = mListView.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    return false;
                }

                Rect rect = new Rect();
                int childCount = mListView.getChildCount();
                int[] listViewCoords = new int[2];
                mListView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                mLongPressPerformed = false;

                for (int i = 0; i < childCount; i++) {
                    child = mListView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        mDownView = child;
                        mDownTime = SystemClock.elapsedRealtime();
                        break;
                    }
                }

                if (mDownView != null) {
                    mDownX = motionEvent.getRawX();
                    mDownY = motionEvent.getRawY();
                    mDownPosition = mListView.getPositionForView(mDownView);
                    if (mCallback.canSwipe(mDownPosition)) {
                        mVelocityTracker = VelocityTracker.obtain();
                        mVelocityTracker.addMovement(motionEvent);
                    } else {
                        mDownView = null;
                    }
                }
                return false;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    return false;
                }

                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;

                if (deltaX > 0) {
                    mSwipeDirection = DIRECTION_RIGHT;
                } else {
                    mSwipeDirection = DIRECTION_LEFT;
                }

                if (!mSwiping && Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    mListView.requestDisallowInterceptTouchEvent(true);

                    // Cancela o evento de ListView para evitar clique
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mListView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if (mSwiping) {
                    mDownView.setTranslationX(deltaX - mSwipingSlop);
                    mDownView.setAlpha(Math.max(0.2f, Math.min(1f,
                            1f - Math.abs(deltaX) / (mViewWidth * 0.5f))));
                    return true;
                }

                if (!mLongPressPerformed && SystemClock.elapsedRealtime() - mDownTime > ViewConfiguration.getLongPressTimeout() &&
                        Math.abs(deltaX) < mSlop && Math.abs(deltaY) < mSlop) {
                    mLongPressPerformed = true;
                    return false;
                }

                return false;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    return false;
                }

                float deltaX = motionEvent.getRawX() - mDownX;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean triggerAction = false;

                if (Math.abs(deltaX) > mViewWidth / 3 && mSwiping) {
                    triggerAction = true;
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX && mSwiping) {
                    triggerAction = (velocityX < 0) == (deltaX < 0);
                }

                if (triggerAction && mDownPosition != ListView.INVALID_POSITION) {
                    if (mSwipeDirection == DIRECTION_RIGHT) {
                        mCallback.onSwipeRight(mDownPosition);
                    } else {
                        mCallback.onSwipeLeft(mDownPosition, mListView.getContext());
                    }

                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                } else {
                    if (mSwiping) {
                        mDownView.animate()
                                .translationX(0)
                                .alpha(1)
                                .setDuration(mAnimationTime)
                                .setListener(null);
                    }
                }

                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                return false;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    return false;
                }

                if (mDownView != null && mSwiping) {
                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }

                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                return false;
            }
        }
        return false;
    }
}