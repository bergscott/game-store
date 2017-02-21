package com.bergscott.android.gamestore;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bergscott.android.gamestore.data.GameStoreContract;

import java.math.BigDecimal;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class ProductDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    TextView mNameTextView;
    TextView mPriceTextView;
    TextView mQuantityTextView;
    TextView mSupplierNameTextView;

    Uri mProductUri;

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

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, mProductUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            mNameTextView.setText(cursor.getString(cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_PRODUCT_NAME)));
            int priceInCents = cursor.getInt(cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_PRODUCT_PRICE));
            mPriceTextView.setText(getString(R.string.list_item_price,
                    ProductUtils.getDecimalPrice(priceInCents)));
            int quantity = cursor.getInt(cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_PRODUCT_QUANTITY));
            mQuantityTextView.setText(getString(R.string.list_item_quantity, quantity));
            mSupplierNameTextView.setText(cursor.getString(cursor.getColumnIndex(
                    GameStoreContract.ProductWithSupplierEntry.COLUMN_SUPPLIER_NAME)));
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
