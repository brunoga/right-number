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

import android.os.Build;

public class RightNumberConstants {
  // Global constants
  public static final String LOG_TAG = "RightNumber";
  public static final String RES_PACKAGE = "br.com.drzoid.rightnumber";
  public static final int ANDROID_API_LEVEL = Integer.parseInt(Build.VERSION.SDK);

  // Preference keys
  public static final String ENABLE_FORMATTING = "enable_formatting";
  public static final String ENABLE_INTERNATIONAL_MODE = "enable_international_mode";
  public static final String DEFAULT_AREA_CODE = "default_area_code";  
  public static final String CARRIERS = "carriers";  
  public static final String TEST_FORMATTING = "test_formatting";
  public static final String CHANGE_CONTACTS = "change_contacts";
  
  public static final String ENABLE_CARRIER_BASE_KEY = "carrier_enable_";

  private RightNumberConstants() { }
}
