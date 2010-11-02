package br.com.drzoid.rightnumber;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class Quirks {
	private final static int CHILE_COUNTRY_CODE = 56;
	
	private final PhoneNumberUtil phoneNumberUtil;
	private final int countryCode;
	
	public Quirks(String currentCountry) {
		phoneNumberUtil = PhoneNumberUtil.getInstance();
		countryCode = phoneNumberUtil.getCountryCodeForRegion(currentCountry);
	}
	
	public String process(PhoneNumber phoneNumber) {
		switch (countryCode) {
		case CHILE_COUNTRY_CODE:
				return processChile(phoneNumber);
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
}
