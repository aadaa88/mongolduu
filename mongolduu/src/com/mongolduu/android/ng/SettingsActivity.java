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

import java.util.Locale;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.misc.Utils;

public class SettingsActivity extends PreferenceActivity {
	private Locale locale = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		Configuration config = getResources().getConfiguration();
		String lang = settings.getString(IMongolduuConstants.LANGUAGE_SETTINGS_ID, "en");
		if (!"".equals(lang)) {
			locale = new Locale(lang);
			Locale.setDefault(locale);
			config.locale = locale;
			getResources().updateConfiguration(config, null);
		}
		
		addPreferencesFromResource(R.xml.settings);
		findPreference(IMongolduuConstants.LANGUAGE_SETTINGS_ID).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object language) {
				startActivity(Utils.createHomeIntent(SettingsActivity.this));
				return true;
			}
		});
		findPreference(IMongolduuConstants.NEWS_NOTIFICATION_SETTINGS_ID).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object language) {
				Utils.registerDevice(getApplicationContext(), true);
				return true;
			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (locale != null) {
			newConfig.locale = locale;
			Locale.setDefault(locale);
			getResources().updateConfiguration(newConfig, null);
		}
		super.onConfigurationChanged(newConfig);
	}
	
	private static final int CONTEXT_MENU_HOME = 0;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, CONTEXT_MENU_HOME, 0, R.string.context_menu_home);
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
}