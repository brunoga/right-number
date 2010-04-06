package br.com.drzoid.rightnumber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Broadcast receiver which intercepts dialing intents and fixes the number.
 */
public class RightNumberReceiver extends BroadcastReceiver {

  private static final String TAG = "RightNumberReceiver";

  @Override
  public void onReceive(Context context, Intent intent) {
    TelephonyManager telephonyManager =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

    // TODO: Get rid of all the logging
    Log.d(TAG, "Line1Number         : " + telephonyManager.getLine1Number());
    Log.d(TAG, "NetworkOperator     : " + telephonyManager.getNetworkOperator());
    Log.d(TAG, "NetworkOperatorName : " + telephonyManager.getNetworkOperatorName());
    Log.d(TAG, "SimOperator         : " + telephonyManager.getSimOperator());
    Log.d(TAG, "SimOperatorName     : " + telephonyManager.getSimOperatorName());
    Log.d(TAG, "SubscriberId        : " + telephonyManager.getSubscriberId());

    String currentCountry = telephonyManager.getNetworkCountryIso().toUpperCase();
    Log.d(TAG, "Current country  : " + currentCountry);

    String originalCountry = telephonyManager.getSimCountryIso().toUpperCase();
    Log.d(TAG, "Original country : " + originalCountry);

    boolean isRoaming = telephonyManager.isNetworkRoaming();
    Log.d(TAG, "Roaming          : " + isRoaming);

    String originalNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
    Log.d(TAG, "Original number : " + originalNumber);

    // Parses the phone number
    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    PhoneNumber parsedOriginalNumber = null;
    try {
      parsedOriginalNumber = phoneNumberUtil.parse(originalNumber, currentCountry);
    } catch (NumberParseException e) {
      Log.e(TAG, "Error parsing number : " + originalNumber);
    }

    // Formats the new number
    String newNumber = phoneNumberUtil.format(parsedOriginalNumber, PhoneNumberFormat.NATIONAL);

    // TODO: Generalize this
    if (currentCountry.equalsIgnoreCase("BR")) {
      if (parsedOriginalNumber.getCountryCode() == 55) {
        // TODO: Why '(' ? I can put an area code without (. 
        if (newNumber.getBytes()[0] == '(') {
          Log.d(TAG, "Brazilian number with area code. Adding operator.");
          newNumber = "041" + newNumber;
        } else {
          Log.d(TAG, "Brazilian number without area code. Using as is.");
        }
      } else {
        Log.d(TAG, "International number. Calling from Brazil.");
        newNumber = "0 041 " + parsedOriginalNumber.getCountryCode() + " " + newNumber;
      }
    } else {
      Log.d(TAG, "Any number. Calling from somewhere else.");
      newNumber = phoneNumberUtil.format(parsedOriginalNumber, PhoneNumberFormat.INTERNATIONAL);
    }

    Log.d(TAG, "New number      : " + newNumber);

    setResultData(newNumber);
  }
}
