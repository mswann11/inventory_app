package com.example.android.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri currentProductUri;
    private EditText productNameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierNameEditText;
    private EditText supplierPhoneEditText;
    private boolean productHasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        if(currentProductUri == null){
            setTitle(R.string.title_add_product);
            invalidateOptionsMenu();
        } else{
            setTitle(R.string.title_edit_product);
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        productNameEditText = findViewById(R.id.edit_product_name);
        priceEditText = findViewById(R.id.edit_price);
        quantityEditText = findViewById(R.id.edit_quantity);
        supplierNameEditText = findViewById(R.id.edit_supplier_name);
        supplierPhoneEditText = findViewById(R.id.edit_supplier_phone);

        productNameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        supplierPhoneEditText.setOnTouchListener(touchListener);
    }

    private void saveProduct() {
        String productNameString = productNameEditText.getText().toString();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplierNameString = supplierNameEditText.getText().toString();
        String supplierPhoneString = supplierPhoneEditText.getText().toString();

        if(currentProductUri == null && TextUtils.isEmpty(productNameString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierPhoneString)){
            return;
        }

        double priceDouble = Double.parseDouble(priceString);
        int quantityInt = Integer.parseInt(quantityString);

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(ProductEntry.COLUMN_PRICE, priceDouble);
        values.put(ProductEntry.COLUMN_QUANTITY, quantityInt);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        if(currentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, R.string.product_save_fail_toast, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_save_success_toast, Toast.LENGTH_SHORT).show();
            }
        } else{
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if(rowsAffected == 0){
                Toast.makeText(this, R.string.product_update_fail_toast, Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, R.string.product_update_success_toast, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentProductUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if(!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!productHasChanged){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteProduct();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct(){
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
            ProductEntry._ID,
            ProductEntry.COLUMN_PRODUCT_NAME,
            ProductEntry.COLUMN_PRICE,
            ProductEntry.COLUMN_QUANTITY,
            ProductEntry.COLUMN_SUPPLIER_NAME,
            ProductEntry.COLUMN_SUPPLIER_PHONE
        };

        return new CursorLoader(this,
                currentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if(cursor.moveToFirst()){
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE);

            String productName = cursor.getString(productNameColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            productNameEditText.setText(productName);
            priceEditText.setText(Double.toString(price));
            quantityEditText.setText(Integer.toString(quantity));
            supplierNameEditText.setText(supplierName);
            supplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        productNameEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        supplierNameEditText.setText("");
        supplierPhoneEditText.setText("");
    }
}
