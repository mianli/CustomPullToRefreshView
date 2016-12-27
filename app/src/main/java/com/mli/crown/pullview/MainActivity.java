package com.mli.crown.pullview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mli.crown.pullview.pulltorefresh.base.PullGridView;
import com.mli.crown.pullview.pulltorefresh.base.PullListView;
import com.mli.crown.pullview.pulltorefresh.base.PullToRefreshListener;
import com.mli.crown.pullview.pulltorefresh.base.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	private List<String> mlist = new ArrayList<>();
	private MyAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		SimService.initSimservice();

		final PullToRefreshLayout pullToRefreshView = (PullToRefreshLayout) findViewById(R.id.pulllistlayout);
		pullToRefreshView.setPullToRefreshListener(new PullToRefreshListener() {
			@Override
			public void toRefresh() {
				SimService.simService(MainActivity.this, new SimService.Callback() {
					@Override
					public void callback(List<String> list) {
						Toast.makeText(MainActivity.this, "toRefresh end", Toast.LENGTH_SHORT).show();
						if (list != null && list.size() > 0) {
							mlist.clear();
							mlist.addAll(list);
							mAdapter.notifyDataSetChanged();
							pullToRefreshView.revertState(true);
						}else {
							pullToRefreshView.revertState(false);
						}
					}
				});
			}

			@Override
			public void toReLoad() {
				SimService.simService(MainActivity.this, new SimService.Callback() {
					@Override
					public void callback(List<String> list) {
						if (list != null && list.size() > 0) {
							mlist.addAll(list);
							mAdapter.notifyDataSetChanged();
							pullToRefreshView.revertState(true);
						}else {
							pullToRefreshView.revertState(false);
						}
						Toast.makeText(MainActivity.this, "load end", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		PullGridView listView = (PullGridView) findViewById(R.id.listview);
		pullToRefreshView.setDispatchListener(listView);
		mAdapter = new MyAdapter();
		listView.setAdapter(mAdapter);
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mlist.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.layout_listitem, null);
			TextView tv = (TextView) view.findViewById(R.id.item_tv);
			tv.setText(mlist.get(position));
			return view;
		}
	}

}
