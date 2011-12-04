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

import java.sql.SQLException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.db.SongInfo;
import com.mongolduu.android.ng.misc.Utils;

public abstract class AbstractSearchAndChartActivity extends OrmLiteGDActivity<DatabaseHelper> {
	protected ListView listview;
	protected View textandprogressbar = null;
	
	protected void initializeListViewListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SongInfo songinfo = (SongInfo) parent.getAdapter().getItem(position);
				if (songinfo.isSavedOnDevice) {
					//Toast.makeText(SearchActivity.this, R.string.toast_message_already_downloaded, Toast.LENGTH_SHORT).show();
					playSong(songinfo);
				} else {
					downloadSong(songinfo);
				}
			}
		});
		listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				SongInfo songinfo = (SongInfo) listview.getAdapter().getItem(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
				if (songinfo.isSavedOnDevice) {
					menu.add(0, CONTEXT_MENU_PLAY, 0, R.string.context_menu_play_song);
					menu.add(0, CONTEXT_MENU_RINGTONE, 0, R.string.context_menu_set_as_ringtone);
					menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_menu_remove_song_from_device);
				} else {
					menu.add(0, CONTEXT_MENU_DOWNLOAD, 0, R.string.context_menu_download);
				}
			}
		});
	}
	
	protected void downloadSong(final SongInfo songinfo) {
    	new AlertDialog.Builder(this).setTitle(R.string.download_dialog_title).setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Utils.downloadSong(AbstractSearchAndChartActivity.this, listview, songinfo);
			}
		}).setNegativeButton(R.string.button_cancel, null).create().show();
    }
	
	protected void deleteSong(SongInfo songinfo) {
    	try {
			Utils.deleteSongFromDevice(getHelper(), songinfo.id);
			songinfo.isSavedOnDevice = false;
			listview.invalidateViews();
			Toast.makeText(this, R.string.toast_message_song_removed_from_device, Toast.LENGTH_SHORT).show();
		} catch (SQLException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
			Utils.showAlertDialog(this, R.string.alert_dialog_title, R.string.alert_dialog_message_database_error);
		}
    }
	
	protected void playSong(SongInfo songinfo) {
    	Utils.playSongInAllSongsPlaylist(this, getHelper(), songinfo);
    }
    
	protected void setSongAsRingtone(SongInfo songinfo) {
		Utils.setSongAsRingtone(this, songinfo);
	}
	
	public void onTextAndProgressBarClick(View view) {
		
	}
	
	protected static final int CONTEXT_MENU_HOME = 0;
	protected static final int CONTEXT_MENU_REFRESH = 1;
	protected static final int CONTEXT_MENU_SEARCH = 2;
	protected static final int CONTEXT_MENU_DELETE = 3;
	protected static final int CONTEXT_MENU_DOWNLOAD = 4;
	protected static final int CONTEXT_MENU_PLAY = 5;
	protected static final int CONTEXT_MENU_RINGTONE = 6;
	
	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (item.getItemId()) {		
		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONTEXT_MENU_HOME, 0, R.string.context_menu_home);		
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		SongInfo songinfo = (SongInfo) listview.getAdapter().getItem(menuInfo.position);
		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE:
			deleteSong(songinfo);
			break;
		case CONTEXT_MENU_DOWNLOAD:
			downloadSong(songinfo);
			break;
		case CONTEXT_MENU_PLAY:
			playSong(songinfo);
			break;
		case CONTEXT_MENU_RINGTONE:
			setSongAsRingtone(songinfo);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}
}
