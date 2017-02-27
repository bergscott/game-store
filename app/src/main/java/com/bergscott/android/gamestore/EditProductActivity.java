package com.bergscott.android.gamestore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
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
import android.widget.Toast;

import java.math.BigDecimal;

import com.bergscott.android.gamestore.data.GameStoreContract;
import com.bergscott.android.gamestore.data.GameStoreContract.ProductEntry;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class EditProductActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

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

    /** Constant spinner position for No Supplier */
    private final int SPINNER_SUPPLIER_NONE = 0;

    /** id of product's supplier */
    private long mSupplier = NO_SUPPLIER;

    /** uri of product passed in with intent */
    private Uri mProductUri;

    /** ID constants for CursorLoaders */
    private final int PRODUCTS_LOADER = 0;
    private final int SUPPLIERS_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // find the relevant text views and assign them to variables
        mNameEditText = (EditText) findViewById(R.id.edit_text_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_text_product_quantity);
        mSupplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);

        mProductUri = getIntent().getData();

        if (mProductUri == null) {
            setTitle("Create Product");
        } else {
            setTitle("Edit Product");
        }

        // setup the spinner with a CursorLoader
        getLoaderManager().initLoader(SUPPLIERS_LOADER, null, this);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the product's supplier.
     * @param cursor Cursor containing full list of supplier ids and names, including "No Supplier"
     */
    private void setupSpinner(Cursor cursor) {

        // initialize a simple cursor adapter to attach to the spinner
        String[] from = { GameStoreContract.SupplierEntry.COLUMN_SUPPLIER_NAME };
        int[] to = { android.R.id.text1 };
        SimpleCursorAdapter supplierAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item, cursor, from, to, 0);

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
        });    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection;
        switch (id) {
            case PRODUCTS_LOADER:
                // set up the projection for the database query
                projection = new String[] {
                        ProductEntry._ID,
                        ProductEntry.COLUMN_PRODUCT_NAME,
                        ProductEntry.COLUMN_PRODUCT_PRICE,
                        ProductEntry.COLUMN_PRODUCT_QUANTITY,
                        ProductEntry.COLUMN_PRODUCT_SUPPLIER
                };
                Log.v("EditProdcutActivity", "Products Loader Started");
                return new CursorLoader(this, mProductUri, projection, null, null, null);
            case SUPPLIERS_LOADER:
                projection = new String[] {
                        GameStoreContract.SupplierEntry._ID,
                        GameStoreContract.SupplierEntry.COLUMN_SUPPLIER_NAME };
                Log.v("EditProdcutActivity", "Suppliers Loader Started");
                return new CursorLoader(this, GameStoreContract.SupplierEntry.CONTENT_URI,
                        projection, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch (loader.getId()) {
            case PRODUCTS_LOADER:
                Log.v("EditProdcutActivity", "Products Loader Finished");
                if (cursor.moveToFirst()) {
                    mNameEditText.setText(cursor.getString(
                            cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));
                    BigDecimal price = ProductUtils.getDecimalPrice(cursor.getInt(
                            cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE)));
                    mPriceEditText.setText(String.valueOf(price));
                    mQuantityEditText.setText(cursor.getString(
                            cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY)));
                    long supplierId = cursor.getLong(
                            cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER));
                    mSupplierSpinner.setSelection(
                            getPositionOfSupplier(mSupplierSpinner, supplierId));
                }
                break;
            case SUPPLIERS_LOADER:
                Log.v("EditProdcutActivity", "Suppliers Loader Finished");
                MatrixCursor extras = new MatrixCursor(new String[] {
                        GameStoreContract.SupplierEntry._ID,
                        GameStoreContract.SupplierEntry.COLUMN_SUPPLIER_NAME
                });
                extras.addRow(new String[] { Long.toString(NO_SUPPLIER), "No Supplier" });
                Cursor[] cursors = { extras, cursor };
                Cursor fullSpinnerCursor = new MergeCursor(cursors);
                setupSpinner(fullSpinnerCursor);
                // if we are editing an existing Product, load it's details into the view with a loader
                if (mProductUri != null) {
                    getLoaderManager().initLoader(PRODUCTS_LOADER, null, this);
                }
                break;
            default: // Do nothing
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case PRODUCTS_LOADER:
                Log.v("EditProdcutActivity", "Products Loader Reset");
                mNameEditText.getText().clear();
                mPriceEditText.getText().clear();
                mQuantityEditText.getText().clear();
                mSupplierSpinner.setSelection(SPINNER_SUPPLIER_NONE);
                break;
            case SUPPLIERS_LOADER:
                Log.v("EditProdcutActivity", "Suppliers Loader Reset");
                mSupplierSpinner.setAdapter(null);
                break;
            default: // Do nothing
        }
    }

    private int getPositionOfSupplier(Spinner spinner, Long id) {
        if(id == null) {
            return SPINNER_SUPPLIER_NONE;
        }
        // get the spinner's cursor adapter
        SimpleCursorAdapter spinnerAdapter = (SimpleCursorAdapter) spinner.getAdapter();
        // get the cursor from the adapter
        Cursor cursor = spinnerAdapter.getCursor();
        // loop through each row of the cursor
        while (cursor.moveToNext()) {
            // if the id of the row in the cursor matches the supplier id we're looking for, return
            // the current position
            if (cursor.getLong(cursor.getColumnIndex(GameStoreContract.SupplierEntry._ID)) == id) {
                Log.v("EditProductActivity", "Supplier_ID: " + id + " Position: " + cursor.getPosition());
                return cursor.getPosition();
            }
        }
        // if the supplier is not found in the cursor, return the No Supplier position
        return SPINNER_SUPPLIER_NONE;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_supplier, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // if there is no product URI, a new product is being created, so hide the delete button
        if (mProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_entry);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save_entry:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete_entry:
                deleteProduct();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "No Product Created", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();

        if (!TextUtils.isEmpty(nameString)) {
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        }

        if (!TextUtils.isEmpty(priceString)) {
            BigDecimal priceBigDecimal = new BigDecimal(priceString);
            int price = priceBigDecimal.movePointRight(2).intValue();
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        }

        if (!TextUtils.isEmpty(quantityString)) {
            int quantity = Integer.parseInt(quantityString);
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        }

        // Only include a supplier if a supplier was selected, or null if no supplier
        if (mSupplier != NO_SUPPLIER) {
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, mSupplier);
        } else {
            values.putNull(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        }

        try {
            if (mProductUri == null) {
                // creating a new product, so insert it into the database
                Uri resultUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

                // check the resulting URI to see if the insert operation was a success
                // and show a Toast message
                if (resultUri == null) {
                    Toast.makeText(this, "Error with saving product", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // editing an existing product, so update it
                int rowsUpdated = getContentResolver().update(mProductUri, values, null, null);

                // display a Toast message indicating if the update was a success
                if (rowsUpdated == 0) {
                    Toast.makeText(this, "Product not updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Product updated!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Not saved: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        // return early if we are not editing a product
        if (mProductUri == null) { return; }

        // delete the product that is being edited
        int rowsDeleted = getContentResolver().delete(mProductUri, null, null);

        // show a Toast message to the user indicated if deletion was successful
        if (rowsDeleted == 0) {
            Toast.makeText(this, "No Product Deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Product Deleted", Toast.LENGTH_SHORT).show();
        }

    }
}
