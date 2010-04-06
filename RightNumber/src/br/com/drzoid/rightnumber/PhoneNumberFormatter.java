package br.com.drzoid.rightnumber;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * A formatter for phone numbers.
 * All the logic for changing numbers is contained in this class.
 *
 * @author rdamazio
 */
public class PhoneNumberFormatter {
  
  private final Context context;
  private final PhoneNumberUtil phoneNumberUtil;

  public PhoneNumberFormatter(Context context) {
    this.context = context;
    this.phoneNumberUtil = PhoneNumberUtil.getInstance();
  }

  public String formatPhoneNumber(String originalNumber, String originalCountry, String currentCountry) {

    // Parses the phone number
    PhoneNumber parsedOriginalNumber = null;
    try {
      // Parse the number assuming it's from the phone's original country
      parsedOriginalNumber = phoneNumberUtil.parse(originalNumber, originalCountry);
    } catch (NumberParseException e) {
      Log.e(RightNumberConstants.LOG_TAG, "Error parsing number : " + originalNumber);
      return originalNumber;
    }

    // Formats the new number.
    // The resulting format is either NATIONAL (if the number if from the current country),
    // INTERNATIONAL (if the current country is unknown) or the country-specific format
    String newNumber = phoneNumberUtil.formatOutOfCountryCallingNumber(parsedOriginalNumber, currentCountry);

    // Process cases not covered by the phone number utils library
    // TODO: Generalize this - make it configurable
    if (currentCountry.equalsIgnoreCase(RightNumberConstants.BRAZIL_COUNTRY_CODE)) {
      newNumber = reformatForBrazil(parsedOriginalNumber, newNumber);
    }
    return newNumber;
  }

  private String reformatForBrazil(PhoneNumber parsedOriginalNumber, String newNumber) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

    // Check if Brazil special case processing is enabled
    if (!preferences.getBoolean(PreferenceKeys.SPECIAL_BR_ENABLE, true)) {
      return newNumber;
    }

    // If the number is from Brazil
    if (parsedOriginalNumber.getCountryCode() ==
        phoneNumberUtil.getCountryCodeForRegion(RightNumberConstants.BRAZIL_COUNTRY_CODE)) {
      if (newNumber.getBytes()[0] == '(') {
        Log.d(RightNumberConstants.LOG_TAG, "Brazilian number with area code. Adding operator.");

        String carrierCode = preferences.getString(
            PreferenceKeys.SPECIAL_BR_CARRIER_CODE,
            RightNumberConstants.BRAZIL_DEFAULT_CARRIER);
        newNumber = RightNumberConstants.BRAZIL_NATIONAL_PREFIX + carrierCode + newNumber;
      } else {
        Log.d(RightNumberConstants.LOG_TAG, "Brazilian number without area code. Using as is.");
      }
    } else {
      Log.d(RightNumberConstants.LOG_TAG, "International number. Calling from Brazil.");

      // +1-650-555-1234 => 0041 1-650-555-1234
      String carrierCode = preferences.getString(
          PreferenceKeys.SPECIAL_BR_INTERNATIONAL_CARRIER_CODE,
          RightNumberConstants.BRAZIL_DEFAULT_CARRIER);
      newNumber = newNumber.replace('+', ' ');
      newNumber = RightNumberConstants.BRAZIL_INTERNATIONAL_PREFIX + carrierCode + newNumber;
    }
    return newNumber;
  }
}
