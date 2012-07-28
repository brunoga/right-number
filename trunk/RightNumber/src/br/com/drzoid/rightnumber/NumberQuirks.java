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

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.widget.Toast;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class NumberQuirks {
	private final static int BRAZIL_COUNTRY_CODE = 55;
	
	private static final String SAO_PAULO_AREA_11 = "11";
	
	private final PhoneNumberUtil phoneNumberUtil;
	private final Context context;
	
	private final boolean dialing;
	
	public NumberQuirks(Context context, boolean dialing) {
		this.phoneNumberUtil = PhoneNumberUtil.getInstance();
		this.context = context;
		this.dialing = dialing;
	}
	
	public PhoneNumber process(PhoneNumber phoneNumber) {
		PhoneNumberType phoneNumberType = phoneNumberUtil.getNumberType(phoneNumber);

		int phoneNumberCountryCode = phoneNumber.getCountryCode();
		if (phoneNumberCountryCode != BRAZIL_COUNTRY_CODE) {
			return phoneNumber;
		}
		
		String areaCode = getAreaCode(phoneNumber);	
		if (!areaCode.equals(SAO_PAULO_AREA_11)) {
			return phoneNumber;
		}

		String subscriberNumber = getSubscriberNumber(phoneNumber);
		if (subscriberNumber.length() != 8) {
			return phoneNumber;
		}
		
		// The extra 9 digit will be added after July, 29th, 2012.
		TimeZone timeZone = TimeZone.getTimeZone("GMT-03:00");
		Calendar currentCalendar = Calendar.getInstance(timeZone);
		
		Calendar targetCalendar = Calendar.getInstance(timeZone);
		targetCalendar.set(2012, 7, 28, 23, 59, 59);
		
		if (!currentCalendar.after(targetCalendar)) {
			// Not there yet.
			return phoneNumber;
		}
		
		switch (phoneNumberType) {
		case MOBILE:
			// Add a leading 9 to mobile phone numbers in São Paulo with area code 11.
			phoneNumber.setNationalNumber(Long.parseLong(areaCode + "9" + subscriberNumber));
			break;
		case FIXED_LINE_OR_MOBILE:
			if (dialing) {
				// Show a warning.
				Toast.makeText(context, context.getString(R.string.ambiguous_mobile_number), Toast.LENGTH_SHORT).show();
			}
		}
		
		return phoneNumber;
	}
	
	private String getAreaCode(PhoneNumber phoneNumber) {		
		int areaCodeLength = phoneNumberUtil.getLengthOfGeographicalAreaCode(phoneNumber);

		if (areaCodeLength == 0) {
			// Temporary hack. It seems that, for cell phone numbers, an area code length of 0 is
			// being returned. 2 is the area code size for Brazil, which is what we are dealing
			// with here anyway.
			areaCodeLength = 2;
		}

		if (areaCodeLength > 0) {
			String nationalSignificantNumber =
					phoneNumberUtil.getNationalSignificantNumber(phoneNumber);
			return nationalSignificantNumber.substring(0, areaCodeLength);
		}
		
		return "";
	}
	
	private String getSubscriberNumber(PhoneNumber phoneNumber) {		
		String nationalSignificantNumber =
				phoneNumberUtil.getNationalSignificantNumber(phoneNumber);

		int areaCodeLength = phoneNumberUtil.getLengthOfGeographicalAreaCode(phoneNumber);

		if (areaCodeLength == 0) {
			// Temporary hack. It seems that, for cell phone numbers, an area code length of 0 is
			// being returned. 2 is the area code size for Brazil, which is what we are dealing
			// with here anyway.
			areaCodeLength = 2;
		}
		
		if (areaCodeLength > 0) {
			return nationalSignificantNumber.substring(areaCodeLength);
		}
		
		return nationalSignificantNumber;
	}
}
