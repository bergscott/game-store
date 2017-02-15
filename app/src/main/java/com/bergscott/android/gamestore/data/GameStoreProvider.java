package com.bergscott.android.gamestore.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by bergs on 2/15/2017.
 */

public class GameStoreProvider extends ContentProvider {

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
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
