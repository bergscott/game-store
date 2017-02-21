package com.bergscott.android.gamestore;

import android.widget.TextView;

import com.bergscott.android.gamestore.data.GameStoreContract;

import java.math.BigDecimal;

/**
 * Created by bergs on 2/21/2017.
 */

public final class ProductUtils {

    public final static BigDecimal getDecimalPrice(int priceInCents) {
        return new BigDecimal(priceInCents).movePointLeft(2);
    }
}
