# InkTouch - Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Step 1: Setup Supabase Database (2 minutes)

1. Go to your Supabase project: https://qdeetwuxlqmijwkbolof.supabase.co
2. Click **SQL Editor** in the left sidebar
3. Click **New Query**
4. Copy and paste this SQL:

```sql
-- Create Products Table
create table public.products (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  price numeric(12,2) not null,
  stock integer not null default 0,
  image_url text,
  created_at timestamp with time zone default now()
);

-- Create Orders Table
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

-- Create Order Items Table
create table public.order_items (
  id uuid primary key default gen_random_uuid(),
  order_id uuid not null references public.orders(id) on delete cascade,
  product_id uuid not null references public.products(id),
  qty integer not null check (qty > 0),
  price numeric(12,2) not null
);

-- Create Indexes
create index on public.products (name);
create index on public.orders (created_at);
create index on public.order_items (order_id);

-- Enable Row Level Security
alter table public.products enable row level security;
alter table public.orders enable row level security;
alter table public.order_items enable row level security;

-- Create Policies (Allow authenticated users full access)
create policy "Allow authenticated users" on public.products for all to authenticated using (true);
create policy "Allow authenticated users" on public.orders for all to authenticated using (true);
create policy "Allow authenticated users" on public.order_items for all to authenticated using (true);
```

5. Click **Run** or press `Ctrl+Enter`
6. You should see "Success. No rows returned"

### Step 2: Build the App (1 minute)

1. Open Android Studio
2. Click **File → Sync Project with Gradle Files**
3. Wait for Gradle sync to complete
4. You should see "BUILD SUCCESSFUL"

### Step 3: Run on Device (1 minute)

1. Connect your Vivo Y22 via USB
2. Enable **Developer Options** and **USB Debugging** on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"
3. Click the **Run** button (green play icon) in Android Studio
4. Select your device from the list
5. Click **OK**

### Step 4: Create Your Account (30 seconds)

1. App launches on Login screen
2. Click **Sign Up** link at the bottom
3. Enter your email (e.g., `admin@inktouch.com`)
4. Enter password (minimum 6 characters, e.g., `admin123`)
5. Click **SIGN UP**
6. After success message, click **Login** link
7. Enter the same credentials
8. Click **LOGIN**

### Step 5: Add Your First Product (30 seconds)

1. You're now on the **Products** screen
2. Click the **+** (FAB) button at bottom-right
3. Fill in the form:
   - **Name**: Coffee
   - **Price**: 25000
   - **Stock**: 50
   - **Image URL**: https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400
4. Click **SAVE**
5. Your first product appears!

### Step 6: Make Your First Sale (1 minute)

1. Tap **Transaction** tab at the bottom
2. Click on the **Coffee** product in the horizontal list
3. It's added to the cart below
4. Adjust quantity if needed using +/- buttons
5. Enter customer name (optional): `John Doe`
6. Enter cash amount: `30000`
7. Change is calculated automatically: `Rp 5,000`
8. Click **PROCESS TRANSACTION**
9. Success! 🎉

### Step 7: View Transaction History

1. Tap **History** tab at the bottom
2. See your transaction listed
3. Click on it to view details
4. See all items, amounts, and customer info

### Step 8: Check Your Profile

1. Tap **Profile** tab at the bottom
2. See your email displayed
3. Click **LOGOUT** when you're done

## 🎯 Sample Data for Testing

Add these products to test the app:

### Product 1: Coffee
- Name: `Coffee`
- Price: `25000`
- Stock: `50`
- Image: `https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400`

### Product 2: Tea
- Name: `Tea`
- Price: `15000`
- Stock: `30`
- Image: `https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400`

### Product 3: Sandwich
- Name: `Sandwich`
- Price: `35000`
- Stock: `20`
- Image: `https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400`

### Product 4: Juice
- Name: `Orange Juice`
- Price: `20000`
- Stock: `40`
- Image: `https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=400`

### Product 5: Cake
- Name: `Chocolate Cake`
- Price: `45000`
- Stock: `15`
- Image: `https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400`

## 🔧 Troubleshooting

### App won't build?
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

### Can't connect to Supabase?
- Check internet connection
- Verify Supabase project is active
- Check API key in `RetrofitClient.java`

### Login fails?
- Make sure you ran the SQL schema
- Check RLS policies are created
- Try signing up with a new email

### Products not loading?
- Check you're logged in
- Verify products exist in database
- Check Supabase dashboard → Table Editor → products

### Images not showing?
- Use HTTPS URLs only
- Test URL in browser first
- Use Unsplash or similar image hosting

## 📱 App Navigation

```
Login Screen
    ↓
Main Screen (Bottom Navigation)
    ├── Products Tab
    │   ├── View all products
    │   ├── Add new product (FAB)
    │   ├── Edit product (Edit icon)
    │   └── Delete product (Delete icon)
    │
    ├── Transaction Tab
    │   ├── Select products (horizontal scroll)
    │   ├── Manage cart (add/remove/adjust)
    │   ├── Enter customer name
    │   ├── Enter cash amount
    │   └── Process transaction
    │
    ├── History Tab
    │   ├── View all orders
    │   └── Click to see details
    │
    └── Profile Tab
        ├── View email
        └── Logout
```

## 🎨 UI Features

- **Material Design**: Modern, clean interface
- **Bottom Navigation**: Easy access to all features
- **Floating Action Button**: Quick add product
- **Cards**: Beautiful product and order cards
- **Real-time Calculations**: Instant subtotal and change
- **Image Loading**: Smooth image loading with Glide
- **Responsive**: Works on all screen sizes

## 💡 Tips

1. **Add products first** before making transactions
2. **Use clear product names** for easy identification
3. **Keep stock updated** to avoid overselling
4. **Use high-quality images** for better presentation
5. **Enter customer names** for better record keeping
6. **Check history regularly** to track sales

## 🎉 You're Ready!

Your InkTouch POS app is now fully functional and ready to use!

**Happy Selling! 💰**

---

**Need help?** Check:
- `README.md` - Full documentation
- `PROJECT_STRUCTURE.md` - Complete file structure
- Supabase Docs: https://supabase.com/docs
- Android Docs: https://developer.android.com
