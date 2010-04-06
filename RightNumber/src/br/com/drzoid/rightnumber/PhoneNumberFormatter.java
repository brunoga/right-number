package br.com.drzoid.rightnumber;

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
  
  public String formatPhoneNumber(String originalNumber, String originalCountry, String currentCountry) {
    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

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
    if (currentCountry.equalsIgnoreCase(RightNumberConstants.BRAZIL_REGION_CODE)) {
      if (parsedOriginalNumber.getCountryCode() ==
          phoneNumberUtil.getCountryCodeForRegion(RightNumberConstants.BRAZIL_REGION_CODE)) {
        if (newNumber.getBytes()[0] == '(') {
          Log.d(RightNumberConstants.LOG_TAG, "Brazilian number with area code. Adding operator.");
          newNumber = "0" + RightNumberConstants.BRAZIL_CARRIER_CODE + newNumber;
        } else {
          Log.d(RightNumberConstants.LOG_TAG, "Brazilian number without area code. Using as is.");
        }
      } else {
        Log.d(RightNumberConstants.LOG_TAG, "International number. Calling from Brazil.");

        // +1-650-555-1234 => 0041 1-650-555-1234
        newNumber = newNumber.replace('+', ' ');
        newNumber = "00" + RightNumberConstants.BRAZIL_CARRIER_CODE + newNumber;
      }
    }
    return newNumber;
  }
}
