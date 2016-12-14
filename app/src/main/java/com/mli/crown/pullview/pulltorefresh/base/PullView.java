package com.mli.crown.pullview.pulltorefresh.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;

import com.mli.crown.pullview.R;

/**
 * Created by crown on 2016/12/14.
 */
public class PullView extends LinearLayout {

	public PullView(Context context) {
		this(context, null);
	}

	public PullView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PullView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.activity_main, this, true);
	}

}
