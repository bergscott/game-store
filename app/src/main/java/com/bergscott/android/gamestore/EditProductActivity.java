package com.bergscott.android.gamestore;

import android.content.ContentValues;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import com.bergscott.android.gamestore.data.GameStoreContract.ProductEntry;

public class EditProductActivity extends AppCompatActivity {

    /** EditText for product name */
    private EditText mNameEditText;

    /** EditText for product price */
    private EditText mPriceEditText;

    /** EditText for product quantity */
    private EditText mQuantityEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        // find the edit text views and assign them to variables
        mNameEditText = (EditText) findViewById(R.id.edit_text_product_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_text_product_quantity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_supplier, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save_supplier:
                saveProduct();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        int price = 0;
        int quantity = 0;

        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, "No Product Created", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(priceString)) {
            BigDecimal priceBigDecimal = new BigDecimal(priceString);
            price = priceBigDecimal.movePointRight(2).intValue();
        }

        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        Uri resultUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        // check the resulting URI to see if the insert operatoin was a success and show a Toast
        if (resultUri == null) {
            Toast.makeText(this, "Error with saving product", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show();
        }

    }
}
