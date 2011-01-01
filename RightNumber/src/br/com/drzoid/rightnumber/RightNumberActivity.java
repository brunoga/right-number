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

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;

/**
 * The main activity, which shows RightNumber preferences.
 */
public class RightNumberActivity extends PreferenceActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    Preference testPreference = findPreference(RightNumberConstants.TEST_FORMATTING);
    testPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        startActivity(new Intent(RightNumberActivity.this, TestNumberActivity.class));
        return true;
      }
    });

    Preference changePreference = findPreference(RightNumberConstants.CHANGE_CONTACTS);
    changePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        new ContactChanger(RightNumberActivity.this).updateContacts();
        return true;
      }
    });
  }
  
  @Override
  public void onResume() {
  	super.onResume();

  	final PreferenceScreen carriersPreferenceScreen =
  		(PreferenceScreen) findPreference(RightNumberConstants.CARRIERS);

  	CheckBoxPreference internationalModePreference =
  		(CheckBoxPreference) findPreference(RightNumberConstants.ENABLE_INTERNATIONAL_MODE);
  	internationalModePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
  		public boolean onPreferenceClick(Preference preference) {
  			CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
  			carriersPreferenceScreen.setEnabled(!checkBoxPreference.isChecked());
  			return true;
  		}
  	});
	
  	if (internationalModePreference.isChecked()) {
  		carriersPreferenceScreen.setEnabled(false);
  	} else {
  		carriersPreferenceScreen.setEnabled(true);
  	}  	
  }
}