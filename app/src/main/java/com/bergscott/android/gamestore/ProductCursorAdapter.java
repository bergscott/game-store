package com.bergscott.android.gamestore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bergscott.android.gamestore.data.GameStoreContract;

/**
 * Created by bergs on 2/15/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_product, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        // find the text view for the name and set it to the value contained in the cursor
        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
        nameTextView.setText(cursor.getString(cursor.getColumnIndex(
                GameStoreContract.ProductEntry.COLUMN_PRODUCT_NAME)));

        // find the text view for the quantity and set it to the value contained in the cursor
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_text_view);
        final int quantity = cursor.getInt(cursor.getColumnIndex(
                GameStoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
        quantityTextView.setText(context.getString(R.string.list_item_quantity, quantity));

        // find the text view for the price and set it to the value contained in the cursor
        TextView priceTextView = (TextView) view.findViewById(R.id.price_text_view);
        int priceInCents = cursor.getInt(cursor.getColumnIndex(
                GameStoreContract.ProductEntry.COLUMN_PRODUCT_PRICE));
        priceTextView.setText(context.getString(R.string.list_item_price,
                ProductUtils.getDecimalPrice(priceInCents)));

        final int rowId = cursor.getInt(cursor.getColumnIndex(GameStoreContract.ProductEntry._ID));

        // find the sale button and set its onClickListener
        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantity == 0) {
                    Toast.makeText(context, "No quantity for sale", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri uri = ContentUris.withAppendedId(
                        GameStoreContract.ProductEntry.CONTENT_URI, rowId);
                ContentValues values = new ContentValues();
                values.put(GameStoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity - 1);

                int rowsUpdated = context.getContentResolver().update(uri, values, null, null);

                if (rowsUpdated == 0) {
                    Toast.makeText(context, "Problem updating database", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
