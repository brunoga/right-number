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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Activity which allows the user to test how a number would be formatted.
 *
 * @author rdamazio
 */
public class TestNumberActivity extends Activity {

  private PhoneNumberFormatter formatter;
  private Spinner dialingFrom;
  private Spinner lineFrom;
  private EditText inputText;
  private TextView outputText;
  private String[] countryCodes;
  private TextView errorView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.test_number);

    formatter = new PhoneNumberFormatter(this);
    countryCodes = getResources().getStringArray(R.array.country_codes);

    // Find relevant widgets
    dialingFrom = (Spinner) findViewById(R.id.dialing_from);
    lineFrom = (Spinner) findViewById(R.id.line_from);
    inputText = (EditText) findViewById(R.id.test_number_input);
    outputText = (TextView) findViewById(R.id.test_formatted_number);
    errorView = (TextView) findViewById(R.id.test_formatting_error);

    // Set default countries to the current ones
    TelephonyManager telephonyManager =
        (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    String originalCountry = telephonyManager.getSimCountryIso().toUpperCase();
    String currentCountry = telephonyManager.getNetworkCountryIso().toUpperCase();
    int originalCountryIndex = findCountry(originalCountry, countryCodes);
    int currentCountryIndex = findCountry(currentCountry, countryCodes);
    if (originalCountryIndex != -1) {
      lineFrom.setSelection(originalCountryIndex);
    }
    if (currentCountryIndex != -1) {
      dialingFrom.setSelection(currentCountryIndex);
    }

    // Attach change listeners to update the formatted number
    inputText.addTextChangedListener(new TextWatcher() {
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Do nothing
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Do nothing
      }

      public void afterTextChanged(Editable s) {
        updateFormattedNumber();
      }
    });

    OnItemSelectedListener spinnerListener = new Spinner.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        updateFormattedNumber();
      }

      public void onNothingSelected(AdapterView<?> arg0) {
        // Do nothing
      }
    };
    dialingFrom.setOnItemSelectedListener(spinnerListener);
    lineFrom.setOnItemSelectedListener(spinnerListener);
  }

  /**
   * Finds the index of a country code in an array of country codes.
   *
   * @param country the country code to find
   * @param countryCodes the complete (unsorted) list of country codes
   * @return the index where it was found, or -1 if not found
   */
  private int findCountry(String country, String[] countryCodes) {
    for (int i = 0; i < countryCodes.length; i++) {
      String countryCode = countryCodes[i];
      if (countryCode.equalsIgnoreCase(country)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Updates the formatted number displayed ot the user based on the current
   * inputs.
   */
  private void updateFormattedNumber() {
    errorView.setVisibility(View.GONE);

    String originalNumber = inputText.getText().toString();
    if (originalNumber.length() == 0) {
      outputText.setText(R.string.test_formatted_blank_number);
      return;
    }

    int fromCountryIndex = dialingFrom.getSelectedItemPosition();
    int lineCountryIndex = lineFrom.getSelectedItemPosition();
    String fromCountryCode = countryCodes[fromCountryIndex];
    String lineCountryCode = countryCodes[lineCountryIndex];

    Log.d(RightNumberConstants.LOG_TAG,
        "Number:" + originalNumber + "; from=" + fromCountryCode + "; line=" + lineCountryCode);

    String newNumber = originalNumber;
    try {
      newNumber = formatter.formatPhoneNumber(originalNumber, lineCountryCode, fromCountryCode);
    } catch (IllegalArgumentException e) {
      errorView.setText(e.getMessage());
      errorView.setVisibility(View.VISIBLE);
    }
    outputText.setText(newNumber);
  }
}
