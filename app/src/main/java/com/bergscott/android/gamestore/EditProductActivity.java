package com.bergscott.android.gamestore;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import com.bergscott.android.gamestore.data.GameStoreContract;
import com.bergscott.android.gamestore.data.GameStoreContract.ProductEntry;

public class EditProductActivity extends AppCompatActivity {

    /** EditText for product name */
    private EditText mNameEditText;

    /** EditText for product price */
    private EditText mPriceEditText;

    /** EditText for product quantity */
    private EditText mQuantityEditText;

    /** Spinner for selecting product's supplier */
    private Spinner mSupplierSpinner;

    /** Constant indicating no supplier */
    private final long NO_SUPPLIER = -1;

    /** id of product's supplier */
    private long mSupplier = NO_SUPPLIER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // find the relevant text views and assign them to variables
        mNameEditText = (EditText) findViewById(R.id.edit_text_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_text_product_quantity);
        mSupplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);

        // setup the spinner
        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the product's supplier.
     */
    private void setupSpinner() {

        String[] projection = { GameStoreContract.SupplierEntry._ID,
                GameStoreContract.SupplierEntry.COLUMN_SUPPLIER_NAME };

        // setup the cursor from the database and add a no supplier line
        Cursor dbCursor = getContentResolver().query(GameStoreContract.SupplierEntry.CONTENT_URI,
                projection, null, null, null);
        MatrixCursor extras = new MatrixCursor(new String[] {
                GameStoreContract.SupplierEntry._ID,
                GameStoreContract.SupplierEntry.COLUMN_SUPPLIER_NAME
        });
        extras.addRow(new String[] { Long.toString(NO_SUPPLIER), "No Supplier" });
        Cursor[] cursors = { extras, dbCursor };
        Cursor fullSpinnerCursor = new MergeCursor(cursors);

        // initialize a simple cursor adapter to attach to the spinner
        String[] from = { GameStoreContract.SupplierEntry.COLUMN_SUPPLIER_NAME };
        int[] to = { android.R.id.text1 };
        SimpleCursorAdapter supplierAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, fullSpinnerCursor, from, to, 0);

        supplierAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // set the adapter to the spinner
        mSupplierSpinner.setAdapter(supplierAdapter);

        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                mSupplier = id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSupplier = NO_SUPPLIER;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_supplier, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save_supplier:
                saveProduct();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        int price = 0;
        int quantity = 0;

        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "No Product Created", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(priceString)) {
            BigDecimal priceBigDecimal = new BigDecimal(priceString);
            price = priceBigDecimal.movePointRight(2).intValue();
        }

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        // Only include a supplier if a supplier was selected
        if (mSupplier != NO_SUPPLIER) {
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, mSupplier);
        }

        Uri resultUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        // check the resulting URI to see if the insert operatoin was a success and show a Toast
        if (resultUri == null) {
            Toast.makeText(this, "Error with saving product", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
        }

    }
}
