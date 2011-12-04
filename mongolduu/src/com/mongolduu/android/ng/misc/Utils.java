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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;
import com.google.android.c2dm.C2DMessaging;
import com.mongolduu.android.ng.ChartActivity;
import com.mongolduu.android.ng.IMongolduuConstants;
import com.mongolduu.android.ng.IMongolduuConstants.StorageState;
import com.mongolduu.android.ng.InfoActivity;
import com.mongolduu.android.ng.MainActivity;
import com.mongolduu.android.ng.MediaPlayerActivity;
import com.mongolduu.android.ng.MediaPlayerService;
import com.mongolduu.android.ng.NewsActivity;
import com.mongolduu.android.ng.PickSongActivity;
import com.mongolduu.android.ng.PlaylistActivity;
import com.mongolduu.android.ng.SearchActivity;
import com.mongolduu.android.ng.SettingsActivity;
import com.mongolduu.android.ng.SongsActivity;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.db.News;
import com.mongolduu.android.ng.db.Playlist;
import com.mongolduu.android.ng.db.SongInfo;

public class Utils {
	public static void copyStream(InputStream in, OutputStream out) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = in.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				out.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		} finally {
			try {
				in.close();
			} catch (Exception ex) {
			}
			try {
				out.close();
			} catch (Exception ex) {
			}
		}
	}

	public static byte[] copyStreamToByteArray(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copyStream(in, out);
		byte[] buffer = out.toByteArray();
		if (buffer.length == 0)
			return null;
		return buffer;
	}

	public static String copyStreamToString(InputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line, result = "";
		while ((line = br.readLine()) != null)
			result += line;
		br.close();
		return result;
	}

	public static Intent createHomeIntent(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	public static Intent createInfoIntent(Context context) {
		Intent intent = new Intent(context, InfoActivity.class);
		return intent;
	}

	public static Intent createSettingsIntent(Context context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		return intent;
	}

	public static Intent createNewsIntent(Context context) {
		Intent intent = new Intent(context, NewsActivity.class);
		return intent;
	}

	public static Intent createSearchIntent(Context context) {
		Intent intent = new Intent(context, SearchActivity.class);
		return intent;
	}

	public static Intent createSearchIntent(Context context, long songId) {
		Intent intent = createSearchIntent(context);
		intent.putExtra(IMongolduuConstants.SONG_ID_EXTRA_ID, songId);
		return intent;
	}

	public static Intent createChartIntent(Context context) {
		Intent intent = new Intent(context, ChartActivity.class);
		return intent;
	}

	public static Intent createPlaylistIntent(Context context) {
		Intent intent = new Intent(context, PlaylistActivity.class);
		return intent;
	}

	public static Intent createSongsIntent(Context context, String playlistName) {
		Intent intent = new Intent(context, SongsActivity.class);
		intent.putExtra(IMongolduuConstants.PLAYLIST_NAME_EXTRA_ID, playlistName);
		return intent;
	}

	public static Intent createPickSongIntent(Context context) {
		Intent intent = new Intent(context, PickSongActivity.class);
		return intent;
	}

	public static Intent createMediaPlayerIntent(Context context) {
		Intent intent = new Intent(context, MediaPlayerActivity.class);
		return intent;
	}

	public static Intent createMediaPlayerIntent(Context context, String playlistName, int playlistPosition) {
		Intent intent = createMediaPlayerIntent(context);
		intent.putExtra(IMongolduuConstants.PLAYLIST_NAME_EXTRA_ID, playlistName);
		intent.putExtra(IMongolduuConstants.PLAYLIST_POSITION_EXTRA_ID, playlistPosition);
		return intent;
	}

	public static Intent createMediaPlayerServiceIntent(Context context) {
		Intent intent = new Intent(context, MediaPlayerService.class);
		return intent;
	}

	public static Intent createEMailComposeIntent(Context context, String address, String subject, String body, String cc) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
		intent.putExtra(Intent.EXTRA_TEXT, body);
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_CC, cc);
		intent.setType("message/rfc822");
		return intent;
	}

	public static File getStorageDirectory() {
		return new File(Environment.getExternalStorageDirectory(), IMongolduuConstants.STORAGE_DIRECTORY);
	}

	public static boolean isExternalStorageMounted() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static StorageState checkStorageDirectory() {
		if (!isExternalStorageMounted()) {
			return StorageState.EXTERNAL_STORAGE_NOT_MOUNTED;
		}
		StatFs statfs = new StatFs(Environment.getExternalStorageDirectory().getPath());
		long size = ((long) statfs.getAvailableBlocks()) * ((long) statfs.getBlockSize());
		if (size < IMongolduuConstants.REQUIRED_EXTERNAL_STORAGE_MINIMUM_FREE_SIZE) {
			return StorageState.NO_SPACE_ON_EXTERNAL_STORAGE;
		}
		File directory = getStorageDirectory();
		if (!directory.exists()) {
			return directory.mkdir() ? StorageState.SUCCESSFUL : StorageState.UNKNOWN_ERROR;
		}
		return StorageState.SUCCESSFUL;
	}

	public static File getSongFile(long id) {
		return new File(Utils.getStorageDirectory(), id + ".dat");
	}

	public static File getRingtoneFile() {
		return new File(Utils.getStorageDirectory(), IMongolduuConstants.RINGTONE_FILE);
	}

	public static boolean existsValidSongFile(long id) {
		File file = getSongFile(id);
		return file.exists() && file.length() > 1024;
	}

	public static String getMD5String(String str) {
		String hash = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(str.getBytes());
			hash = new BigInteger(1, md5.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return hash;
	}

	public static void showAlertDialog(Context context, int title, int message) {
		showAlertDialog(context, context.getString(title), context.getString(message));
	}

	public static void showAlertDialog(Context context, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setCancelable(true).setNeutralButton(context.getString(R.string.button_close), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		if (title != null) {
			builder.setTitle(title);
		}
		builder.create().show();
	}

	public static String formatNewsTimestamp(Context context, long timestamp, long currentTimestamp) {
		String string = "";

		long offset = currentTimestamp * 1000 - timestamp * 1000;
		if (offset < 0) {
			offset = 0;
		}
		long diffDays = offset / (24 * 60 * 60 * 1000);
		if (diffDays > 30) {
			SimpleDateFormat dateparser = new SimpleDateFormat(context.getString(R.string.news_date_format));
			string = dateparser.format(new Date(timestamp * 1000));
		} else if (diffDays > 0) {
			if (diffDays == 1) {
				string = diffDays + " " + context.getString(R.string.news_day_old);
			} else {
				string = diffDays + " " + context.getString(R.string.news_days_old);
			}
		} else {
			long diffHours = offset / (60 * 60 * 1000);

			if (diffHours > 0) {
				if (diffHours == 1) {
					string = diffHours + " " + context.getString(R.string.news_hour_old);
				} else {
					string = diffHours + " " + context.getString(R.string.news_hours_old);
				}
			} else {
				long diffMinutes = offset / (60 * 1000);
				if (diffMinutes >= 0) {
					if (diffMinutes <= 1) {
						string = diffMinutes + " " + context.getString(R.string.news_minute_old);
					} else {
						string = diffMinutes + " " + context.getString(R.string.news_minutes_old);
					}
				}
			}
		}

		return string;
	}

	public static void showNewsReceivedNotification(Context applicationContext, News news) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(applicationContext);
		if (!settings.getBoolean(IMongolduuConstants.NEWS_NOTIFICATION_SETTINGS_ID, true)) {
			return;
		}

		String tickerText = applicationContext.getString(R.string.news_notification_ticker);
		String contentTitle = applicationContext.getString(R.string.news_notification_title);
		String contentText = news.text;
		contentText = contentText.replaceAll("<.*?>", "");

		showNotification(applicationContext, 1, tickerText, contentTitle, contentText, news.timestamp * 1000);
	}

	public static void showNotification(Context applicationContext, int notificationId, String tickerText, String contentTitle, String contentText, long when) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) applicationContext.getSystemService(ns);

		int icon = R.drawable.notification_icon;
		Notification notification = new Notification(icon, tickerText, when);

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_LIGHTS;

		Context context = applicationContext;
		Intent notificationIntent = new Intent(context, NewsActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Intent.FLAG_ACTIVITY_NEW_TASK
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(applicationContext, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(notificationId, notification);
	}

	public static void downloadSong(Context context, ListView listview, SongInfo songinfo) {
		if (songinfo != null) {
			switch (Utils.checkStorageDirectory()) {
			case EXTERNAL_STORAGE_NOT_MOUNTED:
				showAlertDialog(context, R.string.alert_dialog_title, R.string.alert_dialog_message_nosdcard);
				break;
			case NO_SPACE_ON_EXTERNAL_STORAGE:
				showAlertDialog(context, R.string.alert_dialog_title, R.string.alert_dialog_message_nospace);
				break;
			case UNKNOWN_ERROR:
				showAlertDialog(context, R.string.alert_dialog_title, R.string.alert_dialog_message_unknown);
				break;
			default:
				new DownloadSongTask(context, listview, songinfo).execute();
				break;
			}
		}
	}

	public static boolean isAllSongPlaylist(Playlist playlist) {
		return isAllSongPlaylist(playlist.name);
	}

	public static boolean isAllSongPlaylist(String playlistName) {
		return TextUtils.isEmpty(playlistName);
	}

	public static List<SongInfo> getSongs(DatabaseHelper dbHelper, Playlist playlist) throws SQLException {
		List<SongInfo> songinfos = new LinkedList<SongInfo>();
		if (playlist == null || isAllSongPlaylist(playlist)) {
			songinfos.addAll(dbHelper.getSongInfoDao().queryForAll());
		} else {
			for (int i = 0; i < playlist.songs.size(); i++) {
				Long songId = playlist.songs.get(i);
				SongInfo songinfo = dbHelper.getSongInfoDao().queryForId(songId);
				if (songinfo != null) {
					songinfos.add(songinfo);
				}
			}
		}
		return songinfos;
	}

	public static void checkIfSongSavedOnDevice(DatabaseHelper dbHelper, List<SongInfo> songinfos, boolean isSDCardMounted) throws SQLException {
		for (int i = 0; i < songinfos.size(); i++) {
			checkIfSongSavedOnDevice(dbHelper, songinfos.get(i), isSDCardMounted);
		}
	}

	public static void checkIfSongSavedOnDevice(DatabaseHelper dbHelper, SongInfoListAdapter songinfos, boolean isSDCardMounted) throws SQLException {
		for (int i = 0; i < songinfos.getCount(); i++) {
			checkIfSongSavedOnDevice(dbHelper, songinfos.getItem(i), isSDCardMounted);
		}
	}

	public static void checkIfSongSavedOnDevice(DatabaseHelper dbHelper, SongInfo songinfo, boolean isSDCardMounted) throws SQLException {
		boolean existsOnDB = dbHelper.getSongInfoDao().queryForId(songinfo.id) != null;
		songinfo.isSavedOnDevice = existsOnDB;
		if (songinfo.isSavedOnDevice && isSDCardMounted) {
			songinfo.isSavedOnDevice = Utils.existsValidSongFile(songinfo.id);
		}
	}

	public static void deleteSongFromDevice(DatabaseHelper dbHelper, long id) throws SQLException {
		SongInfo songinfo = dbHelper.getSongInfoDao().queryForId(id);
		if (songinfo != null) {
			dbHelper.getSongInfoDao().delete(songinfo);
			List<Playlist> playlists = dbHelper.getPlaylistDao().queryForAll();
			for (int i = 0; i < playlists.size(); i++) {
				Playlist playlist = playlists.get(i);
				while (playlist.songs.remove(songinfo.id))
					;
				dbHelper.getPlaylistDao().update(playlist);
			}
			File file = Utils.getSongFile(songinfo.id);
			if (file.exists()) {
				file.delete();
			}
		}
	}

	public static void playSongInAllSongsPlaylist(Context context, DatabaseHelper dbHelper, SongInfo songinfo) {
		try {
			Playlist playlist = new Playlist();
			List<SongInfo> songs = Utils.getSongs(dbHelper, playlist);
			int position = 0;
			for (int i = 0; i < songs.size(); i++) {
				if (songs.get(i).id == songinfo.id) {
					position = i;
					break;
				}
			}
			context.startActivity(Utils.createMediaPlayerIntent(context, playlist.name, position));
		} catch (SQLException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
			Utils.showAlertDialog(context, R.string.alert_dialog_title, R.string.alert_dialog_message_database_error);
		}
	}

	public static SpannableStringBuilder makeSongInfoClickable(final Context context, String htmltext) {
		SpannableStringBuilder stringBuilder = new SpannableStringBuilder(Html.fromHtml(htmltext));
		URLSpan[] urls = stringBuilder.getSpans(0, stringBuilder.length(), URLSpan.class);
		for (URLSpan span : urls) {
			if (span.getURL() != null && span.getURL().startsWith(IMongolduuConstants.CLICKABLE_SONG_INFO_URL)) {
				int start = stringBuilder.getSpanStart(span);
				int end = stringBuilder.getSpanEnd(span);
				int flags = stringBuilder.getSpanFlags(span);
				stringBuilder.removeSpan(span);
				try {
					final long songId = Long.parseLong(span.getURL().substring(IMongolduuConstants.CLICKABLE_SONG_INFO_URL.length()));
					ClickableSpan myActivityLauncher = new ClickableSpan() {
						public void onClick(View view) {
							context.startActivity(createSearchIntent(context, songId));
						}
					};
					stringBuilder.setSpan(myActivityLauncher, start, end, flags);
				} catch (NumberFormatException e) {
					Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
				}
			}
		}
		return stringBuilder;
	}

	public static void registerDevice(final Context context, boolean immediately) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						String registrationID = C2DMessaging.getRegistrationId(context);
						if (registrationID != null && registrationID.length() > 0) {
							SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
							if (settings.getBoolean(IMongolduuConstants.NEWS_NOTIFICATION_SETTINGS_ID, true)) {
								HttpConnector.registerDevice(registrationID);
							} else {
								HttpConnector.deregisterDevice(registrationID);
							}
						}
						return null;
					}
				}.execute();
			}
		};
		if (immediately) {
			new Handler().post(runnable);
		} else {
			new Handler().postDelayed(runnable, IMongolduuConstants.DEVICE_REGISTER_DELAY);
		}
	}

	public static void setSongAsRingtone(final Context context, final SongInfo songinfo) {
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
			private ProgressDialog dialog;
			
			@Override
			protected void onPreExecute() {
				dialog = ProgressDialog.show(context, "", context.getString(R.string.progress_dialog_set_as_ringtone), true, false);
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					if (songinfo != null && songinfo.id > 0) {
						File songfile = getSongFile(songinfo.id);
						File ringtoneFile = getRingtoneFile();
						copyStream(new FileInputStream(songfile), new FileOutputStream(ringtoneFile));

						context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(ringtoneFile)));

						ContentValues values = new ContentValues();
						values.put(MediaStore.MediaColumns.DATA, ringtoneFile.getAbsolutePath());
						values.put(MediaStore.MediaColumns.TITLE, songinfo.title);
						values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
						values.put(MediaStore.Audio.Media.ARTIST, songinfo.artist);
						values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
						values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
						values.put(MediaStore.Audio.Media.IS_ALARM, true);
						values.put(MediaStore.Audio.Media.IS_MUSIC, false);

						try {
							context.getContentResolver().delete(MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath()), null, null);
						} catch (Exception e) {
						}

						Uri uri = context.getContentResolver().insert(MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath()), values);
						RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, uri);
						return true;
					}
				} catch (Exception e) {
					Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
				}
				return false;
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				dialog.dismiss();
				
				if (result) {
					Toast.makeText(context, R.string.toast_message_set_as_ringtone, Toast.LENGTH_SHORT).show();
				} else {
					Utils.showAlertDialog(context, R.string.alert_dialog_title, R.string.alert_dialog_message_unknown);
				}
			}
		};
		task.execute();
	}
}
