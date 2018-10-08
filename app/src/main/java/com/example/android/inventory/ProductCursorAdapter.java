package com.example.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter{
    public ProductCursorAdapter(Context context,Cursor c){
        super(context, c, 0);
    }

    public View newView(Context context, Cursor c, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    public void bindView(View view, Context context, Cursor c){
        TextView productNameTextView = view.findViewById(R.id.product_name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        TextView supplierNameTextView = view.findViewById(R.id.supplier_name);
        TextView supplierPhoneTextView = view.findViewById(R.id.supplier_phone);

        String productName = c.getString(c.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME));
        String price = c.getString(c.getColumnIndexOrThrow(ProductEntry.COLUMN_PRICE));
        String quantity = c.getString(c.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY));
        String supplierName = c.getString(c.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER_NAME));
        String supplierPhone = c.getString(c.getColumnIndexOrThrow(ProductEntry.COLUMN_SUPPLIER_PHONE));

        productNameTextView.setText(productName);
        priceTextView.setText(price);
        quantityTextView.setText(quantity);
        supplierNameTextView.setText(supplierName);
        supplierPhoneTextView.setText(supplierPhone);
    }
}
