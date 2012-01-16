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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import android.content.Context;
import android.util.Log;

/**
 * A formatter for phone numbers.
 * All the logic for changing numbers is contained in this class.
 *
 * @author rdamazio
 */
public class PhoneNumberFormatter {

  private final Context context;
  private final PhoneNumberUtil phoneNumberUtil;
  private final CarrierCodes carrierCodes;

  public PhoneNumberFormatter(Context context) {
    this.context = context;
    this.phoneNumberUtil = PhoneNumberUtil.getInstance();
    this.carrierCodes = new CarrierCodes(context, phoneNumberUtil);
  }

  /**
   * Formats a phone number for dialing.
   *
   * @param originalNumber the number to format
   * @param originalCountry the country the phone line is from
   * @param currentCountry the country the user is currently in
   * @param defaultAreaCode the area code to use for local numbers
   * @param internationalMode {@code true} if number should be formatted in the international
   *     format
   * @return the formatted number
   * @throws IllegalArgumentException if the number is invalid
   */
  public String formatPhoneNumber(String originalNumber, String originalCountry,
  		String currentCountry, int defaultAreaCode, boolean internationalMode) {
    if (currentCountry.length() == 0) {
      // If currentCountry is empty, it means we do not have a connection to the cell tower. In
      // this case, just return the original number instead of crashing later.
      // TODO: It would be way better if we could cache the value of currentCountry and use this
      // cached value whenever we only lose signal temporarily. Unfortunately all our code is
      // currently instantiated and destroyed in a per-call basis.
      Log.e(RightNumberConstants.LOG_TAG, "Could not obtain current country.");
      return originalNumber;
    }

    // Parses the phone number
    PhoneNumber parsedOriginalNumber = internalParsePhoneNumber(originalNumber, originalCountry);
    if (parsedOriginalNumber == null) {
      return originalNumber;
    }

    if (!phoneNumberUtil.isValidNumber(parsedOriginalNumber)) {
      if (phoneNumberUtil.isPossibleNumber(parsedOriginalNumber) && defaultAreaCode != 0) {
    	// Number is a possible local number and we have a default area code. Prepend default
    	// area code and try again.
    	String numberWithAreaCode = (new StringBuilder())
    	    .append(defaultAreaCode)
    	    .append(originalNumber)
    	    .toString();

    	PhoneNumber parsedNumberWithAreaCode = internalParsePhoneNumber(
    	    numberWithAreaCode, originalCountry);
    	if (parsedNumberWithAreaCode == null) {
    	  // Should never happen, but who knows?
    	  return originalNumber;
    	}

    	if (!phoneNumberUtil.isValidNumber(parsedNumberWithAreaCode)) {
          String message = context.getString(R.string.invalid_number);
          throw new IllegalArgumentException(message);
    	} else {
    	  parsedOriginalNumber = parsedNumberWithAreaCode;
    	}
      } else {
        String message = context.getString(R.string.invalid_number);
        throw new IllegalArgumentException(message);
      }
    }

    if (internationalMode) {
      // Using international mode.
      return phoneNumberUtil.format(parsedOriginalNumber, PhoneNumberFormat.INTERNATIONAL);
    }

    // Check for quirks and apply them whenever necessary.
    Quirks quirks = new Quirks(currentCountry);
    String quirksResult = quirks.process(parsedOriginalNumber);
    if (!quirksResult.equals("")) {
      return quirksResult;
    }

    // Formats the new number.
    // The resulting format is either NATIONAL (if the number is from the current country),
    // INTERNATIONAL (if the current country is unknown) or the country-specific format
    String newNumber = phoneNumberUtil.formatOutOfCountryCallingNumber(parsedOriginalNumber,
    		currentCountry);

    // Process cases not covered by the phone number utils library.
    newNumber = carrierCodes.reformatNumberForCountry(parsedOriginalNumber, newNumber,
    		currentCountry);

    return newNumber;
  }

  /**
   * Parse a given phone number string using the given country as the default one.
   *
   * @param phoneNumber phone number represented as a string
   * @param country country code to use as default
   * @return {@code PhoneNumber} or {@code null} in case of failure
   */
  private PhoneNumber internalParsePhoneNumber(String phoneNumber, String country) {
	PhoneNumber parsedPhoneNumber = null;
	try {
	  parsedPhoneNumber = phoneNumberUtil.parse(phoneNumber, country);
	} catch (NumberParseException e) {
      Log.e(RightNumberConstants.LOG_TAG, "Error parsing number : " + phoneNumber);
	}

	return parsedPhoneNumber;
  }
}
