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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;
import com.mongolduu.android.ng.db.SongInfo;

public class SongInfoListAdapter extends ArrayAdapter<SongInfo> {
	protected boolean showImage;

	public SongInfoListAdapter(Context context, List<SongInfo> all, boolean showImage) {
		super(context, 0, all);
		this.showImage = showImage;
	}
	
	public void addSongs(List<SongInfo> songs) {
		for (int i = 0; i < songs.size(); i++) {
			add(songs.get(i));
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View res = convertView;
		if (res == null)
			res = LayoutInflater.from(getContext()).inflate(R.layout.song_info_item, null);

		ImageView image = (ImageView) res.findViewById(R.id.image);
		TextView text = (TextView) res.findViewById(R.id.text);
		TextView subtitle = (TextView) res.findViewById(R.id.subtitle);

		SongInfo result = getItem(position);
		if (showImage) {
			image.setImageResource(result.isSavedOnDevice ? R.drawable.device : R.drawable.cloud);
		} else {
			image.setVisibility(View.GONE);
		}
		text.setText(result.title);
		subtitle.setText(result.artist);

		return res;
	}
}
