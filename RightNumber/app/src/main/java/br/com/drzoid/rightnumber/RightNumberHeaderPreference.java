/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package br.com.drzoid.rightnumber;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RightNumberHeaderPreference extends Preference {
	private static final String RIGHT_NUMBER_URL =
		"https://sites.google.com/a/bug-br.org.br/android/software/right-number";
	
	public RightNumberHeaderPreference(Context context) {
		super(context);
	}

	public RightNumberHeaderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RightNumberHeaderPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	
	/*
	 * Programatically creates a layout that will be used as a header for the
	 * preferences panel.
	 * 
	 * @see android.preference.Preference#onCreateView(android.view.ViewGroup)
	 */
	@Override
	protected View onCreateView(ViewGroup parent) {
		LinearLayout linearLayout = new LinearLayout(getContext());
		
		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		
		linearLayout.setLayoutParams(layoutParams);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams itemLayoutParams = new  LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		itemLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
		
		ImageView imageView = new ImageView(getContext());
		imageView.setPadding(0, 10, 0, 10);
				
		imageView.setLayoutParams(itemLayoutParams);
		imageView.setImageResource(R.drawable.icon);
		
		TextView textView = new TextView(getContext());

		textView.setLayoutParams(itemLayoutParams);

		String applicationVersion = null;
		
		PackageManager manager = getContext().getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(getContext().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		if (info != null) {
			applicationVersion = info.versionName;
		}
		
		String versionText = getContext().getResources().getString(R.string.app_name);
		if (applicationVersion != null) {
			versionText += " v";
			versionText += applicationVersion;
		}
				
		textView.setText(versionText);
		
		linearLayout.addView(imageView);
		linearLayout.addView(textView);
		
		linearLayout.setId(android.R.id.widget_frame);
		
		return linearLayout;
	}
	
	@Override
	protected void onClick() {		
		Uri uri = Uri.parse(RIGHT_NUMBER_URL);
		getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}
}
