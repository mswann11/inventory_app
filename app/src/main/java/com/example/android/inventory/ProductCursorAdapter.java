package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.data.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    public View newView(Context context, Cursor c, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    public void bindView(View view, final Context context, final Cursor c) {
        TextView productNameTextView = view.findViewById(R.id.product_name);
        TextView priceTextView = view.findViewById(R.id.price);
        final TextView quantityTextView = view.findViewById(R.id.quantity);

        String productName = c.getString(c.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        String price = c.getString(c.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE));
        Double priceDouble = Double.parseDouble(price);
        String formattedPrice = "$" + String.format("%.2f", priceDouble);
        String quantityString = c.getString(c.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY));
        final int quantityInt = Integer.parseInt(quantityString);

        productNameTextView.setText(productName);
        priceTextView.setText(formattedPrice);
        quantityTextView.setText(quantityString);

        final String id = c.getString(c.getColumnIndex(ProductEntry._ID));
        final ImageView saleButton = view.findViewById(R.id.sale_button);
        if (quantityInt == 0) {
            saleButton.setBackgroundResource(R.drawable.out_of_stock_button_background);
        } else {
            saleButton.setBackgroundResource(R.drawable.sale_button_background);
        }
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityInt > 0) {
                    Uri currentUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, Long.parseLong(id));
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, quantityInt - 1);
                    context.getContentResolver().update(currentUri, values, null, null);
                    changeCursor(c);
                }
            }
        });
    }
}
