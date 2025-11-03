package com.example.inktouch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inktouch.R;
import com.example.inktouch.models.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionProductAdapter extends RecyclerView.Adapter<TransactionProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> products;
    private OnProductSelectListener listener;

    public interface OnProductSelectListener {
        void onProductSelected(Product product);
    }

    public TransactionProductAdapter(Context context, OnProductSelectListener listener) {
        this.context = context;
        this.products = new ArrayList<>();
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_product, parent, false);
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

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvStock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_transaction_product);
            tvName = itemView.findViewById(R.id.tv_transaction_product_name);
            tvPrice = itemView.findViewById(R.id.tv_transaction_product_price);
            tvStock = itemView.findViewById(R.id.tv_transaction_product_stock);
        }

        public void bind(Product product) {
            tvName.setText(product.getName());
            
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            tvPrice.setText(formatter.format(product.getPrice()));
            
            tvStock.setText("Stock: " + product.getStock());

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(ivProduct);
            } else {
                ivProduct.setImageResource(R.drawable.ic_product_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null && product.getStock() > 0) {
                    listener.onProductSelected(product);
                }
            });

            // Disable if out of stock
            itemView.setAlpha(product.getStock() > 0 ? 1.0f : 0.5f);
            itemView.setEnabled(product.getStock() > 0);
        }
    }
}
