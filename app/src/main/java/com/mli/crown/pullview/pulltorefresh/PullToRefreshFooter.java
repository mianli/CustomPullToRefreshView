package com.mli.crown.pullview.pulltorefresh;

import android.view.View;
import android.widget.TextView;
import com.mli.crown.pullview.R;

/**
 * Created by crown on 2016/12/11.
 */
public class PullToRefreshFooter implements iPullToRefreshViewStateListener{

	public View mView;
	private TextView mStateTv;

	public PullToRefreshFooter(View view) {
		mView = view.findViewById(R.id.footer_view);
		mStateTv = (TextView) view.findViewById(R.id.footer_tv);
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
	}
}
