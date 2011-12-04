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

public interface IMongolduuConstants {
	public static final String MONGOLDUU_WEBSITE_URL = "http://www.mongolduu.com";
	public static final String NOTIFICATION_SERVICE_MAIL = "mongolduu.notification@googlemail.com";
	public static final int SPLASH_DISPLAY_DURATION = 1000;
	
	public static final String STORAGE_DIRECTORY = "mglduu";
	
	public static final String BASE_URL = "http://192.168.178.25/testserver/";
	
	public static final String SEARCH_URL = BASE_URL + "search.php";
	public static final String SEARCH_STRING_PARAMETER_NAME = "searchstring";
	public static final String OFFSET_PARAMETER_NAME = "offset";
	public static final String SIZE_PARAMETER_NAME = "size";
	public static final int SEARCH_SIZE = 15;
	
	public static final String CHART_URL = BASE_URL + "chart.php";
	public static final String CHART_TYPE_PARAMETER_NAME = "type";
	public static final String CHART_TYPE_TODAY = "daily";
	public static final String CHART_TYPE_WEEK = "weekly";
	public static final String CHART_TYPE_ALLTIME = "alltime";
	public static final String CHART_TYPE_NEW = "newest";
	
	public static final String DOWNLOAD_URL = BASE_URL + "download.php";
	public static final String ALBUMART_URL = BASE_URL + "albumart.php";
	public static final String INFO_URL = BASE_URL + "info.php";
	
	public static final String NEWS_URL = BASE_URL + "news.php";
	public static final String TIMESTAMP_PARAMETER_NAME = "timestamp";
	public static final int NEWS_SIZE = 15;
	
	public static final String DEVICE_URL = BASE_URL + "device.php";
	public static final String ACTION_PARAMETER_NAME = "action";
	public static final String ACTION_REGISTER = "register";
	public static final String ACTION_DEREGISTER = "deregister";
	public static final String C2DMKEY_PARAMETER_NAME = "c2dmkey";
	
	public static final String SONG_ID_PARAMETER_NAME = "id";
	
	public static final int REQUIRED_EXTERNAL_STORAGE_MINIMUM_FREE_SIZE = 5 * 1024 * 1024;
	public enum StorageState { EXTERNAL_STORAGE_NOT_MOUNTED, NO_SPACE_ON_EXTERNAL_STORAGE, UNKNOWN_ERROR, SUCCESSFUL};
	
	public static final String SONG_ID_EXTRA_ID = "songId";
	public static final String PLAYLIST_NAME_EXTRA_ID = "playlistName";
	public static final String PLAYLIST_POSITION_EXTRA_ID = "playlistPosition";
	
	public static final int REWIND_REPLAY_CURRENT_POSITION = 6;
	public static final int DOUBLE_CLICK_DELAY = 500;
	
	public static final int DEVICE_REGISTER_DELAY = 30 * 1000;
	
	public static final String CLICKABLE_SONG_INFO_URL = "http://songinfo?id=";
	
	public static final String NEWS_NOTIFICATION_SETTINGS_ID = "notificationSettings";
	public static final String LANGUAGE_SETTINGS_ID = "languageSettings";
	
	public static final String LOG_TAG = "mongolduu";
	public static final String RINGTONE_FILE = "ringtone.mp3";
}
