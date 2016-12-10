package com.mli.crown.pullview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final PullToRefreshView pullToRefreshView = (PullToRefreshView) findViewById(R.id.pullview);
		final TextView tv = (TextView) findViewById(R.id.container);
		pullToRefreshView.setPullToRefreshListener(new pullToRefreshListener() {
			@Override
			public void toRefresh() {
				SimService.simService(MainActivity.this, new SimService.Callback() {
					@Override
					public void callback(boolean state) {
						tv.setText("refreshed");
						Toast.makeText(MainActivity.this, "toRefresh success", Toast.LENGTH_SHORT).show();
						pullToRefreshView.revertState();
					}
				});
			}

			@Override
			public void toReLoad() {
				SimService.simService(MainActivity.this, new SimService.Callback() {
					@Override
					public void callback(boolean state) {
						tv.setText("loaded");
						Toast.makeText(MainActivity.this, "load success", Toast.LENGTH_SHORT).show();
						pullToRefreshView.revertState();
					}
				});
			}
		});
//		Handler handler = new Handler();
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				pullView.setState(PullView.REVERT_STATE);
//			}
//		}, 10000);
	}
}
