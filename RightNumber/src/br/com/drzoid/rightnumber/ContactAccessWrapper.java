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

import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;

/**
 * A thin wrapper around contact access classes, which changed in API level 5.
 *
 * @author rdamazio
 */
@SuppressWarnings("deprecation")
public abstract class ContactAccessWrapper {

  public abstract String getAuthority();
  public abstract Uri getPhoneUri();
  public abstract String getPhoneIdColumn();
  public abstract String getPhoneNumberColumn();

  public static ContactAccessWrapper create() {
    if (RightNumberConstants.ANDROID_API_LEVEL >= 5) {
      return new EclairAccessWrapper();
    } else {
      return new CupcakeAccessWrapper();
    }
  }

  private static class EclairAccessWrapper extends ContactAccessWrapper {
    @Override
    public String getAuthority() {
      return ContactsContract.AUTHORITY;
    }

    @Override
    public Uri getPhoneUri() {
      return ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    }

    @Override
    public String getPhoneIdColumn() {
      return ContactsContract.CommonDataKinds.Phone._ID;
    }

    @Override
    public String getPhoneNumberColumn() {
      return ContactsContract.CommonDataKinds.Phone.NUMBER;
    }
  }

  private static class CupcakeAccessWrapper extends ContactAccessWrapper {
    @Override
    public String getAuthority() {
      return Contacts.AUTHORITY;
    }

    @Override
    public Uri getPhoneUri() {
      return Contacts.Phones.CONTENT_URI;
    }

    @Override
    public String getPhoneIdColumn() {
      return Contacts.Phones._ID;
    }

    @Override
    public String getPhoneNumberColumn() {
      return Contacts.Phones.NUMBER;
    }
  }
}
