package com.bergscott.android.gamestore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bergscott.android.gamestore.data.GameStoreContract.SupplierEntry;

public class SupplierCatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /** ListView containing the list of suppliers in the database */
    ListView mSuppliersListView;

    /** Cursor Adapter for the Supplier information from database */
    SupplierCursorAdapter mSupplierCursorAdapter;

    /** Supplier Cursor Loader ID */
    private static final int SUPPLIER_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // find the list view in the layout, create an adapter, and set the adapter to the view
        mSuppliersListView = (ListView) findViewById(R.id.catalog_list_view);
        mSupplierCursorAdapter = new SupplierCursorAdapter(this, null);
        mSuppliersListView.setAdapter(mSupplierCursorAdapter);

        // set the OnItemClickListener to launch the supplier editor when a supplier is clicked
        mSuppliersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(SupplierCatalogActivity.this, EditSupplierActivity.class);
                intent.setData(ContentUris.withAppendedId(SupplierEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        // initialize the cursor loader for supplier entries
        getLoaderManager().initLoader(SUPPLIER_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri uri = SupplierEntry.CONTENT_URI;

        String[] projection = {
                SupplierEntry._ID,
                SupplierEntry.COLUMN_SUPPLIER_NAME,
                SupplierEntry.COLUMN_SUPPLIER_PHONE,
                SupplierEntry.COLUMN_SUPPLIER_WEB
        };

        return new CursorLoader(this, uri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // swap the cursor adapter's cursor with the newly returned cursor
        mSupplierCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // clear the cursor from the adapter, since it is being reset
        mSupplierCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
