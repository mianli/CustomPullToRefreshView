package com.mli.crown.pullview.pulltorefresh.base;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.mli.crown.pullview.R;

/**
 * Created by crown on 2016/12/11.
 */
public class PullToRefreshFooter implements iPullToRefreshViewStateListener{

	public View mView;
	private TextView mStateTv;
	private ImageView mLoadingImgv;
	private ObjectAnimator mAnimator;


	public PullToRefreshFooter(View view) {
		mView = view.findViewById(R.id.footer_view);
		mStateTv = (TextView) view.findViewById(R.id.footer_tv);
		mLoadingImgv = (ImageView) mView.findViewById(R.id.loading_imgv);
		mAnimator = ObjectAnimator.ofFloat(mLoadingImgv, "rotation", 360.f, 0.f);
		mAnimator.setDuration(700);
		mAnimator.setRepeatCount(-1);
		mAnimator.setInterpolator(new LinearInterpolator());
	}

	@Override
	public View getView() {
		return mView;
	}

	@Override
	public int getWillLoadingHeight() {
		return mStateTv.getHeight() * 2;
	}

	@Override
	public void setWillLoadinState() {
		mStateTv.setText("松手加载");
	}

	@Override
	public void setLoadingState() {
		mStateTv.setText("正在加载...");
		startAnimation();
	}

	@Override
	public void setNormalState() {
		mStateTv.setText("上拉加载");
	}

	@Override
	public void setLoadinDoneState(boolean success) {
		if(success) {
			mStateTv.setText("加载成功");
		}else {
			mStateTv.setText("加载失败");
		}
		if(mAnimator != null) {
			mAnimator.cancel();
		}
		mLoadingImgv.setVisibility(View.GONE);
	}

	private void startAnimation() {
		mLoadingImgv.setVisibility(View.VISIBLE);
		mAnimator.start();
	}

}
