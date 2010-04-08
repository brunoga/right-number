package br.com.drzoid.rightnumber;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
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

    if (phoneNumberUtil.isValidNumberForRegion(parsedOriginalNumber, originalCountry)) {
      // Process cases not covered by the phone number utils library
      // TODO: Generalize this - make it configurable
      if (currentCountry.equalsIgnoreCase(RightNumberConstants.BRAZIL_COUNTRY_CODE)) {
        newNumber = reformatForBrazil(parsedOriginalNumber, newNumber);
      }
    } else {
      Toast.makeText(context, context.getResources().getText(R.string.invalid_number_toast_text),
          Toast.LENGTH_SHORT).show();
      return originalNumber;
    }
    return newNumber;
  }

  /**
   * Reformats a number when dialing from Brazil.
   *
   * @param parsedOriginalNumber the original number
   * @param newNumber the pre-formatted version of the number
   * @return the proper number for dialing from Brazil
   */
  private String reformatForBrazil(PhoneNumber parsedOriginalNumber, String newNumber) {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

    // Check if Brazil special case processing is enabled
    if (!preferences.getBoolean(PreferenceKeys.SPECIAL_BR_ENABLE, true)) {
      return newNumber;
    }

    // If the number is from Brazil
    if (parsedOriginalNumber.getCountryCode() ==
        phoneNumberUtil.getCountryCodeForRegion(RightNumberConstants.BRAZIL_COUNTRY_CODE)) {
      if (phoneNumberUtil.isValidNumber(parsedOriginalNumber)) {
        // isValidNumber() only returns true if there is a valid area code.
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

  // Info from http://www.exportbureau.com/telephone_codes/international_dialcode.html
  // Common patterns: 0(national)/00(international)+carrier, 0+carrier (intl), 00+carrier (intl), carrier+00 (intl), 
  // TODO: Implement these as well
  // TODO: Check that for all other countries which only take 0/00/011 but no carrier it already gives the right number
  // TODO: Check which countris support dialing with + instead of the IDD
  // Other relevant resources:
  // http://countrycode.org/
  // http://www.timedial.net/world-time-zone-dialing-codes-from-VE.aspx
  // http://en.wikipedia.org/wiki/List_of_international_call_prefixes
  // http://en.wikipedia.org/wiki/List_of_country_calling_codes
  // http://www.howtocallabroad.com/

  private String reformatForBolivia(PhoneNumber parsedOriginalNumber, String newNumber) {
    // Same as Brazil (0/00), carriers: Entel (10), AES (11), Teledata (12), Boliviatel (13)
    return newNumber;
  }

  private String reformatForColombia(PhoneNumber parsedOriginalNumber, String newNumber) {
    // Same as Brazil (0/00), carriers: Orbitel (5), ETB (7), Telecom (9)
    // Exceptions: 03 (long distance to mobiles), #555 (intl Bellsouth), #999 (intl Comcel)
    return newNumber;
  }

  private String reformatForFrance(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 00 (France Telecom), 40 (Tele 2), 50 (Omnicom), 70 (Le 7 Cegetel), 90 (9 Telecom) - intl only
    return newNumber;
  }

  private String reformatForElSalvador(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 00 (default), 14400 (Telefonica) - intl only
    return newNumber;
  }

  private String reformatForGuatemala(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 00 (default), 130+00 (Telefonica), 147-00 (Telgua) - intl only
    return newNumber;
  }

  private String reformatForHongKong(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 001 (PCCW), 0080 (Hutchinson), 009 (New world) - intl only
    return newNumber;
  }

  private String reformatForIsrael(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 00 (default), 012 (Golden Lines), 013 (Barak LTD), 014 (Bezeq LTD) - intl only
    return newNumber;
  }

  private String reformatForJapan(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 001 (KDD), 010 (MyLine), 0061 (Cable IDC), 0041 (Japan Telecom) - intl only
    return newNumber;
  }

  private String reformatForPanama(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 00 (Cable & Wireless), 088+00 (Telecarrier), 055+00 (Clarocom) - intl only
    return newNumber;
  }

  private String reformatForPortugal(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 00 (default), 882 (Rubicon) - intl only
    // Madera and Azores - also no code
    return newNumber;
  }

  private String reformatForSingapore(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 001 (Singtel), 002 (MobileOne), 008 (Starhub), 012 (Singtel Faxplus), 013 (Singtel Budgetcall), 018 (Starhub I-Call), 019 (Singtel V019)
    return newNumber;
  }

  private String reformatForVenezuela(PhoneNumber parsedOriginalNumber, String newNumber) {
    // 00 (default), 01-02 (Etelix), 01-07 (multiphone.net.ve), 01-10 (CANTV), 01-11 (Convergence),
    // 01-14 (Telcel), 01-19 (Totalcom), 01-23 (Orbitel/Entel), 01-50 (LD Telecom), 01-33 (NGTV), 01-99 (Veninfotel)
    // Intl - same + 0 at end
    return newNumber;
  }
}
