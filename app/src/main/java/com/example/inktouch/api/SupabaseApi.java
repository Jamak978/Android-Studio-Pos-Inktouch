package com.example.inktouch.api;

import com.example.inktouch.models.Order;
import com.example.inktouch.models.OrderItem;
import com.example.inktouch.models.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {
    
    // Products endpoints
    @GET("rest/v1/products?select=*")
    Call<List<Product>> getProducts();

    @GET("rest/v1/products?select=*")
    Call<List<Product>> getProductById(@Query("id") String id);

    @POST("rest/v1/products")
    Call<List<Product>> createProduct(@Body Product product);

    @PATCH("rest/v1/products")
    Call<List<Product>> updateProduct(@Query("id") String id, @Body Product product);

    @DELETE("rest/v1/products")
    Call<Void> deleteProduct(@Query("id") String id);

    // Orders endpoints
    @GET("rest/v1/orders?select=*")
    Call<List<Order>> getOrders(@Query("order") String order);

    @GET("rest/v1/orders?select=*")
    Call<List<Order>> getOrderById(@Query("id") String id);

    @POST("rest/v1/orders")
    Call<List<Order>> createOrder(@Body Order order);

    @DELETE("rest/v1/orders")
    Call<Void> deleteOrder(@Query("id") String id);

    // Order Items endpoints
    @GET("rest/v1/order_items?select=*,products(*)")
    Call<List<OrderItem>> getOrderItems(@Query("order_id") String orderId);

    @POST("rest/v1/order_items")
    Call<List<OrderItem>> createOrderItem(@Body OrderItem orderItem);
}
