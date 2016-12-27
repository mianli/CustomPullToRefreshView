package com.mli.crown.pullview.pulltorefresh.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Created by crown on 2016/12/14.
 */
public class PullListView extends ListView implements iPullToRefreshDispatchListener{

	public PullListView(Context context) {
		super(context);
	}

	public PullListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public PullListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public boolean canRefresh() {
		boolean result=false;
		if(this.getFirstVisiblePosition()==0){
			final View topChildView = this.getChildAt(0);
			if(topChildView != null) {
				result=topChildView.getTop()==0;
			}else {
				result = true;
			}
		}
		return result ;
	}

	@Override
	public boolean canLoad() {
		boolean result=false;
		if (getLastVisiblePosition() == (getCount() - 1)) {
			final View bottomChildView = getChildAt(getLastVisiblePosition() - getFirstVisiblePosition());
			if(bottomChildView != null) {
				result= (getHeight()>=bottomChildView.getBottom());
			}else {
				result =true;
			}
		};
		return  result;
	}

}
