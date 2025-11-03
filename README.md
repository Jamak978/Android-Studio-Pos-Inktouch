# InkTouch - Point of Sale (POS) Android Application

A modern Android POS application built with Java and Supabase backend for Vivo Y22 devices.

## Features

- **Authentication**: Login and Sign Up using Supabase Authentication
- **Product Management**: Full CRUD operations for products with **image upload** to Supabase Storage
- **Image Upload**: Pick images from gallery and upload directly to Supabase Storage
- **Transaction Processing**: Create sales transactions with cart functionality
- **Order History**: View all past transactions with detailed information
- **User Profile**: Display logged-in user email and logout functionality
- **Bottom Navigation**: Easy navigation between main features

## Tech Stack

- **Language**: Java
- **IDE**: Android Studio Chipmunk
- **Backend**: Supabase (Database + Authentication + Storage)
- **API Communication**: Retrofit 2
- **Image Loading**: Glide
- **Image Upload**: Supabase Storage with OkHttp
- **UI Components**: Material Design Components
- **Architecture**: Fragment-based with Bottom Navigation

## Color Theme

- Primary: `#04616E`
- Primary Light: `#B7D7C9`

## Database Schema

### Products Table
```sql
create table public.products (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  price numeric(12,2) not null,
  stock integer not null default 0,
  image_url text,
  created_at timestamp with time zone default now()
);
```

### Orders Table
```sql
create table public.orders (
  id uuid primary key default gen_random_uuid(),
  order_number text unique not null,
  customer_name text,
  subtotal numeric(12,2) not null default 0,
  cash numeric(12,2) not null default 0,
  change numeric(12,2) not null default 0,
  user_id uuid not null,
  created_at timestamp with time zone default now()
);
```

### Order Items Table
```sql
create table public.order_items (
  id uuid primary key default gen_random_uuid(),
  order_id uuid not null references public.orders(id) on delete cascade,
  product_id uuid not null references public.products(id),
  qty integer not null check (qty > 0),
  price numeric(12,2) not null
);
```

## Project Structure

```
app/src/main/java/com/example/inktouch/
├── adapters/
│   ├── CartAdapter.java
│   ├── OrderAdapter.java
│   ├── ProductAdapter.java
│   └── TransactionProductAdapter.java
├── api/
│   ├── RetrofitClient.java
│   ├── SupabaseApi.java
│   └── SupabaseAuthApi.java
├── fragments/
│   ├── HistoryFragment.java
│   ├── ProductsFragment.java
│   ├── ProfileFragment.java
│   └── TransactionFragment.java
├── models/
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Product.java
│   └── User.java
├── utils/
│   ├── OrderNumberGenerator.java
│   └── SessionManager.java
├── LoginActivity.java
└── MainActivity.java
```

## Setup Instructions

### 1. Supabase Configuration

The app is already configured with:
- **Base URL**: `https://qdeetwuxlqmijwkbolof.supabase.co/`
- **API Key**: Already embedded in `RetrofitClient.java`

### 2. Database Setup

1. Go to your Supabase project dashboard
2. Navigate to SQL Editor
3. Run the database schema provided above
4. Enable Row Level Security (RLS) policies if needed:

```sql
-- Enable RLS
alter table public.products enable row level security;
alter table public.orders enable row level security;
alter table public.order_items enable row level security;

-- Allow authenticated users to read all products
create policy "Allow authenticated read products"
  on public.products for select
  to authenticated
  using (true);

-- Allow authenticated users to manage products
create policy "Allow authenticated manage products"
  on public.products for all
  to authenticated
  using (true);

-- Similar policies for orders and order_items
create policy "Allow authenticated manage orders"
  on public.orders for all
  to authenticated
  using (true);

create policy "Allow authenticated manage order_items"
  on public.order_items for all
  to authenticated
  using (true);
```

### 3. FLOWCHART
<img width="1128" height="632" alt="Screenshot 2025-11-01 183347" src="https://github.com/user-attachments/assets/cab47545-086f-4237-9c4b-1e104fea8f14" />

### 4.Desain DataBase
<img width="851" height="636" alt="Screenshot 2025-11-02 231104" src="https://github.com/user-attachments/assets/febc1895-566e-4765-853a-3388d8848388" />


### 5. Build and Run

1. Open the project in Android Studio Chipmunk
2. Sync Gradle files
3. Connect your Vivo Y22 device or use an emulator
4. Run the application

### 6. First Time Setup

1. Launch the app
2. Click "Sign Up" to create a new account
3. Enter email and password (minimum 6 characters)
4. After successful signup, login with your credentials
5. Start adding products and processing transactions

## Usage Guide

### Products Management
1. Navigate to **Products** tab
2. Click the **+** FAB button to add a new product
3. Fill in product details (name, price, stock, image URL)
4. Use Edit/Delete buttons on each product card to manage products

### Processing Transactions
1. Navigate to **Transaction** tab
2. Select products from the horizontal list
3. Products are added to cart automatically
4. Adjust quantities using +/- buttons
5. Enter customer name (optional)
6. Enter cash amount
7. System calculates change automatically
8. Click **Process Transaction** to complete

### Viewing History
1. Navigate to **History** tab
2. View all past transactions
3. Click on any order to see detailed information

### Profile
1. Navigate to **Profile** tab
2. View your logged-in email
3. Click **Logout** to sign out

## API Endpoints Used

### Authentication
- `POST /auth/v1/signup` - Create new user
- `POST /auth/v1/token?grant_type=password` - Login user

### Products
- `GET /rest/v1/products` - Get all products
- `POST /rest/v1/products` - Create product
- `PATCH /rest/v1/products?id=eq.{id}` - Update product
- `DELETE /rest/v1/products?id=eq.{id}` - Delete product

### Orders
- `GET /rest/v1/orders?order=created_at.desc` - Get all orders
- `POST /rest/v1/orders` - Create order

### Order Items
- `GET /rest/v1/order_items?order_id=eq.{id}` - Get order items
- `POST /rest/v1/order_items` - Create order item

## Dependencies

```gradle
// Retrofit for API calls
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.9.3'

// Glide for image loading
implementation 'com.github.bumptech.glide:glide:4.15.1'

// Navigation components
implementation 'androidx.navigation:navigation-fragment:2.5.3'
implementation 'androidx.navigation:navigation-ui:2.5.3'

// Material Design
implementation 'com.google.android.material:material:1.9.0'
```

## Troubleshooting

### Connection Issues
- Ensure device has internet connection
- Check if Supabase URL is accessible
- Verify API key is correct

### Authentication Errors
- Make sure Supabase Authentication is enabled
- Check email format is valid
- Password must be at least 6 characters

### Product Images Not Loading
- Ensure image URLs are publicly accessible
- Use HTTPS URLs for images
- Check Glide is properly configured

## License

This project is created for educational purposes.

## Support

For issues or questions, please check:
- Supabase Documentation: https://supabase.com/docs
- Retrofit Documentation: https://square.github.io/retrofit/
- Android Developer Guide: https://developer.android.com/
