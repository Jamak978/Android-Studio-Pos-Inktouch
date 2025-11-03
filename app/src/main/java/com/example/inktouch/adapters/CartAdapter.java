package com.example.inktouch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inktouch.R;
import com.example.inktouch.models.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged();
        void onItemRemoved();
    }

    public CartAdapter(Context context, OnCartItemChangeListener listener) {
        this.context = context;
        this.cartItems = new ArrayList<>();
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem, position);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity, tvTotal;
        ImageButton btnMinus, btnPlus, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_cart_product);
            tvName = itemView.findViewById(R.id.tv_cart_product_name);
            tvPrice = itemView.findViewById(R.id.tv_cart_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            tvTotal = itemView.findViewById(R.id.tv_cart_total);
            btnMinus = itemView.findViewById(R.id.btn_cart_minus);
            btnPlus = itemView.findViewById(R.id.btn_cart_plus);
            btnRemove = itemView.findViewById(R.id.btn_cart_remove);
        }

        public void bind(CartItem cartItem, int position) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            
            tvName.setText(cartItem.getProduct().getName());
            tvPrice.setText(formatter.format(cartItem.getProduct().getPrice()));
            tvQuantity.setText(String.valueOf(cartItem.getQuantity()));
            tvTotal.setText(formatter.format(cartItem.getTotalPrice()));

            if (cartItem.getProduct().getImageUrl() != null && !cartItem.getProduct().getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(cartItem.getProduct().getImageUrl())
                        .placeholder(R.drawable.ic_product_placeholder)
                        .error(R.drawable.ic_product_placeholder)
                        .into(ivProduct);
            } else {
                ivProduct.setImageResource(R.drawable.ic_product_placeholder);
            }

            btnPlus.setOnClickListener(v -> {
                if (cartItem.getQuantity() < cartItem.getProduct().getStock()) {
                    cartItem.incrementQuantity();
                    notifyItemChanged(position);
                    if (listener != null) {
                        listener.onQuantityChanged();
                    }
                }
            });

            btnMinus.setOnClickListener(v -> {
                if (cartItem.getQuantity() > 1) {
                    cartItem.decrementQuantity();
                    notifyItemChanged(position);
                    if (listener != null) {
                        listener.onQuantityChanged();
                    }
                }
            });

            btnRemove.setOnClickListener(v -> {
                cartItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
                if (listener != null) {
                    listener.onItemRemoved();
                }
            });
        }
    }
}
