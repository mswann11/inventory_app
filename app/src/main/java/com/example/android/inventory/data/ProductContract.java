package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductContract {

    private ProductContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "products";

    public static abstract class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_PHONE = "supplier_phone";

        public static boolean isValidPhone(String phone){
            String PHONE_PATTERN = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
            Pattern pattern = Pattern.compile(PHONE_PATTERN);
            Matcher matcher = pattern.matcher(phone);
            return matcher.matches();
        }
    }
}
