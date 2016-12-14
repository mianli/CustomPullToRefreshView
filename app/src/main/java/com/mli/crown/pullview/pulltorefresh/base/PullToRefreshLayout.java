package com.mli.crown.pullview.pulltorefresh.base;

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
public class PullToRefreshLayout extends RelativeLayout {

	private static final String TAG = "PullView";

	private enum ePullState {
		eNormal,
		eWillRefresh,
		eRefreshing,
		eRefreshDone,
		eWillLoad,
		eLoading,
		eLoadDone
	}

	private static final int REVERT_DURATION = 1000;

	boolean isLayout;
	private iPullToRefreshViewStateListener mHeader;
	private iPullToRefreshViewStateListener mFooter;
	private View mContainer;

	private float mStartY;
	private float mMovingY;

	private float mOffsetY;

	private int mPullHeight;

	private ePullState  mState = ePullState.eNormal;

	private int ratio = 3;
	private int mRefreshHeight;
	private int mLoadingHeight;

	private boolean mCanRefresh;
	private boolean mCanLoad;

	//是否自动加载
	private boolean mCanAutoLoad = true;

	private PullToRefreshListener mListener;

	public PullToRefreshLayout(Context context) {
		this(context, null);
	}

	public PullToRefreshLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setPullToRefreshListener(PullToRefreshListener listener) {
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
				mMovingY = ev.getY();

				mPullHeight = (int) ((mMovingY - mStartY) / ratio + mOffsetY);
				if(mState == ePullState.eRefreshing && mPullHeight < 0) {
					mPullHeight = 0;
					Log.i(TAG, "cant toRefresh");
					mStartY = ev.getY();
					mOffsetY = 0;
					return super.dispatchTouchEvent(ev);
				}else if(mState == ePullState.eLoading && mPullHeight > 0) {
					mPullHeight =0;
					Log.i(TAG, "cant load");
					mStartY = ev.getY();
					mOffsetY = 0;
					return super.dispatchTouchEvent(ev);
				}
				requestLayout();
				Log.i(TAG, "现在的状态：" + mState);
				if(mPullHeight >= mRefreshHeight) {
					if(mState != ePullState.eRefreshing) {
						mState = ePullState.eWillRefresh;
						mCanRefresh = true;
						mCanLoad = false;
						changeState();
					}
				}else if((mCanAutoLoad && -mPullHeight > 0) || -mPullHeight > mLoadingHeight) {
					if(mState != ePullState.eLoading) {
						mState = ePullState.eWillLoad;
						mCanRefresh = false;
						mCanLoad = true;
						changeState();
					}
				}else if(mState != ePullState.eRefreshing && mState != ePullState.eLoading){
					mState = ePullState.eNormal;
					mCanRefresh = false;
					mCanLoad = false;
					changeState();
				}
				break;
			case MotionEvent.ACTION_UP:
				revertNormal();
				if(mListener != null) {
					if(mCanRefresh) {
						mState = ePullState.eRefreshing;
						changeState();
						mCanRefresh = false;
						mListener.toRefresh();
					}else if(mCanLoad) {
						mState = ePullState.eLoading;
						changeState();
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
			super.onLayout(changed, l, t, r, b);
			mHeader = new PullToRefreshHeader(this);
			mContainer = getChildAt(1);
			mFooter = new PullToRefreshFooter(this);
			mFooter.getView().post(new Runnable() {
				@Override
				public void run() {
					mRefreshHeight = mHeader.getWillLoadingHeight();
					mLoadingHeight = mFooter.getWillLoadingHeight();
				}
			});
			isLayout = true;
			return;
		}
		//下拉
		mHeader.getView().layout(0, mPullHeight - mHeader.getView().getMeasuredHeight(),
				getWidth(), mPullHeight);
		mContainer.layout(0, mPullHeight,
				getWidth(), mPullHeight + mContainer.getHeight());
		mFooter.getView().layout(0, mPullHeight + mFooter.getView().getHeight(), getWidth(),
			2 * mFooter.getView().getHeight() + mPullHeight);
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
				if(mState != ePullState.eLoading && mState != ePullState.eLoadDone
					|| mPullHeight != -mLoadingHeight) {
					mPullHeight += 1;
					if(mPullHeight >=0) {
						mPullHeight = 0;
						mTimer.cancel();
						mTimer = null;
					}
				}
			}else {
				if(mState != ePullState.eRefreshing && mState != ePullState.eRefreshDone
					|| mPullHeight != mRefreshHeight) {
					mPullHeight -= 1;
					if(mPullHeight <= 0) {
						mPullHeight = 0;
						mTimer.cancel();
						mTimer = null;
					}
				}
			}
			requestLayout();
		}
	}

	//恢复正常状态
	public void revertState(final boolean result) {
		if(mState == ePullState.eRefreshing) {
			mState = ePullState.eRefreshDone;
		}else if(mState == ePullState.eLoading) {
			mState = ePullState.eLoadDone;
		}
		changeState(result);
		mCanRefresh = false;
		mCanLoad = false;
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(mState != ePullState.eRefreshing && mState != ePullState.eLoading) {
					mState = ePullState.eNormal;
					changeState(result);
				}
			}
		}, 5000);
	}

	private void changeState() {
		changeState(true);
	}

	private void changeState(boolean result) {
		switch (mState) {
			case eRefreshing:
				mHeader.setLoadingState();
				break;
			case eLoading:
				mFooter.setLoadingState();
				break;
			case eNormal:
				mHeader.setNormalState();
				mFooter.setNormalState();
				break;
			case eWillRefresh:
				mHeader.setWillLoadinState();
				break;
			case eWillLoad:
				mFooter.setWillLoadinState();
				break;
			case eRefreshDone:
				mHeader.setLoadinDoneState(result);
				break;
			case eLoadDone:
				mFooter.setLoadinDoneState(result);
				break;
		}
	}

}
