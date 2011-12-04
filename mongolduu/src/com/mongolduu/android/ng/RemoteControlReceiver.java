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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {
	private static long lastClickTime;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			MediaPlayerService service = MediaPlayerService.getInstance();
			if (service != null && event != null && event.getAction() == KeyEvent.ACTION_UP) {
				switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
				case KeyEvent.KEYCODE_MEDIA_NEXT:
					service.forward();
					break;
				case KeyEvent.KEYCODE_HEADSETHOOK:
					long currentTime = System.currentTimeMillis();
					long lastTime = getAndSetLastClickTime(currentTime);
					if (currentTime - lastTime < IMongolduuConstants.DOUBLE_CLICK_DELAY) {
						service.forward();
						break;
					}
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					if (service.isPlaying()) {
						service.pause();
					} else {
						service.resume();
					}
					break;
				case KeyEvent.KEYCODE_MEDIA_STOP:
					if (service.isPlaying()) {
						service.pause();
					}
					break;
				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				case KeyEvent.KEYCODE_MEDIA_REWIND:
					service.rewind();
					break;
				default:
					break;
				}
			}
		}
	}

	private synchronized static long getAndSetLastClickTime(long currentTime) {
		long lastTime = RemoteControlReceiver.lastClickTime;
		RemoteControlReceiver.lastClickTime = currentTime;
		return lastTime;
	}
}
