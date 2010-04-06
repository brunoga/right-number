package br.com.drzoid.rightnumber;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * The main activity, which shows RightNumber preferences.
 */
public class RightNumberActivity extends PreferenceActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences);
  }
}