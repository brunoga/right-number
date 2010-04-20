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
    String originalNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

    PhoneNumberFormatter formatter = new PhoneNumberFormatter(context);
    String newNumber = originalNumber;
    try {
      newNumber = formatter.formatPhoneNumber(originalNumber, originalCountry, currentCountry);
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
