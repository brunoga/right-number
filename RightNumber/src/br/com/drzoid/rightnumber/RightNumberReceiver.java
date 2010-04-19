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
    Log.d(RightNumberConstants.LOG_TAG, "Current country  : " + currentCountry);

    String originalCountry = telephonyManager.getSimCountryIso().toUpperCase();
    Log.d(RightNumberConstants.LOG_TAG, "Original country : " + originalCountry);

    String originalNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
    Log.d(RightNumberConstants.LOG_TAG, "Original number : " + originalNumber);

    PhoneNumberFormatter formatter = new PhoneNumberFormatter(context);
    String newNumber = originalNumber;
    try {
      newNumber = formatter.formatPhoneNumber(originalNumber, originalCountry, currentCountry);
    } catch (IllegalArgumentException e) {
      Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    Log.d(RightNumberConstants.LOG_TAG, "New number      : " + newNumber);

    setResultData(newNumber);
  }
}
