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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mongolduu.android.ng.IMongolduuConstants;
import com.mongolduu.android.ng.db.News;
import com.mongolduu.android.ng.db.SongInfo;

public class HttpConnector {

	public static String httpGET(String url) throws ClientProtocolException, IOException {
		HttpClient httpClient = createHttpClient();
		HttpResponse response = httpClient.execute(new HttpGet(url));
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new IOException("HTTP client error: " + response.getStatusLine().getStatusCode());
		}
		return processEntity(response.getEntity());
	}

	public static String httpPOST(String url, List<NameValuePair> nameValuePairs) throws ClientProtocolException, IOException {
		HttpClient httpClient = createHttpClient();
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse response = httpClient.execute(httpPost);
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new IOException("HTTP client error: " + response.getStatusLine().getStatusCode());
		}
		return processEntity(response.getEntity());
	}

	public static String httpPUT(String url, String data) throws ClientProtocolException, IOException {
		HttpClient httpClient = createHttpClient();
		HttpPut httpPut = new HttpPut(url);
		httpPut.setEntity(new StringEntity(data));
		HttpResponse response = httpClient.execute(httpPut);
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new IOException("HTTP client error: " + response.getStatusLine().getStatusCode());
		}
		return processEntity(response.getEntity());
	}

	public static String httpDELETE(String url) throws ClientProtocolException, IOException {
		HttpClient httpClient = createHttpClient();
		HttpResponse response = httpClient.execute(new HttpDelete(url));
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new IOException("HTTP client error: " + response.getStatusLine().getStatusCode());
		}
		return processEntity(response.getEntity());
	}

	public static Bitmap downloadBitmap(String url) throws ClientProtocolException, IOException {
		HttpClient httpClient = createHttpClient();
		HttpResponse response = httpClient.execute(new HttpGet(url));
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new IOException("HTTP client error: " + response.getStatusLine().getStatusCode());
		}
		return processBitmapEntity(response.getEntity());
	}

	public static byte[] downloadResourceAsByteArray(String url) throws ClientProtocolException, IOException {
		HttpClient httpClient = createHttpClient();
		HttpResponse response = httpClient.execute(new HttpGet(url));
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new IOException("HTTP client error: " + response.getStatusLine().getStatusCode());
		}
		return processByteArrayEntity(response.getEntity());
	}

	private static HttpClient createHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.setRedirectHandler(new RedirectHandler());
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), 25000);
		return httpClient;
	}

	private static String processEntity(HttpEntity entity) throws IllegalStateException, IOException {
		String result = Utils.copyStreamToString(entity.getContent());
		entity.consumeContent();
		return result;
	}

	private static Bitmap processBitmapEntity(HttpEntity entity) throws IOException {
		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
		Bitmap bm = BitmapFactory.decodeStream(bufHttpEntity.getContent());
		bufHttpEntity.consumeContent();
		return bm;
	}

	private static byte[] processByteArrayEntity(HttpEntity entity) throws IOException {
		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
		byte[] buffer = Utils.copyStreamToByteArray(bufHttpEntity.getContent());
		bufHttpEntity.consumeContent();
		return buffer;
	}

	public static Bitmap fetchImage(String url) {
		if (url == null || "".equals(url.trim()))
			return null;
		Bitmap image = null;
		try {
			image = downloadBitmap(url);
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return image;
	}

	public static byte[] fetchByteArray(String url) {
		if (url == null || "".equals(url.trim()) || url.length() < 5)
			return null;
		byte[] buffer = null;
		try {
			buffer = downloadResourceAsByteArray(url);
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		if (buffer != null && buffer.length == 0) {
			buffer = null;
		}
		return buffer;
	}

	protected static String getString(JSONObject json, String key) {
		String value = "";
		try {
			value = json.getString(key);
		} catch (JSONException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return value;
	}

	protected static int getInt(JSONObject json, String key) {
		int value = -1;
		try {
			value = json.getInt(key);
		} catch (JSONException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return value;
	}

	protected static long getLong(JSONObject json, String key) {
		long value = -1;
		try {
			value = json.getLong(key);
		} catch (JSONException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return value;
	}

	protected static SongInfo parseSongInfo(JSONObject jsonobject) {
		SongInfo info = new SongInfo();
		info.id = getLong(jsonobject, SongInfo.ID_KEY);
		info.artist = getString(jsonobject, SongInfo.ARTIST_KEY);
		info.album = getString(jsonobject, SongInfo.ALBUM_KEY);
		info.title = getString(jsonobject, SongInfo.TITLE_KEY);
		info.genre = getString(jsonobject, SongInfo.GENRE_KEY);
		return info;
	}

	protected static List<SongInfo> parseSongInfoList(JSONArray jsonarray) {
		ArrayList<SongInfo> searchResults = new ArrayList<SongInfo>(100);
		for (int i = 0; i < jsonarray.length(); i++) {
			try {
				JSONObject jsonobject = jsonarray.getJSONObject(i);
				if (jsonobject != null) {
					searchResults.add(parseSongInfo(jsonobject));
				}
			} catch (JSONException e) {
				Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
			}
		}
		return searchResults;
	}

	public static List<SongInfo> searchArtistOrSong(long songId, String text, int offset, int size) {
		List<SongInfo> searchResults = null;
		try {
			String content = "";
			if (songId > -1) {
				content = httpGET(IMongolduuConstants.SEARCH_URL + "?" + IMongolduuConstants.SONG_ID_PARAMETER_NAME + "=" + songId);
			} else {
				content = httpGET(IMongolduuConstants.SEARCH_URL + "?" + IMongolduuConstants.SEARCH_STRING_PARAMETER_NAME + "=" + URLEncoder.encode(text, "UTF-8") + "&" + IMongolduuConstants.OFFSET_PARAMETER_NAME + "=" + offset + "&" + IMongolduuConstants.SIZE_PARAMETER_NAME + "=" + size);
			}
			JSONArray jsonarray = new JSONArray(content);
			if (jsonarray != null) {
				searchResults = parseSongInfoList(jsonarray);
			}
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return searchResults;
	}

	public static News parseNews(JSONObject jsonobject) {
		News news = null;
		try {
			long timestamp = getLong(jsonobject, News.TIMESTAMP_KEY);
			long currentTimestamp = getLong(jsonobject, News.CURRENT_TIMESTAMP_KEY);
			String text = getString(jsonobject, News.TEXT_KEY);
			news = new News(timestamp, currentTimestamp, text);
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return news;
	}

	public static List<News> fetchNews(long timestamp, int size) {
		List<News> news = null;
		try {
			String content = httpGET(IMongolduuConstants.NEWS_URL + "?" + (timestamp != -1 ? (IMongolduuConstants.TIMESTAMP_PARAMETER_NAME + "=" + timestamp + "&") : "") + IMongolduuConstants.SIZE_PARAMETER_NAME + "=" + size);
			JSONArray jsonarray = new JSONArray(content);
			if (jsonarray != null) {
				news = new ArrayList<News>(100);
				for (int i = 0; i < jsonarray.length(); i++) {
					try {
						JSONObject jsonobject = jsonarray.getJSONObject(i);
						if (jsonobject != null) {
							News temp = parseNews(jsonobject);
							if (temp != null) {
								news.add(temp);
							}
						}
					} catch (JSONException e) {
						Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
					}
				}
			}
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return news;
	}

	public static List<SongInfo> fetchChartList(String chartType) {
		List<SongInfo> searchResults = null;
		try {
			String content = httpGET(IMongolduuConstants.CHART_URL + "?" + IMongolduuConstants.CHART_TYPE_PARAMETER_NAME + "=" + URLEncoder.encode(chartType, "UTF-8"));
			JSONArray jsonarray = new JSONArray(content);
			if (jsonarray != null) {
				searchResults = parseSongInfoList(jsonarray);
			}
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return searchResults;
	}

	public static boolean downloadSong(long id, DownloadSongTask task) {
		try {
			File file = Utils.getSongFile(id);
			if (file.exists()) {
				file.delete();
			}

			URL downloadURL = new URL(IMongolduuConstants.DOWNLOAD_URL + "?" + IMongolduuConstants.SONG_ID_PARAMETER_NAME + "=" + id);

			HttpClient httpClient = createHttpClient();
			HttpResponse httpResponse = httpClient.execute(new HttpGet(downloadURL.toURI()));
			HttpEntity httpEntity = httpResponse.getEntity();
			long length = 0;
			try {
				length = Long.parseLong(httpResponse.getHeaders("Content-Length")[0].getValue());
			} catch (NumberFormatException e) {
				Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
			}

			InputStream input = new BufferedInputStream(httpEntity.getContent());
			OutputStream output = new FileOutputStream(file);
			byte buffer[] = new byte[1024];

			int count = 0;
			int total = 0;
			while ((count = input.read(buffer)) != -1 && !task.isCancelled()) {
				total += count;
				task.publishProgressFromOtherProcess((int) (total * 100 / length));
				output.write(buffer, 0, count);
			}
			output.flush();
			output.close();
			input.close();
			httpEntity.consumeContent();
			return true;
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return false;
	}

	public static Bitmap downloadAlbumImage(long id) {
		try {
			URL downloadURL = new URL(IMongolduuConstants.ALBUMART_URL + "?" + IMongolduuConstants.SONG_ID_PARAMETER_NAME + "=" + id);
			return fetchImage(downloadURL.toString());
		} catch (MalformedURLException e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
		return null;
	}

	public static void registerDevice(String c2dmkey) {
		try {
			httpGET(IMongolduuConstants.DEVICE_URL + "?" + IMongolduuConstants.ACTION_PARAMETER_NAME + "=" + IMongolduuConstants.ACTION_REGISTER + "&" + IMongolduuConstants.C2DMKEY_PARAMETER_NAME + "=" + c2dmkey);
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
	}
	
	public static void deregisterDevice(String c2dmkey) {
		try {
			httpGET(IMongolduuConstants.DEVICE_URL + "?" + IMongolduuConstants.ACTION_PARAMETER_NAME + "=" + IMongolduuConstants.ACTION_DEREGISTER + "&" + IMongolduuConstants.C2DMKEY_PARAMETER_NAME + "=" + c2dmkey);
		} catch (Exception e) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
		}
	}
}
