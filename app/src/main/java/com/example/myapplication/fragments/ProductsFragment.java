package com.example.myapplication.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ProductAdapter;
import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ProductsFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView rvProducts;
    private LinearLayout emptyState;
    private EditText etSearch;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_products, container, false);

        db = new DatabaseHelper(requireContext());

        rvProducts = view.findViewById(R.id.rv_products);
        emptyState = view.findViewById(R.id.empty_state);
        etSearch = view.findViewById(R.id.et_search);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_product);

        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));


        adapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                showProductDetails(product);
            }

            @Override
            public void onEditClick(Product product) {
                showEditDialog(product);
            }

            @Override
            public void onDeleteClick(Product product) {
                showDeleteDialog(product);
            }
        });

        rvProducts.setAdapter(adapter);
        loadProducts();

        fab.setOnClickListener(v -> showAddDialog());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadProducts() {
        List<Product> list = db.getAllProducts();

        if (list.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }

        adapter.setProducts(list);
    }

    private void search(String text) {
        List<Product> results = db.searchProducts(text);
        adapter.setProducts(results);

        if (results.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_product, null);

        TextInputEditText name = dialogView.findViewById(R.id.et_product_name);
        TextInputEditText sku = dialogView.findViewById(R.id.et_product_sku);
        TextInputEditText cat = dialogView.findViewById(R.id.et_product_category);
        TextInputEditText qty = dialogView.findViewById(R.id.et_product_quantity);
        TextInputEditText min = dialogView.findViewById(R.id.et_product_min_stock);
        TextInputEditText price = dialogView.findViewById(R.id.et_product_price);
        TextInputEditText sup = dialogView.findViewById(R.id.et_product_supplier);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String nameStr = name.getText().toString().trim();
            String skuStr = sku.getText().toString().trim();

            if (nameStr.isEmpty() || skuStr.isEmpty()) {
                Toast.makeText(getContext(), "Name and SKU are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Product p = new Product(
                    nameStr,
                    skuStr,
                    cat.getText().toString(),
                    parseIntSafe(qty.getText().toString()),
                    parseIntSafe(min.getText().toString()),
                    parseDoubleSafe(price.getText().toString()),
                    sup.getText().toString()
            );
            db.addProduct(p);
            dialog.dismiss();
            Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
            loadProducts();
        });

        dialog.show();
    }

    private void showEditDialog(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_product, null);

        TextInputEditText name = dialogView.findViewById(R.id.et_product_name);
        TextInputEditText sku = dialogView.findViewById(R.id.et_product_sku);
        TextInputEditText cat = dialogView.findViewById(R.id.et_product_category);
        TextInputEditText qty = dialogView.findViewById(R.id.et_product_quantity);
        TextInputEditText min = dialogView.findViewById(R.id.et_product_min_stock);
        TextInputEditText price = dialogView.findViewById(R.id.et_product_price);
        TextInputEditText sup = dialogView.findViewById(R.id.et_product_supplier);

        // Pre-fill with existing data
        name.setText(product.getName());
        sku.setText(product.getSku());
        cat.setText(product.getCategory());
        qty.setText(String.valueOf(product.getQuantity()));
        min.setText(String.valueOf(product.getMinStock()));
        price.setText(String.valueOf(product.getPrice()));
        sup.setText(product.getSupplier());

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String nameStr = name.getText().toString().trim();
            String skuStr = sku.getText().toString().trim();

            if (nameStr.isEmpty() || skuStr.isEmpty()) {
                Toast.makeText(getContext(), "Name and SKU are required", Toast.LENGTH_SHORT).show();
                return;
            }

            product.setName(nameStr);
            product.setSku(skuStr);
            product.setCategory(cat.getText().toString());
            product.setQuantity(parseIntSafe(qty.getText().toString()));
            product.setMinStock(parseIntSafe(min.getText().toString()));
            product.setPrice(parseDoubleSafe(price.getText().toString()));
            product.setSupplier(sup.getText().toString());

            db.updateProduct(product);
            dialog.dismiss();
            Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
            loadProducts();
        });

        dialog.show();
    }


    private void showDeleteDialog(Product product) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete '" + product.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deleteProduct(product.getId());
                    Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show();
                    loadProducts();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showProductDetails(Product product) {
        String details = "Name: " + product.getName() +
                "\nSKU: " + product.getSku() +
                "\nCategory: " + product.getCategory() +
                "\nQuantity: " + product.getQuantity() +
                "\nMin Stock: " + product.getMinStock() +
                "\nPrice: $" + String.format("%.2f", product.getPrice()) +
                "\nSupplier: " + product.getSupplier();

        new AlertDialog.Builder(getContext())
                .setTitle("Product Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    // ✅ NEW METHOD: Safe integer parsing
    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ✅ NEW METHOD: Safe double parsing
    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}