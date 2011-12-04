/*
 * Copyright (C) 2011 Erdene-Ochir Tuguldur (https://github.com/tugstugi)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongolduu.android.ng;

import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.db.News;
import com.mongolduu.android.ng.misc.HttpConnector;
import com.mongolduu.android.ng.misc.TextAndProgressBarUtils;
import com.mongolduu.android.ng.misc.Utils;

public class NewsActivity extends OrmLiteGDActivity<DatabaseHelper> {
	private class FetchNewsTask extends AsyncTask<Void, Void, List<News>> {
		private boolean newFetch;
		
		public FetchNewsTask(boolean newFetch) {
			this.newFetch = newFetch;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (newFetch) {
				((NewsListAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter()).getNews().clear();
				listview.invalidateViews();
			}
			TextAndProgressBarUtils.configureTextAndProgressBar(listview, null, false, true);
			TextAndProgressBarUtils.showTextAndProgressBar(listview);
		}

		@Override
		protected List<News> doInBackground(Void... params) {
			long timestamp = -1;
			List<News> news = ((NewsListAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter()).getNews();
			if (news.size() > 0) {
				timestamp = news.get(news.size()-1).timestamp;
			}
			return HttpConnector.fetchNews(newFetch ? -1 : timestamp, IMongolduuConstants.NEWS_SIZE);
		}

		@Override
		protected void onPostExecute(List<News> result) {
			if (!isCancelled() && listview != null) {
				if (result != null) {
					((NewsListAdapter) ((HeaderViewListAdapter) listview.getAdapter()).getWrappedAdapter()).getNews().addAll(result);
					listview.invalidateViews();
		
					if (newFetch) {
						if (result.size() == 0) {
							TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.news_nothing), false, false);
						} else if (result.size() < IMongolduuConstants.NEWS_SIZE) {
							TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.news_nomore), false, false);
						} else {
							TextAndProgressBarUtils.configureTextAndProgressBar(listview, String.format(getString(R.string.news_more), IMongolduuConstants.NEWS_SIZE), true, false);
						}
					} else {
						if (result.size() < IMongolduuConstants.NEWS_SIZE) {
							TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.news_nomore), false, false);
						} else {
							TextAndProgressBarUtils.configureTextAndProgressBar(listview, String.format(getString(R.string.news_more), IMongolduuConstants.NEWS_SIZE), true, false);
						}
					}
				} else {
					TextAndProgressBarUtils.configureTextAndProgressBar(listview, getString(R.string.network_problem), false, false);
				}
			}

			currentTask = null;
			refreshItem.setLoading(false);
			super.onPostExecute(result);
		}
	}
	
	public class NewsListAdapter extends BaseAdapter {
		protected Context context;
		protected List<News> all;

		public NewsListAdapter(Context context, List<News> all) {
			this.context = context;
			this.all = all;
		}

		public int getCount() {
			return all.size();
		}

		public News getItem(int position) {
			return all.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public boolean isEnabled(int position) {
			return true;
		}

		public List<News> getNews() {
			return all;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View res = convertView;
			if (res == null)
				res = LayoutInflater.from(context).inflate(R.layout.news_item, null);

			TextView textview = (TextView) res.findViewById(R.id.news_text);
			TextView timeview = (TextView) res.findViewById(R.id.news_time);

			News result = getItem(position);
			textview.setMovementMethod(LinkMovementMethod.getInstance());
			textview.setText(Utils.makeSongInfoClickable(NewsActivity.this, result.text));
			timeview.setText(Utils.formatNewsTimestamp(NewsActivity.this, result.timestamp, result.currentTimestamp));

			return res;
		}
	}

	
	private LoaderActionBarItem refreshItem = null;
	private ListView listview;
	private View textandprogressbar = null;
	
	private FetchNewsTask currentTask;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.news);
        setTitle(R.string.news_activity_title);
        
        startService(Utils.createMediaPlayerServiceIntent(this));
        
        refreshItem = (LoaderActionBarItem) addActionBarItem(Type.Refresh, R.id.action_bar_refresh);
        addActionBarItem(Type.Search, R.id.action_bar_search);
        
        listview = (ListView) findViewById(R.id.list);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
			}
		});
		
		textandprogressbar = getLayoutInflater().inflate(R.layout.text_and_progressbar, listview, false);
		listview.addFooterView(textandprogressbar, null, false);
		TextAndProgressBarUtils.initializeTextAndProgressBar(listview, textandprogressbar);
		TextAndProgressBarUtils.hideTextAndProgressBar(listview);
		
		listview.setAdapter(new NewsListAdapter(this, new ArrayList<News>()));
		
		refreshNewsList(true);
	}
    
    @Override
    public void onResume() {
    	super.onResume();
    }
    
    private void refreshNewsList(boolean firstFetch) {
    	if (currentTask == null) {
			currentTask = new FetchNewsTask(firstFetch);
			currentTask.execute();
		}
    }

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (item.getItemId()) {
		case R.id.action_bar_search:
			startActivity(Utils.createSearchIntent(this));
			return true;
		case R.id.action_bar_refresh:
			refreshNewsList(true);
			return true;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}
	
	public void onTextAndProgressBarClick(View view) {
		refreshNewsList(false);
	}
	
	private static final int CONTEXT_MENU_HOME = 0;
	private static final int CONTEXT_MENU_REFRESH = 1;
	private static final int CONTEXT_MENU_SEARCH = 2;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONTEXT_MENU_HOME, 0, R.string.context_menu_home);
		menu.add(0, CONTEXT_MENU_REFRESH, 0, R.string.context_menu_refresh);
		menu.add(0, CONTEXT_MENU_SEARCH, 0, R.string.context_menu_search);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CONTEXT_MENU_HOME:
			startActivity(Utils.createHomeIntent(this));
			return true;
		case CONTEXT_MENU_REFRESH:
			//refreshItem.setLoading(true);
			refreshNewsList(true);
			return true;
		case CONTEXT_MENU_SEARCH:
			startActivity(Utils.createSearchIntent(this));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}