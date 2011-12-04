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

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.MediaPlayerService.LocalBinder;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.db.Playlist;
import com.mongolduu.android.ng.db.SongInfo;
import com.mongolduu.android.ng.misc.HttpConnector;
import com.mongolduu.android.ng.misc.Utils;

public class MediaPlayerActivity extends OrmLiteGDActivity<DatabaseHelper> {
	private class FetchAlbumImageTask extends AsyncTask<Long, Void, Bitmap> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Bitmap doInBackground(Long... params) {
			return HttpConnector.downloadAlbumImage(params[0]);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (!isCancelled() && result != null) {
				if (albumImageView != null) {
					albumImageView.setImageBitmap(result);
				}
			}
			super.onPostExecute(result);
		}
	}

	private static final DecimalFormat timeFormat = new DecimalFormat("#00");

	private MediaPlayerService service;

	private TextView artistNameText;
	private TextView songNameText;
	private TextView albumNameText;

	private ImageView albumImageView;

	private ImageButton repeatButton;
	private ImageButton shuffleButton;

	private SeekBar seekbar;

	private TextView songCurrentPositionText;
	private TextView songDurationText;

	private ImageButton rewindButton;
	private ImageButton playButton;
	private ImageButton forwardButton;

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((LocalBinder) binder).getService();
			playSongs();
			service.hideNotification();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addActionBarItem(Type.List, R.id.action_bar_list);
		bindService(Utils.createMediaPlayerServiceIntent(this), serviceConnection, Context.BIND_AUTO_CREATE);
		
		initUI();
	}

	private void initUI() {
		setActionBarContentView(R.layout.media_player);

		artistNameText = (TextView) findViewById(R.id.artist_name);
		songNameText = (TextView) findViewById(R.id.song_name);
		albumNameText = (TextView) findViewById(R.id.album_name);

		albumImageView = (ImageView) findViewById(R.id.album_image_view);

		repeatButton = (ImageButton) findViewById(R.id.repeat_button);
		repeatButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service != null) {
					if (service.isRepeating()) {
						service.setRepeat(false);
						repeatButton.setImageResource(R.drawable.repeat_disabled);
					} else {
						service.setRepeat(true);
						repeatButton.setImageResource(R.drawable.repeat_enabled);
					}
				}
			}
		});
		shuffleButton = (ImageButton) findViewById(R.id.shuffle_button);
		shuffleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service != null) {
					if (service.isShuffling()) {
						service.setShuffle(false);
						shuffleButton.setImageResource(R.drawable.shuffle_disabled);
					} else {
						service.setShuffle(true);
						shuffleButton.setImageResource(R.drawable.shuffle_enabled);
					}
				}
			}
		});

		seekbar = (SeekBar) findViewById(R.id.song_progress);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (service != null && fromUser) {
					service.seekTo(progress);
				}
			}
		});

		songCurrentPositionText = (TextView) findViewById(R.id.song_current_position);
		songDurationText = (TextView) findViewById(R.id.song_duration);

		rewindButton = (ImageButton) findViewById(R.id.rewind_button);
		rewindButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service != null) {
					service.rewind();
					if (service.isErrorOccuredDuringPlayback()) {
						Utils.showAlertDialog(MediaPlayerActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_play_error);
					}
				}
			}
		});
		playButton = (ImageButton) findViewById(R.id.play_button);
		playButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service != null) {
					if (service.isPlaying()) {
						service.pause();
						playButton.setImageResource(R.drawable.play);
					} else {
						service.resume();
						if (service.isErrorOccuredDuringPlayback()) {
							Utils.showAlertDialog(MediaPlayerActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_play_error);
						}
						playButton.setImageResource(R.drawable.pause);
					}
				}
			}
		});
		forwardButton = (ImageButton) findViewById(R.id.forward_button);
		forwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (service != null) {
					service.forward();
					if (service.isErrorOccuredDuringPlayback()) {
						Utils.showAlertDialog(MediaPlayerActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_play_error);
					}
				}
			}
		});
		
		lastId = -1;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		initUI();
	}

	@Override
	public void onDestroy() {
		unbindService(serviceConnection);
		super.onDestroy();
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		playSongs();
		if (service != null) {
			service.hideNotification();
		}
	}

	private static final int UI_UPDATE_INTERVAL = 100;
	private Handler handler = new Handler();
	private Runnable uiUpdateTask = new Runnable() {
		@Override
		public void run() {
			updateMediaPlayerView();
			handler.postDelayed(this, UI_UPDATE_INTERVAL);
		}
	};

	@Override
	public void onResume() {
		super.onResume();

		if (service != null) {
			service.hideNotification();
		}

		handler.removeCallbacks(uiUpdateTask);
		handler.postDelayed(uiUpdateTask, UI_UPDATE_INTERVAL);
	}

	@Override
	public void onPause() {
		handler.removeCallbacks(uiUpdateTask);
		if (service != null) {
			if (service.isPlaying()) {
				service.showNotification();
			} else {
				service.hideNotification();
			}
		}
		super.onPause();
	}

	private long lastId = -2;

	private void updateMediaPlayerView() {
		if (service != null) {
			SongInfo songinfo = service.getSongInfo();
			if (lastId != songinfo.id) {
				artistNameText.setText(songinfo.artist);
				songNameText.setText(songinfo.title);
				albumNameText.setText(songinfo.album);
				new FetchAlbumImageTask().execute(songinfo.id);
				lastId = songinfo.id;
				setTitle(songinfo.title);
			}
			playButton.setImageResource(service.isPlaying() ? R.drawable.pause : R.drawable.play);
			shuffleButton.setImageResource(service.isShuffling() ? R.drawable.shuffle_enabled : R.drawable.shuffle_disabled);
			repeatButton.setImageResource(service.isRepeating() ? R.drawable.repeat_enabled : R.drawable.repeat_disabled);
			int duration = service.getDuration();
			songDurationText.setText(timeFormat.format((int) (duration / 60)) + ":" + timeFormat.format((int) (duration % 60)));
			int currentPosition = service.getCurrentPosition();
			if (currentPosition > duration) {
				currentPosition = duration;
			}
			songCurrentPositionText.setText(timeFormat.format((int) (currentPosition / 60)) + ":" + timeFormat.format((int) (currentPosition % 60)));
			seekbar.setMax(duration);
			seekbar.setProgress(currentPosition);
		}
	}

	private void showCurrentPlaylist() {
		String playlistName = null;
		if (service != null) {
			playlistName = service.getPlaylistName();
		}
		startActivity(Utils.createSongsIntent(this, playlistName));
	}

	private void playSongs() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(IMongolduuConstants.PLAYLIST_NAME_EXTRA_ID) && extras.containsKey(IMongolduuConstants.PLAYLIST_POSITION_EXTRA_ID)) {
				String playlistName = extras.getString(IMongolduuConstants.PLAYLIST_NAME_EXTRA_ID);
				int playlistPosition = extras.getInt(IMongolduuConstants.PLAYLIST_POSITION_EXTRA_ID);
				if (service != null) {
					Playlist playlist = null;
					if (!Utils.isAllSongPlaylist(playlistName)) {
						try {
							playlist = getHelper().getPlaylistDao().queryForId(playlistName);
						} catch (SQLException e) {
							Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
						}
					}
					List<SongInfo> songinfos = new LinkedList<SongInfo>();
					try {
						songinfos.addAll(Utils.getSongs(getHelper(), playlist));
					} catch (SQLException e) {
						Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
					}
					service.startPlaylist(playlistName, songinfos, playlistPosition);
				}
				
				getIntent().removeExtra(IMongolduuConstants.PLAYLIST_NAME_EXTRA_ID);
				getIntent().removeExtra(IMongolduuConstants.PLAYLIST_POSITION_EXTRA_ID);
			}
		}
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (item.getItemId()) {
		case R.id.action_bar_list:
			showCurrentPlaylist();
			return true;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}

	private static final int CONTEXT_MENU_HOME = 0;
	private static final int CONTEXT_MENU_PLAYLIST = 1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONTEXT_MENU_HOME, 0, R.string.context_menu_home);
		menu.add(0, CONTEXT_MENU_PLAYLIST, 0, R.string.context_menu_playlist);
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
		case CONTEXT_MENU_PLAYLIST:
			showCurrentPlaylist();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}