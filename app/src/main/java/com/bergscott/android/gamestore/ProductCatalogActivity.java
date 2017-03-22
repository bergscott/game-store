package com.bergscott.android.gamestore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bergscott.android.gamestore.data.GameStoreContract;
import com.bergscott.android.gamestore.data.GameStoreContract.ProductEntry;


public class ProductCatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /** ListView containing the list of products in the database */
    ListView mProductsListView;

    /** Cursor Adapter for the Product information from database */
    ProductCursorAdapter mProductCursorAdapter;

    /** Product Cursor Loader ID */
    private static final int PRODUCT_LOADER = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // find the list view
        mProductsListView = (ListView) findViewById(R.id.catalog_list_view);

        // setup the cursor adapter and set it to the list view
        mProductCursorAdapter = new ProductCursorAdapter(this, null);
        mProductsListView.setAdapter(mProductCursorAdapter);

        // set an on item click listener to launch the product detail activity when a list item is
        // selected
        mProductsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(ProductCatalogActivity.this, ProductDetailActivity.class);
                intent.setData(ContentUris.withAppendedId(
                        GameStoreContract.ProductWithSupplierEntry.CONTENT_URI, id
                ));
                startActivity(intent);
            }
        });

        // initialize the cursor loader for product data
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // get the URI for the products table in the database
        Uri uri = ProductEntry.CONTENT_URI;

        // setup the projection retrieving only the ID, Name, Quantity, and Price of the products
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE
        };

        // return the new cursor loader
        return new CursorLoader(this, uri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // swap the adapter's cursor to the newly loaded cursor
        mProductCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // adapter's data needs to be cleared, so wap in null and drop all reference to the previous
        // cursor to prevent memory leaks
        mProductCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_prodcut:
                Intent intentProduct = new Intent(
                        ProductCatalogActivity.this, EditProductActivity.class);
                startActivity(intentProduct);
                return true;
            case R.id.action_view_suppliers:
                Intent suppliersIntent = new Intent(
                        ProductCatalogActivity.this, SupplierCatalogActivity.class);
                startActivity(suppliersIntent);
                return true;
            case R.id.action_delete_all_products:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteAllProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
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

    private void deleteAllProducts() {
        int productsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        if (productsDeleted == 0) {
            Toast.makeText(this, "No products deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, productsDeleted + " products deleted!", Toast.LENGTH_SHORT).show();
        }
    }
}
