package com.example.inktouch.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inktouch.R;
import com.example.inktouch.adapters.ProductAdapter;
import com.example.inktouch.api.RetrofitClient;
import com.example.inktouch.models.Product;
import com.example.inktouch.utils.ImageUploader;
import com.example.inktouch.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private FloatingActionButton fabAdd;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SessionManager sessionManager;
    
    // Image picker
    private Uri selectedImageUri;
    private ImageView ivProductPreview;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        
        sessionManager = new SessionManager(getContext());
        
        // Ensure access token is set for API calls
        String token = sessionManager.getAccessToken();
        if (token != null) {
            RetrofitClient.setAccessToken(token);
        }
        
        setupImagePicker();
        
        initViews(view);
        setupRecyclerView();
        loadProducts();
        
        fabAdd.setOnClickListener(v -> showProductDialog(null));
        
        return view;
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (ivProductPreview != null && selectedImageUri != null) {
                        Glide.with(this)
                            .load(selectedImageUri)
                            .into(ivProductPreview);
                    }
                }
            }
        );
    }

    private void initViews(View view) {
        rvProducts = view.findViewById(R.id.rv_products);
        fabAdd = view.findViewById(R.id.fab_add_product);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(getContext(), this);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);
    }

    private void loadProducts() {
        showLoading(true);
        
        Call<List<Product>> call = RetrofitClient.getApi().getProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    adapter.setProducts(products);
                    tvEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProductDialog(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_product, null);
        
        TextView tvTitle = dialogView.findViewById(R.id.tv_dialog_title);
        TextInputEditText etName = dialogView.findViewById(R.id.et_product_name);
        TextInputEditText etPrice = dialogView.findViewById(R.id.et_product_price);
        TextInputEditText etStock = dialogView.findViewById(R.id.et_product_stock);
        ivProductPreview = dialogView.findViewById(R.id.iv_product_preview);
        Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Reset selected image
        selectedImageUri = null;

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        if (product != null) {
            tvTitle.setText(R.string.edit_product);
            etName.setText(product.getName());
            etPrice.setText(String.valueOf(product.getPrice()));
            etStock.setText(String.valueOf(product.getStock()));
            
            // Load existing image
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_product_placeholder)
                    .into(ivProductPreview);
            }
        }

        // Image picker button
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(getContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);

            // Check if need to upload image
            if (selectedImageUri != null) {
                // Upload image first
                btnSave.setEnabled(false);
                btnSave.setText("Uploading...");
                
                ImageUploader.uploadImage(getContext(), selectedImageUri, sessionManager.getAccessToken(), new ImageUploader.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        getActivity().runOnUiThread(() -> {
                            if (product == null) {
                                createProduct(name, price, stock, imageUrl, dialog);
                            } else {
                                updateProduct(product.getId(), name, price, stock, imageUrl, dialog);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        getActivity().runOnUiThread(() -> {
                            btnSave.setEnabled(true);
                            btnSave.setText(R.string.save);
                            Toast.makeText(getContext(), "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                // No new image, use existing or empty
                String imageUrl = product != null ? product.getImageUrl() : "";
                if (product == null) {
                    createProduct(name, price, stock, imageUrl, dialog);
                } else {
                    updateProduct(product.getId(), name, price, stock, imageUrl, dialog);
                }
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void createProduct(String name, double price, int stock, String imageUrl, AlertDialog dialog) {
        Product product = new Product(name, price, stock, imageUrl);
        
        Call<List<Product>> call = RetrofitClient.getApi().createProduct(product);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Toast.makeText(getContext(), "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadProducts();
                } else {
                    Toast.makeText(getContext(), "Gagal menambahkan produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct(String id, String name, double price, int stock, String imageUrl, AlertDialog dialog) {
        Product product = new Product(name, price, stock, imageUrl);
        
        Call<List<Product>> call = RetrofitClient.getApi().updateProduct("eq." + id, product);
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Toast.makeText(getContext(), "Produk berhasil diupdate", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadProducts();
                } else {
                    Toast.makeText(getContext(), "Gagal mengupdate produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProduct(Product product) {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_product_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    Call<Void> call = RetrofitClient.getApi().deleteProduct("eq." + product.getId());
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Product deleted", Toast.LENGTH_SHORT).show();
                                loadProducts();
                            } else {
                                Toast.makeText(getContext(), "Failed to delete product", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvProducts.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEditClick(Product product) {
        showProductDialog(product);
    }

    @Override
    public void onDeleteClick(Product product) {
        deleteProduct(product);
    }

    @Override
    public void onProductClick(Product product) {
        // Optional: Show product details
    }
}
