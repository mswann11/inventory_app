package com.example.android.inventory;

import android.content.ContentUris;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private Uri currentProductUri;
    private EditText productNameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierNameEditText;
    private EditText supplierPhoneEditText;
    private TextView productNameTextView;
    private TextView priceTextView;
    private TextView quantityValueTextView;
    private LinearLayout quantityDetailLayout;
    private TextView supplierNameTextView;
    private TextView supplierPhoneTextView;
    private TextView callButton;
    private TextView minusButton;
    private TextView minusButtonDetail;
    private TextView plusButton;
    private TextView plusButtonDetail;
    private TextView priceUnit;
    private TextView quantityLabel;
    private TextView requireProductName;
    private TextView requirePrice;
    private TextView requireSupplierName;
    private TextView requireSupplierPhone;
    private String productName;
    private double price;
    private int quantity;
    private String supplierName;
    private String supplierPhone;
    private boolean productHasChanged = false;
    private MenuItem editMenuItem;
    private MenuItem deleteMenuItem;
    private MenuItem saveMenuItem;

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

        productNameEditText = findViewById(R.id.edit_product_name);
        priceEditText = findViewById(R.id.edit_price);
        quantityEditText = findViewById(R.id.edit_quantity);
        supplierNameEditText = findViewById(R.id.edit_supplier_name);
        supplierPhoneEditText = findViewById(R.id.edit_supplier_phone);

        productNameTextView = findViewById(R.id.text_view_product_name);
        priceTextView = findViewById(R.id.text_view_price);
        quantityValueTextView = findViewById(R.id.text_view_quantity_value);
        quantityDetailLayout = findViewById(R.id.quantity_detail);
        supplierNameTextView = findViewById(R.id.text_view_supplier_name);
        supplierPhoneTextView = findViewById(R.id.text_view_supplier_phone);

        requireProductName = findViewById(R.id.require_title);
        requirePrice = findViewById(R.id.require_price);
        requireSupplierName = findViewById(R.id.require_supplier_name);
        requireSupplierPhone = findViewById(R.id.require_supplier_phone);

        productNameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        supplierPhoneEditText.setOnTouchListener(touchListener);

        callButton = findViewById(R.id.call_button);
        minusButton = findViewById(R.id.minus_button_edit);
        minusButtonDetail = findViewById(R.id.minus_button_detail);
        plusButton = findViewById(R.id.plus_button_edit);
        plusButtonDetail = findViewById(R.id.plus_button_detail);
        priceUnit = findViewById(R.id.editor_unit);
        quantityLabel = findViewById(R.id.quantity_label);

        if (currentProductUri == null) {
            setTitle(R.string.title_add_product);
            callButton.setVisibility(View.GONE);
        } else {
            setTitle(R.string.title_product_details);
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

            productNameEditText.setVisibility(View.GONE);
            priceEditText.setVisibility(View.GONE);
            quantityEditText.setVisibility(View.GONE);
            supplierNameEditText.setVisibility(View.GONE);
            supplierPhoneEditText.setVisibility(View.GONE);

            productNameTextView.setVisibility(View.VISIBLE);
            priceTextView.setVisibility(View.VISIBLE);
            quantityDetailLayout.setVisibility(View.VISIBLE);
            supplierNameTextView.setVisibility(View.VISIBLE);
            supplierPhoneTextView.setVisibility(View.VISIBLE);
            minusButtonDetail.setVisibility(View.VISIBLE);
            plusButtonDetail.setVisibility(View.VISIBLE);

            minusButton.setVisibility(View.GONE);
            plusButton.setVisibility(View.GONE);
            priceUnit.setVisibility(View.GONE);
            quantityLabel.setVisibility(View.GONE);
        }

        minusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (quantity > 0) {
                    decrementQuantity();
                }
                productHasChanged = true;
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                incrementQuantity();
                productHasChanged = true;
            }
        });

        minusButtonDetail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (quantity > 0) {
                    decrementQuantity();
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, quantity);
                    getContentResolver().update(currentProductUri, values, null, null);
                }
            }
        });

        plusButtonDetail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                incrementQuantity();
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_QUANTITY, quantity);
                getContentResolver().update(currentProductUri, values, null, null);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + supplierPhone));
                startActivity(dialIntent);
            }
        });
    }

    private void saveProduct() {
        String productNameString = productNameEditText.getText().toString();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplierNameString = supplierNameEditText.getText().toString();
        String supplierPhoneString = supplierPhoneEditText.getText().toString();

        if (currentProductUri == null && TextUtils.isEmpty(productNameString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierPhoneString)) {
            finish();
            return;
        }

        if (TextUtils.isEmpty(productNameString)) {
            requireProductName.setVisibility(View.VISIBLE);
            return;
        } else {
            requireProductName.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(priceString)) {
            requirePrice.setVisibility(View.VISIBLE);
            return;
        } else {
            requirePrice.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(supplierNameString)) {
            requireSupplierName.setVisibility(View.VISIBLE);
            return;
        } else {
            requireSupplierName.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(supplierPhoneString)) {
            requireSupplierPhone.setVisibility(View.VISIBLE);
            return;
        } else if (!isValidPhone(supplierPhoneString)) {
            requireSupplierPhone.setText(R.string.invalid_phone_error);
            requireSupplierPhone.setVisibility(View.VISIBLE);
            return;
        } else {
            requireSupplierPhone.setVisibility(View.GONE);
        }

        double priceDouble = Double.parseDouble(priceString);

        int quantityInt;
        if (quantityString.equals("")) {
            quantityInt = 0;
        } else {
            quantityInt = Integer.parseInt(quantityString);
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(ProductEntry.COLUMN_PRICE, priceDouble);
        values.put(ProductEntry.COLUMN_QUANTITY, quantityInt);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        if (currentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, R.string.product_save_fail_toast, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_save_success_toast, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.product_update_fail_toast, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_update_success_toast, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        editMenuItem = menu.findItem(R.id.action_edit);
        deleteMenuItem = menu.findItem(R.id.action_delete);
        saveMenuItem = menu.findItem(R.id.action_save);
        if (currentProductUri == null) {
            deleteMenuItem.setVisible(false);
            editMenuItem.setVisible(false);
        } else {
            saveMenuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case R.id.action_edit:
                enterEditMode();
                return true;

            case android.R.id.home:
                if (!productHasChanged) {
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
        if (!productHasChanged) {
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
                if (dialogInterface != null) {
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
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void enterEditMode() {
        productNameEditText.setVisibility(View.VISIBLE);
        priceEditText.setVisibility(View.VISIBLE);
        quantityEditText.setVisibility(View.VISIBLE);
        supplierNameEditText.setVisibility(View.VISIBLE);
        supplierPhoneEditText.setVisibility(View.VISIBLE);

        productNameTextView.setVisibility(View.GONE);
        priceTextView.setVisibility(View.GONE);
        quantityDetailLayout.setVisibility(View.GONE);
        supplierNameTextView.setVisibility(View.GONE);
        supplierPhoneTextView.setVisibility(View.GONE);
        minusButtonDetail.setVisibility(View.GONE);
        plusButtonDetail.setVisibility(View.GONE);

        minusButton.setVisibility(View.VISIBLE);
        plusButton.setVisibility(View.VISIBLE);
        priceUnit.setVisibility(View.VISIBLE);
        quantityLabel.setVisibility(View.VISIBLE);
        callButton.setVisibility(View.GONE);

        saveMenuItem.setVisible(true);
        editMenuItem.setVisible(false);
        setTitle(getString(R.string.title_edit_product));
    }

    public boolean isValidPhone(String phone) {
        String PHONE_PATTERN = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    public void decrementQuantity() {
        quantity--;
        quantityEditText.setText(Integer.toString(quantity));
        quantityValueTextView.setText(Integer.toString(quantity));
    }

    public void incrementQuantity() {
        quantity++;
        quantityEditText.setText(Integer.toString(quantity));
        quantityValueTextView.setText(Integer.toString(quantity));
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

        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE);

            productName = cursor.getString(productNameColumnIndex);
            price = cursor.getDouble(priceColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            supplierName = cursor.getString(supplierNameColumnIndex);
            supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            productNameEditText.setText(productName);
            priceEditText.setText(Double.toString(price));
            quantityEditText.setText(Integer.toString(quantity));
            supplierNameEditText.setText(supplierName);
            supplierPhoneEditText.setText(supplierPhone);

            productNameTextView.setText(String.format("%s%s", getString(R.string.title), productName));
            String formattedPrice = String.format("%.2f", price);
            priceTextView.setText(String.format("%s%s", getString(R.string.price), formattedPrice));
            quantityValueTextView.setText(Integer.toString(quantity));
            supplierNameTextView.setText(String.format("%s%s", getString(R.string.name), supplierName));
            supplierPhoneTextView.setText(String.format("%s%s", getString(R.string.phone), supplierPhone));
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
