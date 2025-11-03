package com.example.inktouch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inktouch.api.RetrofitClient;
import com.example.inktouch.models.AuthRequest;
import com.example.inktouch.models.AuthResponse;
import com.example.inktouch.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvTitle, tvToggleText, tvToggleAction;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvTitle = findViewById(R.id.tv_title);
        tvToggleText = findViewById(R.id.tv_toggle_text);
        tvToggleAction = findViewById(R.id.tv_toggle_action);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            if (isLoginMode) {
                performLogin();
            } else {
                performSignUp();
            }
        });

        tvToggleAction.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            tvTitle.setText(R.string.login);
            btnLogin.setText(R.string.login);
            tvToggleText.setText(R.string.dont_have_account);
            tvToggleAction.setText(R.string.sign_up);
        } else {
            tvTitle.setText(R.string.sign_up);
            btnLogin.setText(R.string.sign_up);
            tvToggleText.setText(R.string.already_have_account);
            tvToggleAction.setText(R.string.login);
        }
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        AuthRequest request = new AuthRequest(email, password);
        Call<AuthResponse> call = RetrofitClient.getAuthApi().signIn(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    
                    // Save session
                    sessionManager.createLoginSession(
                            authResponse.getAccessToken(),
                            authResponse.getUser().getId(),
                            authResponse.getUser().getEmail()
                    );
                    
                    // Set token for API calls
                    RetrofitClient.setAccessToken(authResponse.getAccessToken());
                    
                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    String errorMsg = "Invalid credentials";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("Invalid login credentials")) {
                                errorMsg = "Email atau password salah. Pastikan sudah Sign Up terlebih dahulu.";
                            } else if (errorBody.contains("Email not confirmed")) {
                                errorMsg = "Email belum dikonfirmasi. Cek inbox email Anda.";
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showLoading(false);
                String errorMsg = "Error: " + t.getMessage();
                if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMsg = "Tidak dapat terhubung ke server. Periksa koneksi internet Anda.";
                }
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performSignUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        AuthRequest request = new AuthRequest(email, password);
        Call<AuthResponse> call = RetrofitClient.getAuthApi().signUp(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(LoginActivity.this, "Sign up berhasil! Silakan login.", Toast.LENGTH_LONG).show();
                    etPassword.setText("");
                    toggleMode(); // Switch to login mode
                } else {
                    String errorMsg = "Sign up gagal";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("already registered")) {
                                errorMsg = "Email sudah terdaftar. Silakan login atau gunakan email lain.";
                            } else if (errorBody.contains("Password should be")) {
                                errorMsg = "Password minimal 6 karakter.";
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showLoading(false);
                String errorMsg = "Error: " + t.getMessage();
                if (t.getMessage() != null && t.getMessage().contains("Unable to resolve host")) {
                    errorMsg = "Tidak dapat terhubung ke server. Periksa koneksi internet Anda.";
                }
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
