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
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.db.Playlist;
import com.mongolduu.android.ng.db.SongInfo;
import com.mongolduu.android.ng.misc.SongInfoListAdapter;
import com.mongolduu.android.ng.misc.Utils;

public class SongsActivity extends OrmLiteGDActivity<DatabaseHelper> {
	private String playlistName = null;
	private Playlist playlist = null;
	private boolean isAllSongsPlaylist = false;

	private TextView emptyview;
	private ListView listview;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.list);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			playlistName = extras.getString(IMongolduuConstants.PLAYLIST_NAME_EXTRA_ID);
		}
		isAllSongsPlaylist = Utils.isAllSongPlaylist(playlistName);

		if (isAllSongsPlaylist) {
			setTitle(R.string.playlist_all);
		} else {
			setTitle(playlistName);
		}

		if (!isAllSongsPlaylist) {
			addActionBarItem(Type.Add, R.id.action_bar_add);
		}
		addActionBarItem(Type.Search, R.id.action_bar_search);

		listview = (ListView) findViewById(R.id.list);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				playSong(position);
			}
		});
		listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				if (isAllSongsPlaylist) {
					menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_menu_remove_song_from_device);
				} else {
					menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_menu_remove_song_from_playlist);
				}
				menu.add(0, CONTEXT_MENU_PLAY, 0, R.string.context_menu_play_song);
				menu.add(0, CONTEXT_MENU_RINGTONE, 0, R.string.context_menu_set_as_ringtone);
			}
		});
		emptyview = (TextView) findViewById(R.id.empty);
		emptyview.setText(isAllSongsPlaylist ? R.string.playlist_all_empty : R.string.playlist_empty);

		listview.setAdapter(new SongInfoListAdapter(this, new LinkedList<SongInfo>(), false));
	}
	
	@Override
	public void onResume() {
		refreshSongs();
		
		super.onResume();
	}
	
	private void playSong(int position) {
		startActivity(Utils.createMediaPlayerIntent(SongsActivity.this, playlistName, position));
	}
	
	private void setSongAsRingtone(int position) {
		Utils.setSongAsRingtone(this, ((SongInfoListAdapter) listview.getAdapter()).getItem(position));
	}
	
	private void playSongs() {
		if(playlist.songs.size() > 0) {
			startActivity(Utils.createMediaPlayerIntent(this, playlistName, 0));
		} else {
			Toast.makeText(this, R.string.toast_message_empty_playlist, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void refreshSongs() {
		playlist = new Playlist();
		if (!isAllSongsPlaylist) {
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
		((SongInfoListAdapter) listview.getAdapter()).clear();
		((SongInfoListAdapter) listview.getAdapter()).addSongs(songinfos);
		listview.invalidateViews();
		emptyview.setVisibility(((SongInfoListAdapter) listview.getAdapter()).getCount() > 0 ? View.GONE : View.VISIBLE);
	}

	private void deleteSong(int position) {
		if (isAllSongsPlaylist) {
			try {
				SongInfo songinfo = ((SongInfoListAdapter) listview.getAdapter()).getItem(position);
				Utils.deleteSongFromDevice(getHelper(), songinfo.id);
				((SongInfoListAdapter) listview.getAdapter()).remove(songinfo);
				listview.invalidateViews();
				Toast.makeText(SongsActivity.this, R.string.toast_message_song_removed_from_device, Toast.LENGTH_SHORT).show();
				emptyview.setVisibility(((SongInfoListAdapter) listview.getAdapter()).getCount() > 0 ? View.GONE : View.VISIBLE);
			} catch (SQLException e) {
				Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
				Utils.showAlertDialog(SongsActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_database_error);
			}
		} else {
			try {
				SongInfo songinfo = ((SongInfoListAdapter) listview.getAdapter()).getItem(position);
				((SongInfoListAdapter) listview.getAdapter()).remove(songinfo);
				listview.invalidateViews();
				playlist.songs.remove(position);
				getHelper().getPlaylistDao().update(playlist);
				Toast.makeText(SongsActivity.this, R.string.toast_message_song_removed_from_playlist, Toast.LENGTH_SHORT).show();
				emptyview.setVisibility(((SongInfoListAdapter) listview.getAdapter()).getCount() > 0 ? View.GONE : View.VISIBLE);
			} catch (SQLException e) {
				Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
				Utils.showAlertDialog(SongsActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_database_error);
			}
		}
	}

	public static final int PICK_SONG_REQUEST_CODE = 1;
	
	private void addSongIntoPlaylist() {
		startActivityForResult(Utils.createPickSongIntent(this), PICK_SONG_REQUEST_CODE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Long songId = data.getExtras().getLong(IMongolduuConstants.SONG_ID_EXTRA_ID);
			try {
				SongInfo songinfo = getHelper().getSongInfoDao().queryForId(songId);
				if (songinfo != null) {
					((SongInfoListAdapter) listview.getAdapter()).add(songinfo);
					listview.invalidateViews();
					playlist.songs.add(songId);
					getHelper().getPlaylistDao().update(playlist);
					Toast.makeText(SongsActivity.this, R.string.toast_message_song_added_into_playlist, Toast.LENGTH_SHORT).show();
				}				
			} catch (SQLException e) {
				Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
				Utils.showAlertDialog(SongsActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_database_error);
			}
		}
		emptyview.setVisibility(((SongInfoListAdapter) listview.getAdapter()).getCount() > 0 ? View.GONE : View.VISIBLE);
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (item.getItemId()) {
		case R.id.action_bar_search:
			startActivity(Utils.createSearchIntent(this));
			return true;
		case R.id.action_bar_add:
			addSongIntoPlaylist();
			return true;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}

	private static final int CONTEXT_MENU_HOME = 0;
	private static final int CONTEXT_MENU_ADD = 1;
	private static final int CONTEXT_MENU_SEARCH = 2;
	private static final int CONTEXT_MENU_DELETE = 3;
	private static final int CONTEXT_MENU_PLAY = 4;
	private static final int CONTEXT_MENU_RINGTONE = 5;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONTEXT_MENU_HOME, 0, R.string.context_menu_home);
		if (!isAllSongsPlaylist) {
			menu.add(0, CONTEXT_MENU_ADD, 0, R.string.context_menu_add_song_into_playlist);
		}
		menu.add(0, CONTEXT_MENU_SEARCH, 0, R.string.context_menu_search);
		menu.add(0, CONTEXT_MENU_PLAY, 0, R.string.context_menu_play_playlist);
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
		case CONTEXT_MENU_ADD:
			addSongIntoPlaylist();
			return true;
		case CONTEXT_MENU_SEARCH:
			startActivity(Utils.createSearchIntent(this));
			return true;
		case CONTEXT_MENU_PLAY:
			playSongs();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE:
			deleteSong(menuInfo.position);
			break;
		case CONTEXT_MENU_PLAY:
			playSong(menuInfo.position);
			break;
		case CONTEXT_MENU_RINGTONE:
			setSongAsRingtone(menuInfo.position);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}
}