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

import com.j256.ormlite.field.DatabaseField;

public class SongInfo {
	public static final String ID_KEY = "id";
	public static final String ARTIST_KEY = "artist";
	public static final String ALBUM_KEY = "album";
	public static final String TITLE_KEY = "title";
	public static final String GENRE_KEY = "genre";
	
	@DatabaseField(id = true)
	public long id;
	@DatabaseField(canBeNull = false)
	public String artist;
	@DatabaseField(canBeNull = false)
	public String title;
	@DatabaseField(canBeNull = false)
	public String album;
	@DatabaseField(canBeNull = false)
	public String genre;
	
	public boolean isSavedOnDevice = false;
	
	public SongInfo() {
		id = -1;
		artist = "Unknown";
		album = "Unknown";
		title = "Unknown";
		genre = "Unknown";
	}
	
	public String toString() {
		return title + " " + artist;
	}
}
