package com.mli.crown.pullview.pulltorefresh.base;

import android.view.View;

/**
 * Created by crown on 2016/12/12.
 */
public interface iPullToRefreshViewStateListener {

	View getView();
	int getWillLoadingHeight();
	void setNormalState();
	void setWillLoadinState();
	void setLoadingState();
	void setLoadinDoneState(boolean success);

}
