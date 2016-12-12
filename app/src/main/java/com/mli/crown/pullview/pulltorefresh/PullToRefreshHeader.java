package com.mli.crown.pullview.pulltorefresh;

import android.view.View;
import android.widget.TextView;

import com.mli.crown.pullview.R;

/**
 * Created by crown on 2016/12/11.
 */
public class PullToRefreshHeader implements iPullToRefreshViewStateListener{

	public View mView;
	public TextView mStateTv;

	public PullToRefreshHeader(View view) {
		mView = view.findViewById(R.id.header_view);
		mView.post(new Runnable() {
			@Override
			public void run() {
				mStateTv = (TextView) mView.findViewById(R.id.header_tv);
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
	}

	@Override
	public void setNormalState() {
		mStateTv.setText("下拉刷新");
	}

	@Override
	public void setLoadinDoneState(boolean success) {
		mStateTv.setText(success ? "刷新成功" : "刷新失败");
	}
}
