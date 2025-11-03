package com.example.inktouch.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inktouch.R;
import com.example.inktouch.adapters.CartAdapter;
import com.example.inktouch.adapters.TransactionProductAdapter;
import com.example.inktouch.api.RetrofitClient;
import com.example.inktouch.models.CartItem;
import com.example.inktouch.models.Order;
import com.example.inktouch.models.OrderItem;
import com.example.inktouch.models.Product;
import com.example.inktouch.utils.OrderNumberGenerator;
import com.example.inktouch.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionFragment extends Fragment implements 
        TransactionProductAdapter.OnProductSelectListener,
        CartAdapter.OnCartItemChangeListener {

    private RecyclerView rvProducts, rvCart;
    private TransactionProductAdapter productAdapter;
    private CartAdapter cartAdapter;
    private TextInputEditText etCustomerName, etCash;
    private TextView tvSubtotal, tvChange;
    private Button btnProcess;
    private SessionManager sessionManager;
    private List<CartItem> cartItems = new ArrayList<>();
    private NumberFormat currencyFormatter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        
        sessionManager = new SessionManager(getContext());
        
        // Ensure access token is set for API calls
        String token = sessionManager.getAccessToken();
        if (token != null) {
            RetrofitClient.setAccessToken(token);
        }
        
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        initViews(view);
        setupRecyclerViews();
        setupListeners();
        loadProducts();
        
        return view;
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        rvCart = view.findViewById(R.id.rv_cart);
        etCustomerName = view.findViewById(R.id.et_customer_name);
        etCash = view.findViewById(R.id.et_cash);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvChange = view.findViewById(R.id.tv_change);
        btnProcess = view.findViewById(R.id.btn_process);
    }

    private void setupRecyclerViews() {
        // Products RecyclerView (horizontal grid)
        productAdapter = new TransactionProductAdapter(getContext(), this);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.HORIZONTAL, false));
        rvProducts.setAdapter(productAdapter);

        // Cart RecyclerView
        cartAdapter = new CartAdapter(getContext(), this);
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        etCash.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateChange();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnProcess.setOnClickListener(v -> processTransaction());
    }

    private void loadProducts() {
        Call<List<Product>> call = RetrofitClient.getApi().getProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productAdapter.setProducts(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Error loading products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onProductSelected(Product product) {
        // Check if product already in cart
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                if (item.getQuantity() < product.getStock()) {
                    item.incrementQuantity();
                    cartAdapter.notifyDataSetChanged();
                    updateSubtotal();
                } else {
                    Toast.makeText(getContext(), "Stock limit reached", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }

        // Add new item to cart
        cartItems.add(new CartItem(product, 1));
        cartAdapter.setCartItems(cartItems);
        updateSubtotal();
    }

    @Override
    public void onQuantityChanged() {
        updateSubtotal();
    }

    @Override
    public void onItemRemoved() {
        updateSubtotal();
    }

    private void updateSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        tvSubtotal.setText(currencyFormatter.format(subtotal));
        calculateChange();
    }

    private void calculateChange() {
        String cashStr = etCash.getText().toString().trim();
        if (cashStr.isEmpty()) {
            tvChange.setText(currencyFormatter.format(0));
            return;
        }

        double cash = Double.parseDouble(cashStr);
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }

        double change = cash - subtotal;
        tvChange.setText(currencyFormatter.format(change));
        tvChange.setTextColor(getResources().getColor(
                change >= 0 ? R.color.success : R.color.error
        ));
    }

    private void processTransaction() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), R.string.cart_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        String cashStr = etCash.getText().toString().trim();
        if (cashStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter cash amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double cash = Double.parseDouble(cashStr);
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }

        if (cash < subtotal) {
            Toast.makeText(getContext(), R.string.insufficient_cash, Toast.LENGTH_SHORT).show();
            return;
        }

        double change = cash - subtotal;
        String customerName = etCustomerName.getText().toString().trim();
        String orderNumber = OrderNumberGenerator.generate();
        String userId = sessionManager.getUserId();

        // Create order
        Order order = new Order(orderNumber, customerName, subtotal, cash, change, userId);
        
        btnProcess.setEnabled(false);
        
        Call<List<Order>> call = RetrofitClient.getApi().createOrder(order);
        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Order createdOrder = response.body().get(0);
                    createOrderItems(createdOrder.getId());
                } else {
                    btnProcess.setEnabled(true);
                    Toast.makeText(getContext(), "Gagal membuat order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                btnProcess.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrderItems(String orderId) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(
                    orderId,
                    cartItem.getProduct().getId(),
                    cartItem.getQuantity(),
                    cartItem.getProduct().getPrice()
            );
            orderItems.add(orderItem);
        }

        // Create order items one by one (Supabase REST API limitation)
        createOrderItemsSequentially(orderItems, 0);
    }

    private void createOrderItemsSequentially(List<OrderItem> orderItems, int index) {
        if (index >= orderItems.size()) {
            // All items created successfully
            btnProcess.setEnabled(true);
            Toast.makeText(getContext(), R.string.transaction_success, Toast.LENGTH_LONG).show();
            clearTransaction();
            return;
        }

        OrderItem item = orderItems.get(index);
        Call<List<OrderItem>> call = RetrofitClient.getApi().createOrderItem(item);
        call.enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(Call<List<OrderItem>> call, Response<List<OrderItem>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Update stock after order item is created
                    updateProductStock(item.getProductId(), item.getQty(), orderItems, index);
                } else {
                    btnProcess.setEnabled(true);
                    Toast.makeText(getContext(), "Gagal membuat order items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderItem>> call, Throwable t) {
                btnProcess.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProductStock(String productId, int quantitySold, List<OrderItem> orderItems, int currentIndex) {
        // Get current product to retrieve current stock
        Call<List<Product>> getProductCall = RetrofitClient.getApi().getProductById("eq." + productId);
        getProductCall.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Product product = response.body().get(0);
                    int newStock = product.getStock() - quantitySold;
                    
                    // Update product with new stock
                    Product updatedProduct = new Product();
                    updatedProduct.setStock(newStock);
                    
                    Call<List<Product>> updateCall = RetrofitClient.getApi().updateProduct("eq." + productId, updatedProduct);
                    updateCall.enqueue(new Callback<List<Product>>() {
                        @Override
                        public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                            if (response.isSuccessful()) {
                                // Continue with next order item
                                createOrderItemsSequentially(orderItems, currentIndex + 1);
                            } else {
                                btnProcess.setEnabled(true);
                                Toast.makeText(getContext(), "Gagal update stok produk", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Product>> call, Throwable t) {
                            btnProcess.setEnabled(true);
                            Toast.makeText(getContext(), "Error update stok: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    btnProcess.setEnabled(true);
                    Toast.makeText(getContext(), "Gagal mendapatkan data produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                btnProcess.setEnabled(true);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearTransaction() {
        cartItems.clear();
        cartAdapter.setCartItems(cartItems);
        etCustomerName.setText("");
        etCash.setText("");
        tvSubtotal.setText(currencyFormatter.format(0));
        tvChange.setText(currencyFormatter.format(0));
        loadProducts(); // Reload to update stock
    }
}
