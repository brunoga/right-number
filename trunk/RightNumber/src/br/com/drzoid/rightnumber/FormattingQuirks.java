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

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class FormattingQuirks {
	private final static int CHILE_COUNTRY_CODE = 56;
	private final static int COLOMBIA_COUNTRY_CODE = 57;
	
	private final PhoneNumberUtil phoneNumberUtil;
	private final int countryCode;
	
	public FormattingQuirks(String currentCountry) {
		phoneNumberUtil = PhoneNumberUtil.getInstance();
		countryCode = phoneNumberUtil.getCountryCodeForRegion(currentCountry);
	}
	
	public String process(PhoneNumber phoneNumber) {
		switch (countryCode) {
		case CHILE_COUNTRY_CODE:
			return processChile(phoneNumber);
		case COLOMBIA_COUNTRY_CODE:
			return processColombia(phoneNumber);			
		default:
			return "";
		}
	}
		
	private String processChile(PhoneNumber phoneNumber) {
		PhoneNumberType phoneNumberType = phoneNumberUtil.getNumberType(phoneNumber);
		int phoneNumberCountryCode = phoneNumber.getCountryCode();
		
		if (phoneNumberCountryCode != countryCode) {
			return "";
		}
		
		switch (phoneNumberType) {
		case MOBILE:
			// For mobile phones, we want to use the phone number only without anything else.
			String formattedNumber = phoneNumberUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
			return formattedNumber.substring(3);
		default:
			return "";
		}
	}
	
	private String processColombia(PhoneNumber phoneNumber) {
		PhoneNumberType phoneNumberType = phoneNumberUtil.getNumberType(phoneNumber);
		int phoneNumberCountryCode = phoneNumber.getCountryCode();
		
		if (phoneNumberCountryCode != countryCode) {
			// If making an international call, normal processing is required
			return "";
		}
				
		switch (phoneNumberType) {
		case MOBILE:
			// No need to process the number, just get the plain phone number.
			// All calls from cellphones to cellphones in colombia have no carrier code.
			return String.valueOf(phoneNumber.getNationalNumber());
		case FIXED_LINE:
			// No carrier whatsoever. Just prepend "03" from cellphones.
			return "03" + String.valueOf(phoneNumber.getNationalNumber());
		default:
			return "";
		}	
	}	
}
