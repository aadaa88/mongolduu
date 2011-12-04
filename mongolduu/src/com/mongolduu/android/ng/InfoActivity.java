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

import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.misc.HttpConnector;
import com.mongolduu.android.ng.misc.Utils;

public class InfoActivity extends OrmLiteGDActivity<DatabaseHelper> {
	private class FetchInfoTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String infoURL = params[0];
			String content = null;
			try {
				content = HttpConnector.httpGET(infoURL);
			} catch (Exception e) {
				Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
			}
			return content;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				webview.loadData(result, "text/html", "UTF-8");
			} else {
				webview.loadData(getRawResourceContent(R.raw.info), "text/html", "UTF-8");
			}
			
			progressbar.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
	}
	
	private WebView webview;
	private ProgressBar progressbar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.info);
		setTitle(R.string.info_activity_title);

		webview = (WebView) findViewById(R.id.webview);
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webview.getSettings().setJavaScriptEnabled(false);
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url != null && url.startsWith("http://")) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				} else if (url != null && url.startsWith("mailto:")) {
					MailTo mailto = MailTo.parse(url);
					startActivity(Utils.createEMailComposeIntent(InfoActivity.this, mailto.getTo(), mailto.getSubject(), mailto.getBody(), mailto.getCc()));
					view.reload();
					return true;
				} else {
					return false;
				}
			}
		});

		webview.loadData(getRawResourceContent(R.raw.loading), "text/html", "UTF-8");
		
		progressbar = (ProgressBar) findViewById(R.id.progressbar);
		
		new FetchInfoTask().execute(IMongolduuConstants.INFO_URL);
	}
	
	private String getRawResourceContent(int rawResourceId) {
		String content = "";
		InputStream input = null;
		try {
			input = getResources().openRawResource(rawResourceId);
			content = Utils.copyStreamToString(input);
		} catch (IOException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		return content;
	}

	private static final int CONTEXT_MENU_HOME = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONTEXT_MENU_HOME, 0, R.string.context_menu_home);
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}