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

public class News {
	public static final String TIMESTAMP_KEY = "timestamp";
	public static final String CURRENT_TIMESTAMP_KEY = "current_timestamp";
	public static final String TEXT_KEY = "text";
	
	public long timestamp;
	public long currentTimestamp;
	public String text;
	
	public News() {
		timestamp = -1;
		text = "Unknown";
	}
	
	public News(long timestamp, long currentTimestamp, String text) {
		this.timestamp = timestamp;
		this.currentTimestamp = currentTimestamp;
		this.text = text;
	}
}
