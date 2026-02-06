package com.example.myapplication.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ProductAdapter;
import com.example.myapplication.DatabaseHelper;
import com.example.myapplication.Product;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private DatabaseHelper db;

    private TextView tvTotalProducts, tvTotalItems, tvTotalValue, tvLowStock;
    private RecyclerView rvLowStock;
    private PieChart pieChart;
    private View cardLowStock;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        db = new DatabaseHelper(requireContext());

        tvTotalProducts = view.findViewById(R.id.tv_total_products);
        tvTotalItems = view.findViewById(R.id.tv_total_items);
        tvTotalValue = view.findViewById(R.id.tv_total_value);
        tvLowStock = view.findViewById(R.id.tv_low_stock_count);

        cardLowStock = view.findViewById(R.id.card_low_stock_alert);
        rvLowStock = view.findViewById(R.id.rv_low_stock);
        pieChart = view.findViewById(R.id.pie_chart);

        loadStats();
        loadLowStock();
        loadCategoryChart();

        return view;
    }

    private void loadStats() {
        tvTotalProducts.setText(String.valueOf(db.getTotalProductCount()));
        tvTotalItems.setText(String.valueOf(db.getTotalItemCount()));
        tvTotalValue.setText(String.format("$%.2f", db.getTotalInventoryValue()));
    }

    private void loadLowStock() {
        List<Product> lowStock = db.getLowStockProducts();
        tvLowStock.setText(String.valueOf(lowStock.size()));

        if (lowStock.isEmpty()) {
            cardLowStock.setVisibility(View.GONE);
            return;
        }

        cardLowStock.setVisibility(View.VISIBLE);

        rvLowStock.setLayoutManager(new LinearLayoutManager(requireContext()));

        // FIXED: must implement all 3 interface methods
        ProductAdapter adapter = new ProductAdapter(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) { }

            @Override
            public void onEditClick(Product product) { }

            @Override
            public void onDeleteClick(Product product) { }
        });

        adapter.setProducts(lowStock);
        rvLowStock.setAdapter(adapter);
    }

    private void loadCategoryChart() {
        List<Product> products = db.getAllProducts();

        Map<String, Integer> map = new HashMap<>();

        for (Product p : products) {
            String category = (p.getCategory() == null || p.getCategory().trim().isEmpty())
                    ? "Uncategorized"
                    : p.getCategory();

            map.put(category, map.getOrDefault(category, 0) + p.getQuantity());
        }

        List<PieEntry> entries = new ArrayList<>();
        for (String key : map.keySet()) {
            entries.add(new PieEntry(map.get(key), key));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.BLUE, Color.GREEN, Color.MAGENTA, Color.RED, Color.CYAN);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.setDrawEntryLabels(true);
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
    }
}
