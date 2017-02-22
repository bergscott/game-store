package com.bergscott.android.gamestore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.URLUtil;

import com.bergscott.android.gamestore.data.GameStoreContract.ProductEntry;
import com.bergscott.android.gamestore.data.GameStoreContract.SupplierEntry;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by bergs on 2/15/2017.
 */

public class GameStoreProvider extends ContentProvider {

    /** Tag for log messages */
    public static final String LOG_TAG = GameStoreProvider.class.getSimpleName();

    private final IllegalArgumentException PRODUCT_NAME_EXCEPTION =
            new IllegalArgumentException("Product requires a name");
    private final IllegalArgumentException PRODUCT_QUANTITY_EXCEPTION =
            new IllegalArgumentException("Product requires a valid quantity");
    private final IllegalArgumentException PRODUCT_PRICE_EXCEPTION =
            new IllegalArgumentException("Product requires a valid price");
    private final IllegalArgumentException SUPPLIER_NAME_EXCEPTION =
            new IllegalArgumentException("Supplier requires a name");
    private final IllegalArgumentException SUPPLIER_URL_EXCEPTION =
            new IllegalArgumentException("Invalid URL entered for supplier");
    private final IllegalArgumentException SUPPLIER_PHONE_EXCEPTION =
            new IllegalArgumentException("Invalid phone number entered");

    /** Database Helper for Pet SQL Database */
    private GameStoreDbHelper mDbHelper;

    /** Uri match code for querying full PRODUCTS table */
    private static final int PRODUCTS = 100;

    /** Uri match code for querying a single entry in the PRODUCTS table */
    private static final int PRODUCT_ID = 101;

