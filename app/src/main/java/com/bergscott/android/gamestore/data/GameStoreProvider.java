package com.bergscott.android.gamestore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.R.attr.data;

/**
 * Created by bergs on 2/15/2017.
 */

public class GameStoreProvider extends ContentProvider {

    /** Tag for log messages */
    public static final String LOG_TAG = GameStoreProvider.class.getSimpleName();

    /** Database Helper for Pet SQL Database */
    private GameStoreDbHelper mDbHelper;

    /** Uri match code for querying full PRODUCTS table */
    private static final int PRODUCTS = 100;

    /** Uri match code for querying a single entry in the PRODUCTS table */
    private static final int PRODUCT_ID = 101;

    /** Uri match code for querying full SUPPLIERS table */
    private static final int SUPPLIERS = 200;

    /** Uri match code for querying a single entry in the SUPPLIERS table */
    private static final int SUPPLIER_ID = 201;

    /** Uri matcher for database requests */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // add the URIs to match to the URI matcher
    static {
        sUriMatcher.addURI(GameStoreContract.CONTENT_AUTHORITY,
                GameStoreContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(GameStoreContract.CONTENT_AUTHORITY,
                GameStoreContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
        sUriMatcher.addURI(GameStoreContract.CONTENT_AUTHORITY,
                GameStoreContract.PATH_SUPPLIERS, SUPPLIERS);
        sUriMatcher.addURI(GameStoreContract.CONTENT_AUTHORITY,
                GameStoreContract.PATH_SUPPLIERS + "/#", SUPPLIER_ID);
    }

    /**
     * Initialize the provider and the database helper object
     */
    @Override
    public boolean onCreate() {
        // Create and initialize a GameStoreDbHelper object to gain access to the database
        mDbHelper = new GameStoreDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Match the uri to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(GameStoreContract.ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI and query the products
                // table for the entry with that ID
                selection = GameStoreContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the products table for the row where the _id matches
                // the id extracted from the uri
                cursor = database.query(GameStoreContract.ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case SUPPLIERS:
                // For the SUPPLIERS code, query the suppliers table directly
                cursor = database.query(GameStoreContract.SupplierEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null ,null, sortOrder);
                break;
            case SUPPLIER_ID:
                // For the SUPPLIER_ID code, extract out the ID from the URI and query the suppliers
                // table for the entry with that ID
                selection = GameStoreContract.SupplierEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the suppliers table for the row where the _id matches
                // the id extracted from the uri
                cursor = database.query(GameStoreContract.SupplierEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null ,null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            case SUPPLIERS:
                return insertSupplier(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        // TODO: Sanity checks

        return insertValuesToTable(uri, values, GameStoreContract.ProductEntry.TABLE_NAME);
    }

    private Uri insertSupplier(Uri uri, ContentValues values) {
        // TODO: Sanity checks

        return insertValuesToTable(uri, values, GameStoreContract.SupplierEntry.TABLE_NAME);
    }

    /**
     * Inserts the given content values into the table with the given name. Returns the Uri
     * of the new entry
     * @param uri address of the table
     * @param values valuse of new entry to be inserted into table
     * @param tableName name of the table in the database
     * @return the uri of the new  table entry
     */
    private Uri insertValuesToTable(Uri uri, ContentValues values, String tableName) {
        // get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // insert the new supplier with the given values into the suppliers table
        long id = database.insert(tableName, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify the content resolver of the change
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID of the newly inserted row appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }
}
