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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.db.Playlist;
import com.mongolduu.android.ng.misc.Utils;

public class PlaylistActivity extends OrmLiteGDActivity<DatabaseHelper> {
	private static final Playlist PLAYLIST_ALL = new Playlist();

	public class PlaylistListAdapter extends BaseAdapter {
		protected Context context;
		protected List<Playlist> all;

		public PlaylistListAdapter(Context context, List<Playlist> all) {
			this.context = context;
			this.all = all;
		}

		public int getCount() {
			return all.size() + 1;
		}

		public Playlist getItem(int position) {
			if (position == 0) {
				return PLAYLIST_ALL;
			}
			return all.get(position - 1);
		}

		public long getItemId(int position) {
			return position;
		}

		public boolean isEnabled(int position) {
			return true;
		}

		public List<Playlist> getPlaylists() {
			return all;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View res = convertView;
			if (res == null)
				res = LayoutInflater.from(context).inflate(R.layout.playlist_item, null);

			TextView text = (TextView) res.findViewById(R.id.text);

			Playlist result = getItem(position);
			if (result.equals(PLAYLIST_ALL)) {
				text.setText(getString(R.string.playlist_all));
			} else {
				text.setText(result.name);
			}

			return res;
		}
	}

	private ListView listview;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.list);
		setTitle(R.string.playlist_activity_title);

		addActionBarItem(Type.Add, R.id.action_bar_add);
		addActionBarItem(Type.Search, R.id.action_bar_search);

		listview = (ListView) findViewById(R.id.list);
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Playlist playlist = ((PlaylistListAdapter) listview.getAdapter()).getItem(position);
				startActivity(Utils.createSongsIntent(PlaylistActivity.this, playlist.name));
			}
		});
		listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.add(0, CONTEXT_MENU_PLAY, 0, R.string.context_menu_play_playlist);
				if (((AdapterView.AdapterContextMenuInfo) menuInfo).position > 0) {
					menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_menu_remove_playlist);
				}
			}
		});

		List<Playlist> playlists = new LinkedList<Playlist>();
		try {
			playlists = getHelper().getPlaylistDao().queryForAll();
		} catch (SQLException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		listview.setAdapter(new PlaylistListAdapter(this, playlists));
	}

	private void createNewPlaylist() {
		View contentView = LayoutInflater.from(this).inflate(R.layout.playlist_dialog, null);
		final EditText edittext = (EditText) contentView.findViewById(R.id.edittext);
		new AlertDialog.Builder(this).setTitle(R.string.playlist_dialog_title).setView(contentView).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = edittext.getText().toString().trim();
				if (Utils.isAllSongPlaylist(name)) {
					Utils.showAlertDialog(PlaylistActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_playlist_name_empty);
				} else {
					try {
						if (getHelper().getPlaylistDao().queryForId(name) == null) {
							Playlist playlist = new Playlist(name);
							getHelper().getPlaylistDao().create(playlist);
							((PlaylistListAdapter)listview.getAdapter()).getPlaylists().add(playlist);
							listview.invalidateViews();
						} else {							
							Utils.showAlertDialog(PlaylistActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_playlist_already_exists);
						}
					} catch (SQLException e) {
						Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
						Utils.showAlertDialog(PlaylistActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_database_error);
					}
				}
			}
		}).setNegativeButton(R.string.button_cancel, null).create().show();
	}
	
	private void playPlaylist(int position) {
		Playlist playlist = ((PlaylistListAdapter) listview.getAdapter()).getItem(position);
		try {
			if (!Utils.isAllSongPlaylist(playlist)) {
				getHelper().getPlaylistDao().refresh(playlist);
			}
			if(Utils.getSongs(getHelper(), playlist).size() > 0) {
				startActivity(Utils.createMediaPlayerIntent(PlaylistActivity.this, playlist.name, 0));
			} else {
				Toast.makeText(PlaylistActivity.this, R.string.toast_message_empty_playlist, Toast.LENGTH_SHORT).show();
			}
		} catch (SQLException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
			Utils.showAlertDialog(PlaylistActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_database_error);
		}
	}

	private void deletePlaylist(int position) {
		try {
			Playlist playlist = ((PlaylistListAdapter) listview.getAdapter()).getItem(position);
			getHelper().getPlaylistDao().delete(playlist);
			((PlaylistListAdapter) listview.getAdapter()).getPlaylists().remove(playlist);
			listview.invalidateViews();
		} catch (SQLException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
			Utils.showAlertDialog(PlaylistActivity.this, R.string.alert_dialog_title, R.string.alert_dialog_message_database_error);
		}
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (item.getItemId()) {
		case R.id.action_bar_search:
			startActivity(Utils.createSearchIntent(this));
			return true;
		case R.id.action_bar_add:
			createNewPlaylist();
			return true;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}

	private static final int CONTEXT_MENU_HOME = 0;
	private static final int CONTEXT_MENU_ADD = 1;
	private static final int CONTEXT_MENU_SEARCH = 2;
	private static final int CONTEXT_MENU_PLAY = 3;
	private static final int CONTEXT_MENU_DELETE = 4;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONTEXT_MENU_HOME, 0, R.string.context_menu_home);
		menu.add(0, CONTEXT_MENU_ADD, 0, R.string.context_menu_add_playlist);
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
		case CONTEXT_MENU_ADD:
			createNewPlaylist();
			return true;
		case CONTEXT_MENU_SEARCH:
			startActivity(Utils.createSearchIntent(this));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = null;
		switch (item.getItemId()) {
		case CONTEXT_MENU_PLAY:
			menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			playPlaylist(menuInfo.position);
			break;
		case CONTEXT_MENU_DELETE:
			menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			deletePlaylist(menuInfo.position);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}
}