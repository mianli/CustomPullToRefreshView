package com.mli.crown.pullview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.mli.crown.pullview.pulltorefresh.PullToRefreshListener;
import com.mli.crown.pullview.pulltorefresh.PullToRefreshView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final PullToRefreshView pullToRefreshView = (PullToRefreshView) findViewById(R.id.pullview);
		pullToRefreshView.setPullToRefreshListener(new PullToRefreshListener() {
			@Override
			public void toRefresh() {
				SimService.simService(MainActivity.this, new SimService.Callback() {
					@Override
					public void callback(boolean state) {
						Toast.makeText(MainActivity.this, "toRefresh success", Toast.LENGTH_SHORT).show();
						pullToRefreshView.revertState(false);
					}
				});
			}

			@Override
			public void toReLoad() {
				SimService.simService(MainActivity.this, new SimService.Callback() {
					@Override
					public void callback(boolean state) {
						Toast.makeText(MainActivity.this, "load success", Toast.LENGTH_SHORT).show();
						pullToRefreshView.revertState(true);
					}
				});
			}
		});
	}
}
