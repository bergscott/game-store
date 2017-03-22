package com.bergscott.android.gamestore;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bergscott.android.gamestore.data.GameStoreContract.SupplierEntry;

public class EditSupplierActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field for Supplier name */
    private EditText mNameEditText;

    /** EditText field for Supplier phone number */
    private EditText mPhoneEditText;

    /** EditText field for Supplier website */
    private EditText mWebEditText;

    /** Uri of the supplier being edited. Null if creating a new product */
    private Uri mSupplierUri;

    /** Id of suppliers loader */
    private final int SUPPLIERS_LOADER = 0;

    private boolean mSupplierHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mSupplierHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_supplier);

        // find the edit text fields in the layout
        mNameEditText = (EditText) findViewById(R.id.edit_text_supplier_name);
        mPhoneEditText = (EditText) findViewById(R.id.edit_text_supplier_phone);
        mWebEditText = (EditText) findViewById(R.id.edit_text_supplier_web);

        // Get the current supplier's uri from the intent. If a new supplier is being created, the
        // uri will be null
        mSupplierUri = getIntent().getData();

        // set the activity's title based on whether we are creating a supplier or editing an
        // existing supplier
        if (mSupplierUri == null) {
            setTitle("Create Supplier");
        } else {
            setTitle("Edit Supplier");
        }

        mNameEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mWebEditText.setOnTouchListener(mTouchListener);

        // if we are editing an existing supplier, initialize a loader that will fill in the edit
        // text fields with the pre-existing values
        if (mSupplierUri != null) {
            getLoaderManager().initLoader(SUPPLIERS_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_supplier, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // if we are creating a new supplier, hide the delete option
        if (mSupplierUri == null) {
            menu.findItem(R.id.action_delete_entry).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_entry:
                saveSupplier();
                finish();
                return true;
            case R.id.action_delete_entry:
                deleteSupplier();
                finish();
                return true;
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mSupplierHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditSupplierActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditSupplierActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSupplier() {
        String nameString = mNameEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String webString = mWebEditText.getText().toString().trim();

        // if no values were entered, inform the user that no supplier was created and return early
        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(phoneString)
                && TextUtils.isEmpty(webString)) {
            Toast.makeText(this, "No Supplier Created", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(SupplierEntry.COLUMN_SUPPLIER_NAME)) {
            values.put(SupplierEntry.COLUMN_SUPPLIER_NAME, nameString);
        }

        if (!TextUtils.isEmpty(phoneString)) {
            values.put(SupplierEntry.COLUMN_SUPPLIER_PHONE, phoneString);
        } else {
            values.putNull(SupplierEntry.COLUMN_SUPPLIER_PHONE);
        }

        if (!TextUtils.isEmpty(webString)) {
            values.put(SupplierEntry.COLUMN_SUPPLIER_WEB, webString);
        } else {
            values.putNull(SupplierEntry.COLUMN_SUPPLIER_WEB);
        }

        // insert the supplier into the database
        try {
            if (mSupplierUri == null) {
                // insert the new supplier into the suppliers table
                Uri resultUri = getContentResolver().insert(SupplierEntry.CONTENT_URI, values);

                // check the resulting URI to see if the insert operation was a success and show a Toast
                if (resultUri == null) {
                    Toast.makeText(this, "Error with saving supplier", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Supplier added!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // updating an existing supplier, so update it
                int rowsUpdated = getContentResolver().update(mSupplierUri, values, null, null);

                // check to see if the update was a success a show a message to the user
                if (rowsUpdated == 0) {
                    Toast.makeText(this, "Error with updating supplier", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Supplier updated!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Not saved: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSupplier() {
        // return early if we are creating a new supplier
        if (mSupplierUri == null) { return; }

        // delete the supplier that is currently being edited
        int rowsDeleted = getContentResolver().delete(mSupplierUri, null, null);

        // show a Toast message to the user indicated if deletion was successful
        if (rowsDeleted == 0) {
            Toast.makeText(this, "No Supplier Deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Supplier Deleted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                SupplierEntry._ID,
                SupplierEntry.COLUMN_SUPPLIER_NAME,
                SupplierEntry.COLUMN_SUPPLIER_PHONE,
                SupplierEntry.COLUMN_SUPPLIER_WEB
        };
        return new CursorLoader(this, mSupplierUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            mNameEditText.setText(cursor.getString(
                    cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_NAME)));
            String phoneNumber = cursor.getString(
                    cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_PHONE));
            mPhoneEditText.setText(phoneNumber);
            String website = cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_WEB));
            mWebEditText.setText(website);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.getText().clear();
        mPhoneEditText.getText().clear();
        mWebEditText.getText().clear();
    }

    @Override
    public void onBackPressed() {
        if (!mSupplierHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
