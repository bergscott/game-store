package com.bergscott.android.gamestore;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bergscott.android.gamestore.data.GameStoreContract;

import java.math.BigDecimal;

/**
 * Created by bergs on 2/15/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_game, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // find the text view for the name and set it to the value contained in the cursor
        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
        nameTextView.setText(cursor.getString(cursor.getColumnIndex(
                GameStoreContract.ProductEntry.COLUMN_PRODUCT_NAME)));

        // find the text view for the quantity and set it to the value contained in the cursor
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_text_view);
        int quantity = cursor.getInt(cursor.getColumnIndex(
                GameStoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
        quantityTextView.setText(context.getString(R.string.list_item_quantity, quantity));

        // find the text view for the price and set it to the value contained in the cursor
        TextView priceTextView = (TextView) view.findViewById(R.id.price_text_view);
        int priceInCents = cursor.getInt(cursor.getColumnIndex(
                GameStoreContract.ProductEntry.COLUMN_PRODUCT_PRICE));
        priceTextView.setText(context.getString(R.string.list_item_price,
                ProductUtils.getDecimalPrice(priceInCents)));
    }
}
