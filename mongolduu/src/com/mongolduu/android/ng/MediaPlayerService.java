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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.mongolduu.android.ng.db.SongInfo;
import com.mongolduu.android.ng.misc.Utils;

public class MediaPlayerService extends Service implements OnPreparedListener, OnCompletionListener {
	public class LocalBinder extends Binder {
		public MediaPlayerService getService() {
			return MediaPlayerService.this;
		}
	}

	private static Method mRegisterMediaButtonEventReceiver;
	private static Method mUnregisterMediaButtonEventReceiver;
	private AudioManager mAudioManager;
	private ComponentName mRemoteControlResponder;

	static {
		initializeRemoteControlRegistrationMethods();
	}
	
	private static MediaPlayerService mInstance;

	private static final int NOTIFICATION_ID = 99999;

	private static final Class<?>[] mSetForegroundSignature = new Class[] { boolean.class };
	private static final Class<?>[] mStartForegroundSignature = new Class[] { int.class, Notification.class };
	private static final Class<?>[] mStopForegroundSignature = new Class[] { boolean.class };

	private boolean isNotificationShowed = false;
	private NotificationManager mNM;
	private Method mSetForeground;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mSetForegroundArgs = new Object[1];
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	private MediaPlayer player;
	private boolean repeat;
	private boolean shuffle;
	private String playlistName;
	private List<SongInfo> playlist = new LinkedList<SongInfo>();
	private List<SongInfo> currentPlaylist = new LinkedList<SongInfo>();
	private int currentPlaylistPosition = 0;
	private SongInfo songinfo = new SongInfo();
	private int duration = 0;

	private boolean errorDuringPlayback = false;
	
	private boolean shouldResume = false;
	private TelephonyManager mTelephonyService;
	private PhoneStateListener phoneStateListener = new PhoneStateListener() {
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				shouldResume = pause();
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (shouldResume) {
					resume();
				}
				shouldResume = false;
				break;
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder();
	}

