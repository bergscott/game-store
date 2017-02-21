package com.bergscott.android.gamestore;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.bergscott.android.gamestore.data.GameStoreContract.SupplierEntry;

public class EditSupplierActivity extends AppCompatActivity {

    /** EditText field for Supplier name */
    private EditText mNameEditText;

    /** EditText field for Supplier phone number */
    private EditText mPhoneEditText;

    /** EditText field for Supplier website */
    private EditText mWebEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_supplier);

        // find the edit text fields in the layout
        mNameEditText = (EditText) findViewById(R.id.edit_text_supplier_name);
        mPhoneEditText = (EditText) findViewById(R.id.edit_text_supplier_phone);
        mWebEditText = (EditText) findViewById(R.id.edit_text_supplier_web);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_supplier, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_supplier:
                saveSupplier();
                finish();
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
        values.put(SupplierEntry.COLUMN_SUPPLIER_NAME, nameString);
        if (!TextUtils.isEmpty(phoneString)) {
            values.put(SupplierEntry.COLUMN_SUPPLIER_PHONE, phoneString);
        }
        if (!TextUtils.isEmpty(webString)) {
            values.put(SupplierEntry.COLUMN_SUPPLIER_WEB, webString);
        }

        // insert the supplier into the database
        Uri resultUri = getContentResolver().insert(SupplierEntry.CONTENT_URI, values);

        // check the resulting URI to see if the insert operatoin was a success and show a Toast
        if (resultUri == null) {
            Toast.makeText(this, "Error with saving supplier", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Supplier added!", Toast.LENGTH_SHORT).show();
        }
    }
}
