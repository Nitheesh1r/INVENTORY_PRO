package com.example.myapplication.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.Product;
import com.example.myapplication.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class TransactionsFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView rvTransactions;
    private LinearLayout emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        db = new DatabaseHelper(requireContext());

        rvTransactions = view.findViewById(R.id.rv_transactions);
        emptyState = view.findViewById(R.id.empty_state);

        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_add_transaction);

        fab.setOnClickListener(v -> showAddTransactionDialog());

        loadTransactions();

        return view;
    }

    private void loadTransactions() {
        List<Transaction> list = db.getAllTransactions();

        if (list.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }

        rvTransactions.setAdapter(new TransactionAdapter(list));
    }

    private void showAddTransactionDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_transaction, null);

        AutoCompleteTextView spinnerProduct = dialogView.findViewById(R.id.spinner_product);
        AutoCompleteTextView spinnerType = dialogView.findViewById(R.id.spinner_type);

        TextInputEditText qty = dialogView.findViewById(R.id.et_quantity);
        TextInputEditText notes = dialogView.findViewById(R.id.et_notes);

        List<Product> products = db.getAllProducts();

        ArrayList<String> productNames = new ArrayList<>();
        for (Product p : products) productNames.add(p.getName());

        spinnerProduct.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, productNames));

        spinnerType.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, new String[]{"in", "out"}));

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            Product p = products.get(productNames.indexOf(spinnerProduct.getText().toString()));

            int qtyVal = Integer.parseInt(qty.getText().toString());
            String type = spinnerType.getText().toString();

            if (type.equals("out") && p.getQuantity() < qtyVal) {
                qty.setError("Insufficient stock");
                return;
            }

            Transaction t = new Transaction(
                    p.getId(),
                    p.getName(),
                    type,
                    qtyVal,
                    notes.getText().toString()
            );

            db.addTransaction(t);

            if (type.equals("in")) {
                p.setQuantity(p.getQuantity() + qtyVal);
            } else {
                p.setQuantity(p.getQuantity() - qtyVal);
            }
            db.updateProduct(p);

            dialog.dismiss();
            loadTransactions();
        });

        dialog.show();
    }

    // Simple adapter for transactions
    private static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.Holder> {

        private final List<Transaction> list;

        public TransactionAdapter(List<Transaction> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transactions, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int i) {

            Transaction t = list.get(i);

            h.type.setText(t.isStockIn() ? "STOCK IN" : "STOCK OUT");
            h.qty.setText((t.isStockIn() ? "+" : "-") + t.getQuantity());
            h.name.setText(t.getProductName());
            h.notes.setText("Notes: " + t.getNotes());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class Holder extends RecyclerView.ViewHolder {

            TextView type, date, name, qty, notes;

            public Holder(@NonNull View v) {
                super(v);

                type = v.findViewById(R.id.tv_transaction_type);
                date = v.findViewById(R.id.tv_transaction_date);
                name = v.findViewById(R.id.tv_product_name);
                qty = v.findViewById(R.id.tv_quantity);
                notes = v.findViewById(R.id.tv_transaction_notes);
            }
        }
    }
}
