package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSku, tvCategory, tvQuantity, tvPrice, tvStatus;
        ImageButton btnEdit, btnDelete;  // ✅ ADDED THIS

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvSku = itemView.findViewById(R.id.tv_product_sku);
            tvCategory = itemView.findViewById(R.id.tv_product_category);
            tvQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvStatus = itemView.findViewById(R.id.tv_stock_status);

            // ✅ ADDED THESE TWO LINES
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(Product product) {
            tvName.setText(product.getName());
            tvSku.setText("SKU: " + product.getSku());
            tvCategory.setText(product.getCategory());
            tvQuantity.setText(String.valueOf(product.getQuantity()));
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));

            if (product.isLowStock()) {
                tvStatus.setText("LOW STOCK");
                tvStatus.setBackgroundResource(R.drawable.bg_status_low);
            } else {
                tvStatus.setText("IN STOCK");
                tvStatus.setBackgroundResource(R.drawable.bg_status_good);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });

            // ✅ ADDED EDIT BUTTON CLICK LISTENER
            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(product);
            });

            // ✅ ADDED DELETE BUTTON CLICK LISTENER
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(product);
            });
        }
    }
}