package com.mli.crown.pullview;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by crown on 2016/12/8.
 */
public class SimService {

	private static List<String> list = new ArrayList<>();
	private static int i = 0;

	public static void initSimservice() {
		for (int i = 0; i < 100; i++) {
			list.add(String.valueOf(i));
		}
	}

	public interface Callback {
		void callback(List<String> list);
	}

	//模拟加载数据
	public static void simService(Context context, final Callback callback) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				List<String> data = new ArrayList<String>();
				for (int pos = i; pos < 15 + i; pos++) {
					if(pos == list.size()) {
						break;
					}else  {
						data.add(list.get(pos));
					}
				}
				i += 15;

				callback.callback(data);
			}
		}, 4000);
	}

}