    /** Uri match code for querying a single entry in the PRODUCTS table joined with its SUPPLIER */
    private static final int PRODUCT_WITH_SUPPLIER_ID = 111;

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
                GameStoreContract.PATH_PRODUCTS_WITH_SUPPLIER + "/#", PRODUCT_WITH_SUPPLIER_ID);
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
                cursor = database.query(ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI and query the products
                // table for the entry with that ID
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the products table for the row where the _id matches
                // the id extracted from the uri
                cursor = database.query(ProductEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case SUPPLIERS:
                // For the SUPPLIERS code, query the suppliers table directly
                cursor = database.query(SupplierEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null ,null, sortOrder);
                break;
            case SUPPLIER_ID:
                // For the SUPPLIER_ID code, extract out the ID from the URI and query the suppliers
                // table for the entry with that ID
                selection = SupplierEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the suppliers table for the row where the _id matches
                // the id extracted from the uri
                cursor = database.query(SupplierEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null ,null, sortOrder);
                break;
            case PRODUCT_WITH_SUPPLIER_ID:
                // For the PRODUCT_WITH_SUPPLIER_ID code, extract out the ID from the URI and query
                // a join of the product and suppliers table with that ID
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.rawQuery("SELECT * FROM "
                        + ProductEntry.TABLE_NAME + " LEFT JOIN "
                        + SupplierEntry.TABLE_NAME + " ON "
                        + ProductEntry.TABLE_NAME + "." + ProductEntry.COLUMN_PRODUCT_SUPPLIER + "="
                        + SupplierEntry.TABLE_NAME + "." + SupplierEntry._ID
                        + " WHERE " + ProductEntry.TABLE_NAME + "." + ProductEntry._ID
                        + "=?", selectionArgs);

                // set the uri to this entry in the products table for notification purposes
                uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,
                        ContentUris.parseId(uri));
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
        // if the product name column is present check that the name is not null or empty
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (TextUtils.isEmpty(name)) {
                throw PRODUCT_NAME_EXCEPTION;
            }
        } else {
            // if not present, throw an exception
            throw PRODUCT_NAME_EXCEPTION;
        }

        // if the product quantity column is present, check that it is valid (quantity can be null,
        // as it has a default value of 0)
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw PRODUCT_QUANTITY_EXCEPTION;
            }
        }

        // if the price column is present, check that it is not null and is valid
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price < 0) {
                throw PRODUCT_PRICE_EXCEPTION;
            }
        } else {
            throw PRODUCT_PRICE_EXCEPTION;
        }

        // check for supplier ID?

        return insertValuesToTable(uri, values, ProductEntry.TABLE_NAME);
    }

    private Uri insertSupplier(Uri uri, ContentValues values) {
        // if the supplier name column is empty or not present, throw an exception
        if (values.containsKey(SupplierEntry.COLUMN_SUPPLIER_NAME)) {
            String name = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_NAME);
            if (TextUtils.isEmpty(name)) {
                throw SUPPLIER_NAME_EXCEPTION;
            }
        } else {
            throw SUPPLIER_NAME_EXCEPTION;
        }

        // if the supplier web column is present, check to see if it is a valid url
        if (values.containsKey(SupplierEntry.COLUMN_SUPPLIER_WEB)) {
            String urlString = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_WEB);
            if(!Patterns.WEB_URL.matcher(urlString).matches()
                    || !URLUtil.isValidUrl(urlString)) {
                urlString = "http://" + urlString;
                if (Patterns.WEB_URL.matcher(urlString).matches()) {
                    values.put(SupplierEntry.COLUMN_SUPPLIER_WEB, urlString);
                } else {
                    throw SUPPLIER_URL_EXCEPTION;
                }
            }
        }

        // if the supplier phone column is present and is too short to be a phone number, throw
        // an exception
        if (values.containsKey(SupplierEntry.COLUMN_SUPPLIER_PHONE)) {
            String phoneString = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_PHONE);
            if (phoneString.length() < 4) {
                throw SUPPLIER_PHONE_EXCEPTION;
            }
        }

        return insertValuesToTable(uri, values, SupplierEntry.TABLE_NAME);
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
    public int update(Uri uri, ContentValues contentValues, 
                      String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case SUPPLIERS:
                return updateSupplier(uri, contentValues, selection, selectionArgs);
            case SUPPLIER_ID:
                selection = SupplierEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateSupplier(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    
    private int updateProduct(Uri uri, ContentValues values, String selection, 
                              String[] selectionArgs) {
        // if the product name column is present check that the name is not null or empty
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (TextUtils.isEmpty(name)) {
                throw PRODUCT_NAME_EXCEPTION;
            }
        }

        // if the product quantity column is present, check that it is valid (quantity can be null,
        // as it has a default value of 0)
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw PRODUCT_QUANTITY_EXCEPTION;
            }
        }

        // if the price column is present, check that it is not null and is valid
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price < 0) {
                throw PRODUCT_PRICE_EXCEPTION;
            }
        }

        // should we validate a supplier ID (FOREIGN KEY)??? (must be null or match a supplier's ID)

        // update the selected products in the products table
        return updateTable(ProductEntry.TABLE_NAME, uri, values, selection, selectionArgs);
    }
    
    private int updateSupplier(Uri uri, ContentValues values, String selection, 
                               String[] selectionArgs) {
        // if the supplier name column is present and is empty, throw an exception
        if (values.containsKey(SupplierEntry.COLUMN_SUPPLIER_NAME)) {
            String name = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_NAME);
            if (TextUtils.isEmpty(name)) {
                throw SUPPLIER_NAME_EXCEPTION;
            }
        }

        // if the supplier web column is present, check to see if it is a valid url
        if (values.containsKey(SupplierEntry.COLUMN_SUPPLIER_WEB)) {
            String urlString = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_WEB);
            if(!Patterns.WEB_URL.matcher(urlString).matches()) {
                throw SUPPLIER_URL_EXCEPTION;
            }
        }

        // if the supplier phone column is present and is too short to be a phone number, throw
        // an exception
        if (values.containsKey(SupplierEntry.COLUMN_SUPPLIER_PHONE)) {
            String phoneString = values.getAsString(SupplierEntry.COLUMN_SUPPLIER_PHONE);
            if (phoneString.length() < 4) {
                throw SUPPLIER_PHONE_EXCEPTION;
            }
        }

        return updateTable(SupplierEntry.TABLE_NAME, uri, values, selection, selectionArgs);
    }

    private int updateTable(String tableName, Uri uri, ContentValues values,
                            String selection, String[] selectionArgs) {

        // if there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get the writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Update the selected entries in the table with the given values,
        // storing the number of rows that were updated
        int rowsUpdated = database.update(tableName, values, selection, selectionArgs);

        // if any rows were updated, notify the content resolver of the change
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return the number of rows that were updated
        return rowsUpdated;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }
}