	@Override
	public void onCreate() {
		mInstance = this;
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		try {
			mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
		} catch (NoSuchMethodException e) {
			mStartForeground = mStopForeground = null;
			return;
		}
		try {
			mSetForeground = getClass().getMethod("setForeground", mSetForegroundSignature);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
		}
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mRemoteControlResponder = new ComponentName(getPackageName(), RemoteControlReceiver.class.getName());
		registerRemoteControl();
		
		mTelephonyService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyService.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public void onDestroy() {
		if (player != null) {
			player.stop();
			player.release();
		}
		
		stopForegroundCompat();
		unregisterRemoteControl();
		mTelephonyService.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
	}
	
	public static MediaPlayerService getInstance() {
		return mInstance;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (!repeat) {
			forward();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		duration = mp.getDuration();
	}

	private void invokeMethod(Method method, Object[] args) {
		try {
			mStartForeground.invoke(this, mStartForegroundArgs);
		} catch (InvocationTargetException e) {
			// Should not happen.
			Log.w("ApiDemos", "Unable to invoke method", e);
		} catch (IllegalAccessException e) {
			// Should not happen.
			Log.w("ApiDemos", "Unable to invoke method", e);
		}
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	void startForegroundCompat(Notification notification) {
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(NOTIFICATION_ID);
			mStartForegroundArgs[1] = notification;
			invokeMethod(mStartForeground, mStartForegroundArgs);
			return;
		}

		// Fall back on the old API.
		mSetForegroundArgs[0] = Boolean.TRUE;
		invokeMethod(mSetForeground, mSetForegroundArgs);
		mNM.notify(NOTIFICATION_ID, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	void stopForegroundCompat() {
		// If we have the new stopForeground API, then use it.
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			try {
				mStopForeground.invoke(this, mStopForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w("ApiDemos", "Unable to invoke stopForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w("ApiDemos", "Unable to invoke stopForeground", e);
			}
			return;
		}

		// Fall back on the old API. Note to cancel BEFORE changing the
		// foreground state, since we could be killed at that point.
		mNM.cancel(NOTIFICATION_ID);
		mSetForegroundArgs[0] = Boolean.FALSE;
		invokeMethod(mSetForeground, mSetForegroundArgs);
	}

	public synchronized void showNotification() {
		if (songinfo != null) {
			isNotificationShowed = true;
			if (!isErrorOccuredDuringPlayback()) {
				startForegroundCompat(createNotification(songinfo.artist, songinfo.title));
			} else {
				startForegroundCompat(createNotification(getString(R.string.app_name), getString(R.string.alert_dialog_message_play_error)));
			}
		}
	}

	public synchronized void hideNotification() {
		isNotificationShowed = false;
		stopForegroundCompat();
	}

	public void startPlaylist(String playlistName, List<SongInfo> playlist, int currentPlaylistPosition) {
		this.playlistName = playlistName;
		this.playlist = new LinkedList<SongInfo>(playlist);
		this.currentPlaylist = new LinkedList<SongInfo>(playlist);
		this.currentPlaylistPosition = 0;
		if (currentPlaylistPosition >= 0 && currentPlaylistPosition < this.currentPlaylist.size()) {
			this.currentPlaylistPosition = currentPlaylistPosition;
		}
		songinfo = currentPlaylist.get(currentPlaylistPosition);
		if (shuffle) {
			Collections.shuffle(currentPlaylist);
		}
		play();
	}

	public boolean isRepeating() {
		return repeat;
	}

	public synchronized void setRepeat(boolean repeat) {
		this.repeat = repeat;
		if (player != null) {
			player.setLooping(repeat);
		}
	}

	public boolean isShuffling() {
		return shuffle;
	}

	public synchronized void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
		if (shuffle) {
			Collections.shuffle(currentPlaylist);
		} else {
			this.currentPlaylist = new LinkedList<SongInfo>(playlist);
		}
	}

	public boolean isPlaying() {
		if (player == null)
			return false;
		return player.isPlaying();
	}

	public synchronized void play() {
		if (songinfo.id != -1) {
			if (player != null) {
				player.setOnCompletionListener(null);
				player.setOnPreparedListener(null);
				player.stop();
				player.release();
				player = null;
			}

			try {
				errorDuringPlayback = false;
				duration = 0;
				player = MediaPlayer.create(this, Uri.fromFile(Utils.getSongFile(songinfo.id)));
				player.setLooping(repeat);
				player.setOnCompletionListener(this);
				player.setOnPreparedListener(this);
				player.start();
				if (isNotificationShowed) {
					showNotification();
				}
			} catch (Exception e) {
				Log.e(IMongolduuConstants.LOG_TAG, "exception", e);
				errorDuringPlayback = true;
				showNotification();
			}
		}
	}

	public synchronized boolean isErrorOccuredDuringPlayback() {
		return errorDuringPlayback;
	}

	public synchronized void resume() {
		if (player != null && !player.isPlaying()) {
			player.start();
		}
	}

	public synchronized boolean pause() {
		if (player != null && player.isPlaying()) {
			player.pause();
			return true;
		}
		return false;
	}

	public synchronized void forward() {
		currentPlaylistPosition++;
		if (currentPlaylistPosition >= 0 && currentPlaylistPosition < currentPlaylist.size()) {
			songinfo = currentPlaylist.get(currentPlaylistPosition);
			play();
		} else {
			currentPlaylistPosition = 0;
			songinfo = currentPlaylist.get(currentPlaylistPosition);
			play();
		}
	}

	public synchronized void rewind() {
		if (getCurrentPosition() > IMongolduuConstants.REWIND_REPLAY_CURRENT_POSITION) {
			play();
		} else {
			currentPlaylistPosition--;
			if (currentPlaylistPosition >= 0 && currentPlaylistPosition < currentPlaylist.size()) {
				songinfo = currentPlaylist.get(currentPlaylistPosition);
				play();
			} else {
				currentPlaylistPosition = 0;
			}
		}
	}

	public int getDuration() {
		return duration / 1000;
	}

	public synchronized int getCurrentPosition() {
		if (player == null)
			return 0;
		return player.getCurrentPosition() / 1000;
	}

	public synchronized void seekTo(int sec) {
		if (player != null) {
			player.seekTo(sec * 1000);
		}
	}
	
	public synchronized void phoneStateRinging() {
		
	}
	
	public synchronized void phoneStateIdle() {
		
	}

	public SongInfo getSongInfo() {
		return songinfo;
	}

	public int getCurrentPlaylistPosition() {
		return currentPlaylistPosition;
	}

	public String getPlaylistName() {
		return playlistName;
	}

	public Notification createNotification(String artistName, String songName) {
		Notification notification = new Notification(R.drawable.play1, artistName + " - " + songName, System.currentTimeMillis());
		PendingIntent intent = PendingIntent.getActivity(this, 0, Utils.createMediaPlayerIntent(this), 0);
		notification.setLatestEventInfo(getApplicationContext(), artistName, songName, intent);
		return notification;
	}

	private static void initializeRemoteControlRegistrationMethods() {
		try {
			if (mRegisterMediaButtonEventReceiver == null) {
				mRegisterMediaButtonEventReceiver = AudioManager.class.getMethod("registerMediaButtonEventReceiver", new Class[] { ComponentName.class });
			}
			if (mUnregisterMediaButtonEventReceiver == null) {
				mUnregisterMediaButtonEventReceiver = AudioManager.class.getMethod("unregisterMediaButtonEventReceiver", new Class[] { ComponentName.class });
			}
		} catch (NoSuchMethodException nsme) {

		}
	}

	private void registerRemoteControl() {
		try {
			if (mRegisterMediaButtonEventReceiver == null) {
				return;
			}
			mRegisterMediaButtonEventReceiver.invoke(mAudioManager, mRemoteControlResponder);
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.e(IMongolduuConstants.LOG_TAG, "exception", ie);
		}
	}

	private void unregisterRemoteControl() {
		try {
			if (mUnregisterMediaButtonEventReceiver == null) {
				return;
			}
			mUnregisterMediaButtonEventReceiver.invoke(mAudioManager, mRemoteControlResponder);
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			System.err.println("unexpected " + ie);
		}
	}
}
