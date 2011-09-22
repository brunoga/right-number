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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Broadcast receiver which intercepts dialing intents and fixes the number.
 */
public class RightNumberReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    if (!preferences.getBoolean(RightNumberConstants.ENABLE_FORMATTING, true)) {
      // Formatting disabled, do nothing
      return;
    }
    
    TelephonyManager telephonyManager =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

    String currentCountry = telephonyManager.getNetworkCountryIso().toUpperCase();
    String originalCountry = telephonyManager.getSimCountryIso().toUpperCase();
    
    // Get phone number to process.
    String originalNumber = getResultData();
    if (originalNumber == null) {
    	// No previous result data. Use phone number from intent.
    	originalNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
    }

    PhoneNumberFormatter formatter = new PhoneNumberFormatter(context);
    
    boolean internationalMode = preferences.getBoolean(
        RightNumberConstants.ENABLE_INTERNATIONAL_MODE, false);
    
    int defaultAreaCode = 0;
    try {
      defaultAreaCode = Integer.parseInt(preferences.getString(
          RightNumberConstants.DEFAULT_AREA_CODE, ""));
    } catch (NumberFormatException e) {
    	// Intentionally do nothing.
    }

    String newNumber = originalNumber;
    try {
      newNumber = formatter.formatPhoneNumber(originalNumber, originalCountry, currentCountry,
      		defaultAreaCode, internationalMode);
    } catch (IllegalArgumentException e) {
      Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    StringBuilder debugData = new StringBuilder();
    debugData.append("Current Country  : ");
    debugData.append(currentCountry);
    debugData.append('\n');

    debugData.append("Original Country : ");
    debugData.append(originalCountry);
    debugData.append('\n');

    debugData.append("Original Number  : ");
    debugData.append(originalNumber);
    debugData.append('\n');

    debugData.append("New Number       : ");
    debugData.append(newNumber);
    debugData.append('\n');

    Log.d(RightNumberConstants.LOG_TAG, debugData.toString());

    setResultData(newNumber);
  }  
}
