package com.example.inktouch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inktouch.R;
import com.example.inktouch.models.Order;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
        void onDeleteClick(Order order);
    }

    public OrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.orders = new ArrayList<>();
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvCustomerName, tvSubtotal, tvDate;
        ImageView ivDelete;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tv_order_number);
            tvCustomerName = itemView.findViewById(R.id.tv_order_customer);
            tvSubtotal = itemView.findViewById(R.id.tv_order_subtotal);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        public void bind(Order order) {
            tvOrderNumber.setText(order.getOrderNumber());
            tvCustomerName.setText(order.getCustomerName() != null && !order.getCustomerName().isEmpty() 
                    ? order.getCustomerName() : "Guest");
            
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            tvSubtotal.setText(formatter.format(order.getSubtotal()));

            // Format date
            if (order.getCreatedAt() != null) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                    Date date = inputFormat.parse(order.getCreatedAt());
                    tvDate.setText(outputFormat.format(date));
                } catch (ParseException e) {
                    tvDate.setText(order.getCreatedAt());
                }
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(order);
                }
            });
        }
    }
}
