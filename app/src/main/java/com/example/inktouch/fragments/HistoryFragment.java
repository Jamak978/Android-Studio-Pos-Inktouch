package com.example.inktouch.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inktouch.R;
import com.example.inktouch.adapters.OrderAdapter;
import com.example.inktouch.api.RetrofitClient;
import com.example.inktouch.models.Order;
import com.example.inktouch.models.OrderItem;
import com.example.inktouch.utils.ExcelExporter;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Button btnExportExcel;
    private Button btnShareWhatsApp;
    
    private List<Order> currentOrders;
    private Map<String, List<OrderItem>> orderItemsMap = new HashMap<>();
    
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        
        // Ensure access token is set for API calls
        com.example.inktouch.utils.SessionManager sessionManager = new com.example.inktouch.utils.SessionManager(getContext());
        String token = sessionManager.getAccessToken();
        if (token != null) {
            RetrofitClient.setAccessToken(token);
        }
        
        initViews(view);
        setupRecyclerView();
        loadOrders();
        
        return view;
    }

    private void initViews(View view) {
        rvOrders = view.findViewById(R.id.rv_orders);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        btnExportExcel = view.findViewById(R.id.btn_export_excel);
        btnShareWhatsApp = view.findViewById(R.id.btn_share_whatsapp);
        
        btnExportExcel.setOnClickListener(v -> exportToExcel());
        btnShareWhatsApp.setOnClickListener(v -> shareViaWhatsApp());
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(getContext(), this);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        showLoading(true);
        
        Call<List<Order>> call = RetrofitClient.getApi().getOrders("created_at.desc");
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentOrders = response.body();
                    adapter.setOrders(currentOrders);
                    tvEmpty.setVisibility(currentOrders.isEmpty() ? View.VISIBLE : View.GONE);
                    loadAllOrderItems();
                } else {
                    Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        showOrderDetails(order);
    }

    @Override
    public void onDeleteClick(Order order) {
        deleteOrder(order);
    }

    private void showOrderDetails(Order order) {
        // Load order items
        Call<List<OrderItem>> call = RetrofitClient.getApi().getOrderItems("eq." + order.getId());
        call.enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderItem> items = response.body();
                    displayOrderDialog(order, items);
                } else {
                    Toast.makeText(getContext(), "Failed to load order items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrderDialog(Order order, List<OrderItem> items) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        StringBuilder message = new StringBuilder();
        message.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        message.append("Customer: ").append(order.getCustomerName() != null && !order.getCustomerName().isEmpty() 
                ? order.getCustomerName() : "Guest").append("\n");
        
        // Format date
        if (order.getCreatedAt() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(order.getCreatedAt());
                message.append("Date: ").append(outputFormat.format(date)).append("\n");
            } catch (ParseException e) {
                message.append("Date: ").append(order.getCreatedAt()).append("\n");
            }
        }
        
        message.append("\nItems:\n");
        for (OrderItem item : items) {
            message.append("- ");
            if (item.getProduct() != null) {
                message.append(item.getProduct().getName());
            } else {
                message.append("Product ID: ").append(item.getProductId());
            }
            message.append(" x").append(item.getQty());
            message.append(" @ ").append(formatter.format(item.getPrice()));
            message.append(" = ").append(formatter.format(item.getTotalPrice()));
            message.append("\n");
        }
        
        message.append("\nSubtotal: ").append(formatter.format(order.getSubtotal())).append("\n");
        message.append("Cash: ").append(formatter.format(order.getCash())).append("\n");
        message.append("Change: ").append(formatter.format(order.getChange()));

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.order_details)
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void deleteOrder(Order order) {
        new AlertDialog.Builder(getContext())
                .setTitle("Hapus Order")
                .setMessage("Apakah Anda yakin ingin menghapus order " + order.getOrderNumber() + "?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    performDeleteOrder(order);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void performDeleteOrder(Order order) {
        Call<Void> call = RetrofitClient.getApi().deleteOrder("eq." + order.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Order berhasil dihapus", Toast.LENGTH_SHORT).show();
                    loadOrders();
                } else {
                    Toast.makeText(getContext(), "Gagal menghapus order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void loadAllOrderItems() {
        if (currentOrders == null || currentOrders.isEmpty()) {
            return;
        }

        orderItemsMap.clear();
        for (Order order : currentOrders) {
            Call<List<OrderItem>> call = RetrofitClient.getApi().getOrderItems("eq." + order.getId());
            call.enqueue(new Callback<List<OrderItem>>() {
                @Override
                public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        orderItemsMap.put(order.getId(), response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                    // Silent fail for individual items
                }
            });
        }
    }

    private void exportToExcel() {
        if (currentOrders == null || currentOrders.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data untuk di-export", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkStoragePermission()) {
            performExportToExcel();
        } else {
            requestStoragePermission();
        }
    }

    private void performExportToExcel() {
        try {
            File file = ExcelExporter.exportOrdersToExcel(getContext(), currentOrders, orderItemsMap);
            Toast.makeText(getContext(), "File berhasil di-export ke: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            
            // Option to open file
            new AlertDialog.Builder(getContext())
                    .setTitle("Export Berhasil")
                    .setMessage("File Excel telah disimpan di:\n" + file.getAbsolutePath() + "\n\nBuka file sekarang?")
                    .setPositiveButton("Buka", (dialog, which) -> openFile(file))
                    .setNegativeButton("Tutup", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error export: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void openFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(getContext(), 
                    getContext().getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Tidak ada aplikasi untuk membuka file Excel", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareViaWhatsApp() {
        if (currentOrders == null || currentOrders.isEmpty()) {
            Toast.makeText(getContext(), "Tidak ada data untuk dibagikan", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = ExcelExporter.generateWhatsAppMessage(currentOrders, orderItemsMap);
        
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/?text=" + Uri.encode(message)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int write = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            return write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performExportToExcel();
            } else {
                Toast.makeText(getContext(), "Permission diperlukan untuk export file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
