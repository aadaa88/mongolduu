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

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mongolduu.android.ng.R;

public class TextAndProgressBarUtils {
	static class TextAndProgressBarViewHolder {
		public ProgressBar progressbar;
		public TextView textview;

		public TextAndProgressBarViewHolder(ProgressBar progressbar, TextView textview) {
			this.progressbar = progressbar;
			this.textview = textview;
		}
	}
	
	public static void initializeTextAndProgressBar(View parentview, View textandprogressbar) {
		parentview.setTag(textandprogressbar);
		TextView textview = (TextView) textandprogressbar.findViewById(R.id.text_and_progressbar_text);
		ProgressBar progressbar = (ProgressBar) textandprogressbar.findViewById(R.id.text_and_progressbar_progressbar);
		textandprogressbar.setTag(new TextAndProgressBarViewHolder(progressbar, textview));
		//progressbar.setIndeterminateDrawable(parentview.getResources().getDrawable(R.drawable.progressbar_animation));
	}
	
	public static void configureTextAndProgressBar(View parentview, String message, boolean clickable, boolean showprogressbar) {
		View textandprogressbar = (View) parentview.getTag();
		TextAndProgressBarViewHolder holder = (TextAndProgressBarViewHolder) textandprogressbar.getTag();
		TextView textview = holder.textview;
		ProgressBar progressbar = holder.progressbar;

		if (showprogressbar) {
			progressbar.setVisibility(View.VISIBLE);
			textview.setVisibility(View.GONE);
		} else {
			progressbar.setVisibility(View.GONE);
			textview.setVisibility(View.VISIBLE);
		}

		textview.setClickable(clickable);
		if (message != null) {
			textview.setText(message);
		}
	}

	public static void showTextAndProgressBar(View parentview) {
		View textandprogressbar = (View) parentview.getTag();
		textandprogressbar.setVisibility(View.VISIBLE);
	}

	public static void hideTextAndProgressBar(View parentview) {
		View textandprogressbar = (View) parentview.getTag();
		textandprogressbar.setVisibility(View.GONE);
	}
}
