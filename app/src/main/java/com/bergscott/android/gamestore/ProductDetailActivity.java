package com.bergscott.android.gamestore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bergscott.android.gamestore.data.GameStoreContract;

public class ProductDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    TextView mNameTextView;
    TextView mPriceTextView;
    TextView mQuantityTextView;
    TextView mSupplierNameTextView;
    Button mPhoneButton;
    Button mWebButton;
    Button mShipmentButton;
    Button mSaleButton;

    Uri mProductUri;

    int mQuantity = 0;

    private final int PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        mProductUri = getIntent().getData();

        mNameTextView = (TextView) findViewById(R.id.detail_product_name);
        mPriceTextView = (TextView) findViewById(R.id.detail_product_price);
        mQuantityTextView = (TextView) findViewById(R.id.detail_product_quantity);
        mSupplierNameTextView = (TextView) findViewById(R.id.detail_supplier_name);
        mPhoneButton = (Button) findViewById(R.id.supplier_phone_button);
        mWebButton = (Button) findViewById(R.id.supplier_web_button);
        mShipmentButton = (Button) findViewById(R.id.detail_shipment_button);
        mSaleButton = (Button) findViewById(R.id.detail_sale_button);

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        mShipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create a ContentValues object with the new quantity (increment the quantity by 1)
                ContentValues values = new ContentValues();
                values.put(GameStoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, mQuantity + 1);

                // update the entry in the database
                updateQuantity(values);
            }
        });

        mSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // return early if quantity is 0
                if (mQuantity == 0) {
                    Toast.makeText(ProductDetailActivity.this, "No Quantity to Sell",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // create a ContentValues object with the new quantity (decrement quantity by 1)
                ContentValues values = new ContentValues();
                values.put(GameStoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, mQuantity - 1);

                // update the entry in the database
                updateQuantity(values);
            }
        });
    }

    /**
     * Updates the current product's quantity in the products table of the database
     * @param values ContentValues containing quantity key and new value pair
     */
    private void updateQuantity(ContentValues values) {
        Uri uri = ContentUris.withAppendedId(GameStoreContract.ProductEntry.CONTENT_URI,
                ContentUris.parseId(mProductUri));

        int rowsModified = getContentResolver().update(uri, values, null, null);

        // if now row of the database was updated, show the user a message indicating an error
        if (rowsModified == 0) {
            Toast.makeText(ProductDetailActivity.this, "Error with updating quantity",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, mProductUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            // set the Text of the layout TextViews with the appropriate values in the cursor
            mNameTextView.setText(cursor.getString(cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_PRODUCT_NAME)));
            int priceInCents = cursor.getInt(cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_PRODUCT_PRICE));
            mPriceTextView.setText(getString(R.string.list_item_price,
                    ProductUtils.getDecimalPrice(priceInCents)));
            mQuantity = cursor.getInt(cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_PRODUCT_QUANTITY));
            mQuantityTextView.setText(getString(R.string.list_item_quantity, mQuantity));
            mSupplierNameTextView.setText(cursor.getString(cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_SUPPLIER_NAME)));

            // setup the button to launch the phone dialer with the supplier's number
            int phoneColumnIndex = cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_SUPPLIER_PHONE);

            // if phone number is Null, hide the button
            if (cursor.isNull(phoneColumnIndex)) {
                mPhoneButton.setVisibility(View.GONE);
            } else {
                // phone number is not null, make button visible and set its OnClickListener
                mPhoneButton.setVisibility(View.VISIBLE);

                final Uri phoneUri = Uri.parse("tel:" + cursor.getString(cursor.getColumnIndex(
                        GameStoreContract.ProductWithSupplierEntry.COLUMN_SUPPLIER_PHONE)));

                mPhoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent phoneIntent = new Intent(Intent.ACTION_VIEW, phoneUri);
                        if (phoneIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(phoneIntent);
                        }
                    }
                });
            }

            // setup the button to launch the web browser with the supplier's url
            int webColumnIndex = cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_SUPPLIER_WEB);

            // if website is null, hide the button
            if (cursor.isNull(webColumnIndex)) {
                mWebButton.setVisibility(View.GONE);
            } else {
                // website is not null, so make button visible and set its OnClickListener
                mWebButton.setVisibility(View.VISIBLE);

                final Uri webUri = Uri.parse(cursor.getString(cursor.getColumnIndex(
                        GameStoreContract.ProductWithSupplierEntry.COLUMN_SUPPLIER_WEB)));

                mWebButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                        if (webIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(webIntent);
                        }
                    }
                });
            }

        } else {
            Log.w("ProductDetailActivity", "Loader returned empty cursor");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameTextView.setText(null);
        mPriceTextView.setText(null);
        mQuantityTextView.setText(null);
        mSupplierNameTextView.setText(null);
    }
}
