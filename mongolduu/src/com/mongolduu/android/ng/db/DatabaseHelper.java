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

package com.mongolduu.android.ng.db;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static final String DATABASE_NAME = "mongolduu.db";
	private static final int DATABASE_VERSION = 3;

	private Dao<SongInfo, Long> songinfoDao = null;
	private Dao<Playlist, String> playlistDao = null;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, SongInfo.class);
			TableUtils.createTable(connectionSource, Playlist.class);
			
			Log.i(DatabaseHelper.class.getName(), "created new entries in onCreate: ");
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, SongInfo.class, true);
			TableUtils.dropTable(connectionSource, Playlist.class, true);
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	
	public Dao<SongInfo, Long> getSongInfoDao() throws SQLException {
		if (songinfoDao == null) {
			songinfoDao = getDao(SongInfo.class);
		}
		return songinfoDao;
	}
	
	public Dao<Playlist, String> getPlaylistDao() throws SQLException {
		if (playlistDao == null) {
			playlistDao = getDao(Playlist.class);
		}
		return playlistDao;
	}

	@Override
	public void close() {
		super.close();
		songinfoDao = null;
		playlistDao = null;
	}
}
