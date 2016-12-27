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

	private float mMovingOffset;

	private float mOffsetY;

	private int mPullHeight;

	private ePullState  mState = ePullState.eNormal;

	private int ratio = 3;
	private int mRefreshHeight;
	private int mLoadingHeight;

	private boolean mCanRefresh;
	private boolean mCanLoad;

	boolean mDispatch = false;

	//是否自动加载
	private boolean mCanAutoLoad = true;

	private PullToRefreshListener mListener;

	private iPullToRefreshDispatchListener mDispatchListener = new iPullToRefreshDispatchListener() {
		@Override
		public boolean canRefresh() {
			return true;
		}

		@Override
		public boolean canLoad() {
			return true;
		}
	};

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

	public void setDispatchListener(iPullToRefreshDispatchListener listener) {
		this.mDispatchListener = listener;
	}

	private int mEvents = 0;

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
				mEvents = 0;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_POINTER_UP:
				// 过滤多点触碰
				mEvents = -1;
				break;
			case MotionEvent.ACTION_MOVE:
				if(mEvents == -1) {
					break;
				}

				if(mState == ePullState.eRefreshing && mPullHeight < 0) {//正在刷新的时候，不能进行上拉
					mPullHeight = 0;
					Log.i(TAG, "cant toRefresh");
					mStartY = ev.getY();
					mOffsetY = 0;
					return false;
				}else if(mState == ePullState.eLoading && mPullHeight > 0) {//正在加载的时候，不能进行上拉
					mPullHeight =0;
					Log.i(TAG, "cant load");
					mStartY = ev.getY();
					mOffsetY = 0;
					return false;
				}

				Log.i(TAG, String.valueOf(mDispatchListener.canRefresh()) + mDispatchListener.canLoad());
				mMovingOffset = ev.getY();

				if(mDispatchListener.canRefresh() || mDispatchListener.canLoad()) {
					mMovingY = ev.getY();
					mPullHeight = (int) ((mMovingY - mStartY) / ratio + mOffsetY);
					requestLayout();
				}

				Log.i(TAG, "现在的状态：" + mState);
				if(mPullHeight >= mRefreshHeight) {//进行下拉，超过可刷新的高度
					if(mState != ePullState.eRefreshing) {//如果不是在刷新就进行刷新
						mState = ePullState.eWillRefresh;
						mCanRefresh = true;
						mCanLoad = false;
						mDispatch = false;
						changeState();
					}
					Log.i(TAG, "will refresh dispatch:" + mDispatch);
				//拖动到底部能自动进行加载并有上拉动作或者上拉超过上拉加载的高度值,如果不是在加载就进行加载
				}else if((mCanAutoLoad && -mPullHeight > 0 && mDispatchListener.canLoad()) || -mPullHeight > mLoadingHeight) {
					if(mState != ePullState.eLoading ) {
						mState = ePullState.eWillLoad;
						mCanRefresh = false;
						mCanLoad = true;
						changeState();
					}
					Log.i(TAG, "will load dispatch:" + mDispatch);
				//如果不是在刷新状态，也不是在加载状态，拖动的过程将状态重置为正常状态
				}else if(mState != ePullState.eRefreshing && mState != ePullState.eLoading){
					mState = ePullState.eNormal;
					mCanRefresh = false;
					mCanLoad = false;
					changeState();
					Log.i(TAG, "dispatch:" + mDispatch);
				}
				if(mDispatchListener.canRefresh() && mPullHeight > 0 || mDispatchListener.canLoad() && mPullHeight < 0) {
					return false;
				}
				break;
			case MotionEvent.ACTION_UP:
				//释放恢复状态
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
		return super.dispatchTouchEvent(ev);
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
				mDispatch = true;
				break;
			case eLoadDone:
				mFooter.setLoadinDoneState(result);
				mDispatch = true;
				break;
		}
	}

}
