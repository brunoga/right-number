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

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * A thin wrapper around contact access classes, which changed in API level 5.
 *
 * @author rdamazio
 */
@SuppressWarnings("deprecation")
public abstract class ContactAccessWrapper {
  /**
   * Interface for building operation batches and applying them, in an API-level-safe way.
   */
  public interface OperationBatchBuilder {
    /** Adds an update to a single string column. */
    boolean addUpdate(Uri updateUri, String column, String value);

    /** Applies all added updates. */
    boolean apply();

    /** Returns the current number of accumulated changes. */
    int getNumPendingChanges();
  }

  public abstract Uri getPhoneUri();
  public abstract String getPhoneIdColumn();
  public abstract String getPhoneNumberColumn();
  public abstract OperationBatchBuilder newBatchBuilder(int expectedSize);

  public static ContactAccessWrapper create(Context context) {
    if (RightNumberConstants.ANDROID_API_LEVEL >= 5) {
      return new EclairAccessWrapper(context);
    } else {
      return new CupcakeAccessWrapper(context);
    }
  }

  private static class EclairAccessWrapper extends ContactAccessWrapper {
    private final Context context;

    public EclairAccessWrapper(Context context) {
      this.context = context;
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

    /**
     * Eclair+ version of the batch builder.
     * This relies on {@link ContentResolver#applyBatch}.
     */
    private static class EclairOperationBatchBuilder implements OperationBatchBuilder {
      private final ArrayList<ContentProviderOperation> operations;
      private final ContentResolver resolver;

      public EclairOperationBatchBuilder(ContentResolver resolver, int expectedSize) {
        this.resolver = resolver;
        this.operations = new ArrayList<ContentProviderOperation>(expectedSize);
      }

      @Override
      public boolean addUpdate(Uri updateUri, String column, String value) {
        operations.add(ContentProviderOperation.newUpdate(updateUri)
            .withValue(column, value)
            .build());
        return true;
      }

      @Override
      public boolean apply() {
        if (operations.isEmpty()) return true;

        try {
          Log.d(RightNumberConstants.LOG_TAG, "Applying " + operations.size() + " changes");
          resolver.applyBatch(ContactsContract.AUTHORITY, operations);
          Log.d(RightNumberConstants.LOG_TAG, "Changes applied");
          return true;
        } catch (RemoteException e) {
          Log.e(RightNumberConstants.LOG_TAG, "Failed to apply changes", e);
          return false;
        } catch (OperationApplicationException e) {
          Log.e(RightNumberConstants.LOG_TAG, "Failed to apply changes", e);
          return false;
        } finally {
          operations.clear();
        }
      }

      @Override
      public int getNumPendingChanges() {
        return operations.size();
      }
    }

    @Override
    public OperationBatchBuilder newBatchBuilder(int expectedSize) {
      return new EclairOperationBatchBuilder(context.getContentResolver(), expectedSize);
    }
  }

  private static class CupcakeAccessWrapper extends ContactAccessWrapper {
    private final Context context;

    public CupcakeAccessWrapper(Context context) {
      this.context = context;
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

    /**
     * Cupcake version of the batch builder.
     * Since API level < 5 doesn't support batch updates, we apply each operation
     * immediately.
     */
    private class CupcakeOperationBatchBuilder implements OperationBatchBuilder {
      private final ContentResolver resolver;

      public CupcakeOperationBatchBuilder(ContentResolver resolver) {
        this.resolver = resolver;
      }

      @Override
      public boolean addUpdate(Uri updateUri, String column, String value) {
        // Apply immediately
        ContentValues values = new ContentValues(1);
        values.put(column, value);
        int numUpdates = resolver.update(updateUri, values, null, null);

        if (numUpdates < 1) {
          Log.e(RightNumberConstants.LOG_TAG, "Change not applied to URI " + updateUri);
          return false;
        }
        return true;
      }

      @Override
      public boolean apply() {
        // Do nothing
        return true;
      }

      @Override
      public int getNumPendingChanges() {
        // Nothing is ever accumulated here.
        return 0;
      }
    }

    @Override
    public OperationBatchBuilder newBatchBuilder(int expectedSize) {
      return new CupcakeOperationBatchBuilder(context.getContentResolver());
    }
  }
}
