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

package com.mongolduu.android.ng.misc;

import java.io.File;
import java.sql.SQLException;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.IMongolduuConstants;
import com.mongolduu.android.ng.OrmLiteGDActivity;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.db.SongInfo;

public class DownloadSongTask extends AsyncTask<Void, Integer, Boolean> {
	private Context context;
	private ListView listview;
	private SongInfo songinfo;

	private Dialog dialog;
	private ProgressBar progressbar;

	public DownloadSongTask(Context context, ListView listview, SongInfo songinfo) {
		this.context = context;
		this.listview = listview;
		this.songinfo = songinfo;
	}

	@Override
	protected void onPreExecute() {
		dialog = new Dialog(context);
		dialog.setContentView(R.layout.download_dialog);
		dialog.setTitle(R.string.downloading_dialog_title);
		((TextView) dialog.findViewById(R.id.song_title)).setText(songinfo.title);
		((TextView) dialog.findViewById(R.id.song_artist)).setText(songinfo.artist);
		progressbar = (ProgressBar) dialog.findViewById(R.id.download_progressbar);
		progressbar.setIndeterminate(false);
		progressbar.setMax(100);
		((Button) dialog.findViewById(R.id.download_cancel_button)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				DownloadSongTask.this.cancel(true);
				dialog.dismiss();
				Toast.makeText(context, R.string.toast_message_download_cancelled, Toast.LENGTH_LONG).show();
			}
		});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				DownloadSongTask.this.cancel(true);
				Toast.makeText(context, R.string.toast_message_download_cancelled, Toast.LENGTH_LONG).show();
			}
		});
		dialog.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		return HttpConnector.downloadSong(songinfo.id, this);
	}

	public void publishProgressFromOtherProcess(Integer... values) {
		publishProgress(values);
	}

	@Override
	public void onProgressUpdate(Integer... values) {
		progressbar.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		dialog.dismiss();
		
		@SuppressWarnings("unchecked")
		DatabaseHelper dbHelper = ((OrmLiteGDActivity<DatabaseHelper>) context).getHelper();
		boolean successful = false;

		if (!isCancelled() && result) {
			if (result) {				
				if (Utils.existsValidSongFile(songinfo.id)) {
					try {
						if (dbHelper.getSongInfoDao().queryForId(songinfo.id) != null) {
							dbHelper.getSongInfoDao().update(songinfo);
						} else {
							dbHelper.getSongInfoDao().create(songinfo);
						}
						successful = true;
					} catch (SQLException e) {
						Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
						Utils.showAlertDialog(context, R.string.alert_dialog_title, R.string.alert_dialog_message_database_error);
					}
				} else {
					Utils.showAlertDialog(context, R.string.alert_dialog_title, R.string.alert_dialog_message_download_failed);
				}
			} else {
				Utils.showAlertDialog(context, R.string.alert_dialog_title, R.string.alert_dialog_message_download_failed);
			}
		}
		
		if (!successful) {
			// remove song file and info!
			try {
				File file = Utils.getSongFile(songinfo.id);
				if (file.exists()) {
					file.delete();
				}
			} catch (Exception e) {
				Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
			}
			try {
				SongInfo temp = dbHelper.getSongInfoDao().queryForId(songinfo.id);
				if (temp != null) {
					dbHelper.getSongInfoDao().delete(temp);
				}
			} catch (Exception e) {
				Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
			}
		}
		
		if (successful){
			Toast.makeText(context, R.string.toast_message_download_ok, Toast.LENGTH_SHORT).show();
			songinfo.isSavedOnDevice = true;
			listview.invalidateViews();
		}

		super.onPostExecute(result);
	}
}
