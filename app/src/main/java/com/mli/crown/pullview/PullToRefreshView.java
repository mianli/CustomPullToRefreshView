package com.mli.crown.pullview;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by crown on 2016/11/9.
 */
public class PullToRefreshView extends RelativeLayout {

	private static final String TAG = "PullView";


	public static final int REVERT_STATE = 0;
	public static final int REFRESHING_STATE = 1;
	public static final int LOADING_STATE = 2;

	private static final int REVERT_DURATION = 1000;

	boolean isLayout;
	private View mHeaderView;
	private View mFooterView;
	private View mContainer;

	private float mStartY;
	private float mMovingY;

	private float mOffsetY;

	private int mPullHeight;

	private int mState;

	private int mRefreshHeight = 200;
	private int mLoadingHeight = 200;

	private boolean mCanRefresh;
	private boolean mCanLoad;

	private pullToRefreshListener mListener;

	public PullToRefreshView(Context context) {
		this(context, null);
	}

	public PullToRefreshView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PullToRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setPullToRefreshListener(pullToRefreshListener listener) {
		this.mListener = listener;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				mStartY = ev.getY();
				if(mTimerTask != null) {
					mTimerTask.cancel();
					mTimerTask = null;
					if(mPullHeight != 0) {
						mOffsetY = mPullHeight;
					}else {
						mOffsetY = 0;
					}
					requestLayout();
				}
				break;
			case MotionEvent.ACTION_MOVE:
				mHeaderView.setVisibility(VISIBLE);
				mFooterView.setVisibility(VISIBLE);
				mMovingY = ev.getY();

				Log.i(TAG, "pullheight" + mPullHeight);
				mPullHeight = (int) ((mMovingY - mStartY) / 3 + mOffsetY);
				if(mState == REFRESHING_STATE && mPullHeight < 0) {
					mPullHeight = 0;
					Log.i(TAG, "cant toRefresh");
					mStartY = ev.getY();
					mOffsetY = 0;
					return super.dispatchTouchEvent(ev);
				}else if(mState == LOADING_STATE && mPullHeight > 0) {
					mPullHeight =0;
					Log.i(TAG, "cant load");
					mStartY = ev.getY();
					mOffsetY = 0;
					return super.dispatchTouchEvent(ev);
				}else {
					mPullHeight = (int) ((mMovingY - mStartY) / 3 + mOffsetY);
					requestLayout();
				}
				if(mPullHeight >= mRefreshHeight && mState != REFRESHING_STATE) {
					mState = REFRESHING_STATE;
					mCanRefresh = true;
					mCanLoad = false;
				}else if(-mPullHeight >= mLoadingHeight && mState != LOADING_STATE) {
					mState = LOADING_STATE;
					mCanRefresh = false;
					mCanLoad = true;
				}
				break;
			case MotionEvent.ACTION_UP:
				revertNormal();
				if(mListener != null) {
					if(mCanRefresh) {
						mCanRefresh = false;
						mListener.toRefresh();
					}else if(mCanLoad) {
						mCanLoad = false;
						mListener.toReLoad();
					}
				}
				break;
			default:
				break;
		}
		super.dispatchTouchEvent(ev);
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(!isLayout) {
			isLayout = true;
			super.onLayout(changed, l, t, r, b);
			mHeaderView = findViewById(R.id.header_view);
			mFooterView = findViewById(R.id.footer_view);
			mContainer = findViewById(R.id.container);
			mHeaderView.setVisibility(GONE);
			mFooterView.setVisibility(GONE);
		}
			//下拉
			mHeaderView.layout(0, mPullHeight - mHeaderView.getMeasuredHeight(),
				getWidth(), mPullHeight);
			mContainer.layout(0, mPullHeight,
				getWidth(), mPullHeight + mContainer.getHeight());
		mFooterView.layout(0, mPullHeight + mFooterView.getHeight(), getWidth(),
			2 * mFooterView.getHeight() + mPullHeight);
	}

	private void revertNormal() {
		handleView();
	}

	private Handler mHandler= new Handler();
	private Timer mTimer;
	private TimerTask mTimerTask;
	private void handleView() {
		mTimer = new Timer();
		if(mTimerTask != null) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						revertPullHeight();
					}
				});
			}
		};
		mTimer.schedule(mTimerTask, 0, 1);
	}

	private void revertPullHeight() {
		if(mPullHeight != 0) {
			if(mPullHeight < 0) {
				if(mState == REVERT_STATE
					|| (mState == LOADING_STATE && mPullHeight != -mLoadingHeight))
				mPullHeight += 1;
				if(mPullHeight >0) {
					mPullHeight = 0;
					mTimer.cancel();
					mTimer = null;
				}
			}else {
				if(mState == REVERT_STATE
					|| (mState == REFRESHING_STATE && mPullHeight != mRefreshHeight)) {
					mPullHeight -= 1;
				}
				if(mPullHeight < 0) {
					mPullHeight = 0;
					mTimer.cancel();
					mTimer = null;
				}
			}
			requestLayout();
		}
	}

	public void setState(int state) {
		mState = state;
		if(mState == REVERT_STATE) {
			mCanRefresh = true;
			mCanLoad = true;
		}
	}

	//恢复正常状态
	public void revertState() {
		mState = REVERT_STATE;
		mCanRefresh = false;
		mCanLoad = false;
	}

	private void changeState(int state) {

	}

}
