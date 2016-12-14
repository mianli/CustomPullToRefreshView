package com.mli.crown.pullview;

import android.content.Context;
import android.os.Handler;

/**
 * Created by crown on 2016/12/8.
 */
public class SimService {

	public interface Callback {
		void callback(boolean state);
	}

	//模拟加载数据
	public static void simService(Context context, final Callback callback) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				callback.callback(true);
			}
		}, 10000);
	}

}
