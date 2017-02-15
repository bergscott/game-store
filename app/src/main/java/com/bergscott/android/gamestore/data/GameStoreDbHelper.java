package com.bergscott.android.gamestore.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.bergscott.android.gamestore.data.GameStoreContract.ProductEntry;
import com.bergscott.android.gamestore.data.GameStoreContract.SupplierEntry;


/**
 * Created by bergs on 2/15/2017.
 */

public class GameStoreDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "game_store.db";

    public GameStoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // create a String SQL statement to create the suppliers table
        String SQL_CREATE_ENTRIES_SUPPLIERS =
                "CREATE TABLE " + SupplierEntry.TABLE_NAME + " ("
                + SupplierEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SupplierEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + SupplierEntry.COLUMN_SUPPLIER_PHONE + " INTEGER, "
                + SupplierEntry.COLUMN_SUPPLIER_WEB + " TEXT);";

        // create a String SQL statement to create the products table
        String SQL_CREATE_ENTRIES_PRODUCTS =
                "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0), "
                + ProductEntry.COLUMN_PRODUCT_SUPPLIER + " INTEGER, "
                + "FOREIGN KEY(" + ProductEntry.COLUMN_PRODUCT_SUPPLIER + ") REFERENCES " +
                        SupplierEntry.TABLE_NAME + "(" + SupplierEntry._ID + "));";

        // execute the statements and create the table
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES_SUPPLIERS);
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES_PRODUCTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // the db is still at version 1, nothing to do here.
    }
}
