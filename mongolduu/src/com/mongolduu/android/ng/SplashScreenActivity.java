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

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.mongolduu.android.ng.db.DatabaseHelper;
import com.mongolduu.android.ng.misc.Utils;

/**
 * See https://github.com/cleverua/android_startup_activity
 */
public class SplashScreenActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (needStartApp()) {
        	setContentView(R.layout.splash);
        	Utils.checkStorageDirectory();
        	new Handler().postDelayed(new Runnable(){
    			public void run() {
    				Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
    				SplashScreenActivity.this.startActivity(intent);
    				SplashScreenActivity.this.finish();
    			}
        	}, IMongolduuConstants.SPLASH_DISPLAY_DURATION);
        } else {
        	finish();
        }
    }
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        // this prevents StartupActivity recreation on Configuration changes
        // (device orientation changes or hardware keyboard open/close).
        // just do nothing on these changes:
        super.onConfigurationChanged(null);
    }
    
    private boolean needStartApp() {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningTaskInfo> tasksInfo = am.getRunningTasks(1024);
        
        if (!tasksInfo.isEmpty()) {
            final String ourAppPackageName = getPackageName();
            RunningTaskInfo taskInfo;
            final int size = tasksInfo.size();
            for (int i = 0; i < size; i++) {
                taskInfo = tasksInfo.get(i);
                if (ourAppPackageName.equals(taskInfo.baseActivity.getPackageName())) {
                    // continue application start only if there is the only Activity in the task
                    // (BTW in this case this is the StartupActivity)
                    return taskInfo.numActivities == 1;
                }
            }
        }
        
        return true;
    }
}
