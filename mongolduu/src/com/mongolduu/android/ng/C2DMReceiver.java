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

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.mongolduu.android.ng.db.News;
import com.mongolduu.android.ng.misc.HttpConnector;
import com.mongolduu.android.ng.misc.Utils;

public class C2DMReceiver extends C2DMBaseReceiver {

	public C2DMReceiver() {
		super(IMongolduuConstants.NOTIFICATION_SERVICE_MAIL);
	}
	

	@Override
	public void onRegistered(Context context, String registrationId) {
		Log.w("C2DMReceiver-onRegistered", registrationId);
	}

	@Override
	public void onUnregistered(Context context) {
		Log.w("C2DMReceiver-onUnregistered", "got here!");
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.w("C2DMReceiver-onError", errorId);
		Toast.makeText(context, "Push notification error: " + errorId, 3000).show();
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.w("C2DMReceiver", intent.getStringExtra("message"));
		News news = null;
		try {
			JSONObject json = new JSONObject(intent.getStringExtra("message"));
			news = HttpConnector.parseNews(json);
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		if (news != null) {
			Utils.showNewsReceivedNotification(context, news);
		}
	}
}