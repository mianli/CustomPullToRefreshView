package com.mli.crown.pullview;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by crown on 2016/11/9.
 */
public class PullToRefreshView extends RelativeLayout {

	private static final String TAG = "PullView";


	private enum ePullState {
		eNormal,
		eWillRefresh,
		eRefreshing,
		eRefreshDone,
		eWillLoad,
		eLoading,
		eLoadingDone
	}

	private static final int REVERT_DURATION = 1000;

	boolean isLayout;
	private View mHeaderView;
	private View mFooterView;
	private View mContainer;

	private float mStartY;
	private float mMovingY;

	private float mOffsetY;

	private int mPullHeight;

	private ePullState  mState = ePullState.eNormal;

	private int mRefreshHeight = 200;
	private int mLoadingHeight = 200;

	private boolean mCanRefresh;
	private boolean mCanLoad;

	private boolean mCanAutoLoad = true;

	private PullToRefreshListener mListener;

	private TextView mShowStateText;

	public PullToRefreshView(Context context) {
		this(context, null);
	}

	public PullToRefreshView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PullToRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
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
				mHeaderView.setVisibility(VISIBLE);
				mFooterView.setVisibility(VISIBLE);
				mMovingY = ev.getY();

				Log.i(TAG, "pullheight" + mPullHeight);
				mPullHeight = (int) ((mMovingY - mStartY) / 3 + mOffsetY);
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
				}else {
					mPullHeight = (int) ((mMovingY - mStartY) / 3 + mOffsetY);
					requestLayout();
				}
				if(mPullHeight >= mRefreshHeight && mState != ePullState.eRefreshing) {
					mState = ePullState.eWillRefresh;
					mCanRefresh = true;
					mCanLoad = false;
					changeState();
				}else if( ((!mCanAutoLoad && -mPullHeight >= mLoadingHeight)
					|| mCanAutoLoad && -mPullHeight > 0)
					&& mState != ePullState.eLoading) {
					mState = ePullState.eWillLoad;
					mCanRefresh = false;
					mCanLoad = true;
					changeState();
				}else {
					if(mState != ePullState.eRefreshing && mState != ePullState.eLoading) {
						mState = ePullState.eNormal;
					}
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
			isLayout = true;
			super.onLayout(changed, l, t, r, b);
			mHeaderView = findViewById(R.id.header_view);
			mFooterView = findViewById(R.id.footer_view);
			mContainer = findViewById(R.id.container);
			mHeaderView.setVisibility(GONE);
			mFooterView.setVisibility(GONE);

			mShowStateText = (TextView) findViewById(R.id.container);
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
				if(mState != ePullState.eLoading
					|| (mState == ePullState.eLoading && mPullHeight != -mLoadingHeight))
				mPullHeight += 1;
				if(mPullHeight >0) {
					mPullHeight = 0;
					mTimer.cancel();
					mTimer = null;
				}
			}else {
				if(mState != ePullState.eRefreshing
					|| (mState == ePullState.eRefreshing && mPullHeight != mRefreshHeight)) {
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

	//恢复正常状态
	public void revertState() {
		if(mState == ePullState.eRefreshing) {
			mState = ePullState.eRefreshDone;
			changeState();
		}else if(mState == ePullState.eLoading) {
			mState = ePullState.eLoadingDone;
		}
		changeState();
		mState = ePullState.eNormal;
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				changeState();
				mCanRefresh = false;
				mCanLoad = false;
			}
		}, 1000);
	}

	private void changeState() {
		switch (mState) {
			case eRefreshing:
				mShowStateText.setText("正在刷新");
				break;
			case eLoading:
				mShowStateText.setText("正在加载");
				break;
			case eNormal:
				mShowStateText.setText("正常状态");
				break;
			case eWillRefresh:
				mShowStateText.setText("释放刷新");
				break;
			case eWillLoad:
				mShowStateText.setText("释放加载");
				break;
			case eRefreshDone:
				mShowStateText.setText("刷新成功");
				break;
			case eLoadingDone:
				mShowStateText.setText("加载成功");
				break;
		}
	}

}
