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
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Helper which can update all the user's contacts to international format.
 *
 * @author rdamazio
 */
public class ContactChanger implements DialogInterface.OnCancelListener {
  private final Context context;
  private final Handler uiHandler = new Handler();
  private final AtomicBoolean updatingCancelled = new AtomicBoolean(false);

  public ContactChanger(Context context) {
    this.context = context;
  }

  public void updateContacts() {
    // First, show a confirmation dialog.
    // TODO: Add a preview option
    Builder alertBuilder = new AlertDialog.Builder(context);
    alertBuilder.setMessage(R.string.change_contacts_confirm)
                .setPositiveButton(android.R.string.yes, new OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    onConfirmedUpdateContacts();
                  }
                })
                .setNegativeButton(android.R.string.no, new OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                  }
                });
    alertBuilder.create().show();
  }

  protected void onConfirmedUpdateContacts() {
    // Show a progress dialog
    final ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setTitle(R.string.change_contacts_progress_title);
    progressDialog.setMessage(context.getString(R.string.change_contacts_progress_formatting));
    progressDialog.setIndeterminate(false);
    progressDialog.setCancelable(true);
    progressDialog.setOnCancelListener(this);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.show();

    // Start a thread to do the contact updating
    new Thread() {
      @Override
      public void run() {
        doContactUpdates(progressDialog);
      }
    }.start();
  }

  private void doContactUpdates(final ProgressDialog progressDialog) {
    updatingCancelled.set(false);

    // Query for all phone numbers
    ContactAccessWrapper contactAccess = ContactAccessWrapper.create();
    // TODO: This requires API level 5
    ContentResolver contentResolver = context.getContentResolver();
    Cursor cursor = contentResolver.query(contactAccess.getPhoneUri(),
        new String[] { contactAccess.getPhoneIdColumn(), contactAccess.getPhoneNumberColumn() },
        null,   // selection
        null,   // selectionArgs
        null);  // sortOrder

    if (cursor == null) {
      Log.e(RightNumberConstants.LOG_TAG, "Unable to get phone numbers cursor");
      dismissDialog(progressDialog);
    }

    int numPhoneNumbers = cursor.getCount();
    progressDialog.setMax(numPhoneNumbers);

    // Prepare for formatting
    TelephonyManager telephonyManager =
        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    String originalCountry = telephonyManager.getSimCountryIso().toUpperCase();
    PhoneNumberFormatter phoneNumberFormatter = new PhoneNumberFormatter(context);
    final ArrayList<ContentProviderOperation> changes =
        new ArrayList<ContentProviderOperation>(numPhoneNumbers);

    boolean partialFailure = false;
    while (!updatingCancelled.get() && cursor.moveToNext()) {
      // Update progress.
      incrementProgressBy(progressDialog, 1);

      // Get the number
      int columnIdx = cursor.getColumnIndexOrThrow(contactAccess.getPhoneNumberColumn());
      String number = cursor.getString(columnIdx);

      // Parse it.
      String newNumber;
      try {
      	newNumber = phoneNumberFormatter.formatPhoneNumber(number, originalCountry,
      		originalCountry, true);
      } catch (IllegalArgumentException e) {
      	Log.e(RightNumberConstants.LOG_TAG, "Invalid number: " + number, e);
      	partialFailure = true;
      	continue;
      }
      
      if (newNumber.equals(number)) {
        // Nothing to do.
        Log.d(RightNumberConstants.LOG_TAG, "Not updating number " + number);
        continue;
      }

      // Get the number ID to update
      int idColumnIdx = cursor.getColumnIndexOrThrow(contactAccess.getPhoneIdColumn());
      long numberId = cursor.getLong(idColumnIdx);
      Uri updateUri = Uri.withAppendedPath(contactAccess.getPhoneUri(), Long.toString(numberId));

      // Create the update operation
      Log.d(RightNumberConstants.LOG_TAG,
          "Updating number: id=" + numberId + "; old=" + number + "; new=" + newNumber);
      changes.add(ContentProviderOperation.newUpdate(updateUri)
          .withValue(contactAccess.getPhoneNumberColumn(), newNumber)
          .build());

      // Apply every 1000 changes.
      if (changes.size() >= 1000) {
        partialFailure |= !applyChanges(changes, contactAccess, contentResolver, progressDialog);
      }
    }

    // Apply any remaining changes
    partialFailure |= !applyChanges(changes, contactAccess, contentResolver, progressDialog);

    // All done.
    cursor.close();
    dismissDialog(progressDialog);

    if (partialFailure) {
      showToast(R.string.change_contacts_partial_failure);
    } else {
      showToast(R.string.change_contacts_success);
    }
  }

  private boolean applyChanges(ArrayList<ContentProviderOperation> changes,
      ContactAccessWrapper contactAccess, ContentResolver contentResolver,
      ProgressDialog progressDialog) {
    if (changes.isEmpty()) return true;

    setProgressMessage(progressDialog, R.string.change_contacts_progress_applying);

    boolean success = true;
    try {
      Log.d(RightNumberConstants.LOG_TAG, "Applying " + changes.size() + " changes");
      contentResolver.applyBatch(contactAccess.getAuthority(), changes);
      Log.d(RightNumberConstants.LOG_TAG, "Changes applied");
    } catch (RemoteException e) {
      Log.e(RightNumberConstants.LOG_TAG, "Failed to apply changes", e);
      success = false;
    } catch (OperationApplicationException e) {
      Log.e(RightNumberConstants.LOG_TAG, "Failed to apply changes", e);
      success = false;
    }

    changes.clear();

    setProgressMessage(progressDialog, R.string.change_contacts_progress_formatting);

    return success;
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    updatingCancelled.set(true);
    showToast(R.string.change_contacts_cancelled);
  }

  private void dismissDialog(final ProgressDialog progressDialog) {
    uiHandler.post(new Runnable() {
      @Override
      public void run() {
        progressDialog.dismiss();
      }
    });
  }

  private void showToast(final int resId) {
    uiHandler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
      }
    });
  }

  private void setProgressMessage(final ProgressDialog progressDialog, final int resId) {
    uiHandler.post(new Runnable() {
      @Override
      public void run() {
        progressDialog.setMessage(context.getString(resId));
      }
    });
  }

  private void incrementProgressBy(final ProgressDialog progressDialog, final int progressed) {
    uiHandler.post(new Runnable() {
      @Override
      public void run() {
        progressDialog.incrementProgressBy(progressed);
      }
    });
  }
}
