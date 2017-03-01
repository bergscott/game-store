package com.bergscott.android.gamestore;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.bergscott.android.gamestore.data.GameStoreContract.SupplierEntry;

/**
 * Created by bergs on 3/1/2017.
 */

public class SupplierCursorAdapter extends CursorAdapter {

    public SupplierCursorAdapter(Context context, Cursor c) { super(context, c, 0); }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_supplier, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // find the text view for the supplier name and set its text with the value in the cursor
        TextView nameTextView = (TextView) view.findViewById(R.id.list_item_supplier_name_text_view);
        nameTextView.setText(
                cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_NAME))
        );

        // find the text view for the supplier phone number and set its text with the value in the
        // cursor
        TextView phoneTextView = (TextView) view.findViewById(R.id.list_item_supplier_phone_text_view);
        String phoneNumber =
                cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_PHONE));
        phoneTextView.setText(context.getString(R.string.list_item_phone, phoneNumber));

        // find the text view for the website and set its text with the value in the cursor
        TextView webTextView = (TextView) view.findViewById(R.id.list_item_supplier_web_text_view);
        String website =
                cursor.getString(cursor.getColumnIndex(SupplierEntry.COLUMN_SUPPLIER_WEB));
        webTextView.setText(context.getString(R.string.list_item_web, website));
    }
}
