package com.mli.crown.pullview.pulltorefresh.base;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.mli.crown.pullview.R;

/**
 * Created by crown on 2016/12/11.
 */
public class PullToRefreshHeader implements iPullToRefreshViewStateListener{

	public View mView;
	public TextView mStateTv;
	private ImageView mRefreshImgv;

	private ObjectAnimator animator;

	public PullToRefreshHeader(View view) {
		mView = view.findViewById(R.id.header_view);
		mView.post(new Runnable() {
			@Override
			public void run() {
				mStateTv = (TextView) mView.findViewById(R.id.header_tv);
				mRefreshImgv = (ImageView) mView.findViewById(R.id.refresh_imgv);
				animator = ObjectAnimator.ofFloat(mRefreshImgv, "rotation", 360.f, 0.f);
				animator.setRepeatCount(-1);
				animator.setInterpolator(new LinearInterpolator());
				animator.setDuration(700);
			}
		});
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
		mStateTv.setText("松手刷新");
	}

	@Override
	public void setLoadingState() {
		mStateTv.setText("正在刷新...");
		startAnimation();
	}

	@Override
	public void setNormalState() {
		mStateTv.setText("下拉刷新");
	}

	@Override
	public void setLoadinDoneState(boolean success) {
		mStateTv.setText(success ? "刷新成功" : "刷新失败");
		if(animator != null) {
			animator.cancel();
		}
		mRefreshImgv.setVisibility(View.GONE);
	}

	private void startAnimation() {
		animator.start();
		mRefreshImgv.setVisibility(View.VISIBLE);
	}

}
