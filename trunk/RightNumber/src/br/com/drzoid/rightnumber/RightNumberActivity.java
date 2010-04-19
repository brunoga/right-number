package br.com.drzoid.rightnumber;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

/**
 * The main activity, which shows RightNumber preferences.
 */
public class RightNumberActivity extends PreferenceActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);

    Preference testPreference = findPreference("test_formatting");
    testPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      public boolean onPreferenceClick(Preference preference) {
        startActivity(new Intent(RightNumberActivity.this, TestNumberActivity.class));
        return true;
      }
    });
  }
}