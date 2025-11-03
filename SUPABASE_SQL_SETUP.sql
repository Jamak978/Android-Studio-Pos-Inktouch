-- ============================================
-- INKTOUCH POS - SUPABASE SQL SETUP
-- Project URL: https://qdeetwuxlqmijwkbolof.supabase.co
-- ============================================

-- 1. CREATE TABLES
-- ============================================

-- Products Table
CREATE TABLE IF NOT EXISTS public.products (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  price NUMERIC(12,2) NOT NULL,
  stock INTEGER NOT NULL DEFAULT 0,
  image_url TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Orders Table
CREATE TABLE IF NOT EXISTS public.orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_number TEXT UNIQUE NOT NULL,
  customer_name TEXT,
  subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
  cash NUMERIC(12,2) NOT NULL DEFAULT 0,
  change NUMERIC(12,2) NOT NULL DEFAULT 0,
  user_id UUID NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Order Items Table
CREATE TABLE IF NOT EXISTS public.order_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
  product_id UUID NOT NULL REFERENCES public.products(id),
  qty INTEGER NOT NULL CHECK (qty > 0),
  price NUMERIC(12,2) NOT NULL
);

-- 2. CREATE INDEXES
-- ============================================

CREATE INDEX IF NOT EXISTS idx_products_name ON public.products (name);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON public.orders (created_at);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON public.orders (user_id);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON public.order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON public.order_items (product_id);

-- 3. ENABLE ROW LEVEL SECURITY (RLS)
-- ============================================

ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

-- 4. CREATE RLS POLICIES
-- ============================================

-- Products Policies
DROP POLICY IF EXISTS "Allow authenticated users to read products" ON public.products;
CREATE POLICY "Allow authenticated users to read products"
  ON public.products FOR SELECT
  TO authenticated
  USING (true);

DROP POLICY IF EXISTS "Allow authenticated users to insert products" ON public.products;
CREATE POLICY "Allow authenticated users to insert products"
  ON public.products FOR INSERT
  TO authenticated
  WITH CHECK (true);

DROP POLICY IF EXISTS "Allow authenticated users to update products" ON public.products;
CREATE POLICY "Allow authenticated users to update products"
  ON public.products FOR UPDATE
  TO authenticated
  USING (true);

DROP POLICY IF EXISTS "Allow authenticated users to delete products" ON public.products;
CREATE POLICY "Allow authenticated users to delete products"
  ON public.products FOR DELETE
  TO authenticated
  USING (true);

-- Orders Policies
DROP POLICY IF EXISTS "Allow authenticated users to read orders" ON public.orders;
CREATE POLICY "Allow authenticated users to read orders"
  ON public.orders FOR SELECT
  TO authenticated
  USING (true);

DROP POLICY IF EXISTS "Allow authenticated users to insert orders" ON public.orders;
CREATE POLICY "Allow authenticated users to insert orders"
  ON public.orders FOR INSERT
  TO authenticated
  WITH CHECK (true);

DROP POLICY IF EXISTS "Allow authenticated users to update orders" ON public.orders;
CREATE POLICY "Allow authenticated users to update orders"
  ON public.orders FOR UPDATE
  TO authenticated
  USING (true);

DROP POLICY IF EXISTS "Allow authenticated users to delete orders" ON public.orders;
CREATE POLICY "Allow authenticated users to delete orders"
  ON public.orders FOR DELETE
  TO authenticated
  USING (true);

-- Order Items Policies
DROP POLICY IF EXISTS "Allow authenticated users to read order_items" ON public.order_items;
CREATE POLICY "Allow authenticated users to read order_items"
  ON public.order_items FOR SELECT
  TO authenticated
  USING (true);

DROP POLICY IF EXISTS "Allow authenticated users to insert order_items" ON public.order_items;
CREATE POLICY "Allow authenticated users to insert order_items"
  ON public.order_items FOR INSERT
  TO authenticated
  WITH CHECK (true);

DROP POLICY IF EXISTS "Allow authenticated users to update order_items" ON public.order_items;
CREATE POLICY "Allow authenticated users to update order_items"
  ON public.order_items FOR UPDATE
  TO authenticated
  USING (true);

DROP POLICY IF EXISTS "Allow authenticated users to delete order_items" ON public.order_items;
CREATE POLICY "Allow authenticated users to delete order_items"
  ON public.order_items FOR DELETE
  TO authenticated
  USING (true);

-- 5. STORAGE BUCKET SETUP
-- ============================================

-- Create storage bucket for product images
INSERT INTO storage.buckets (id, name, public)
VALUES ('products', 'products', true)
ON CONFLICT (id) DO NOTHING;

-- Storage Policies
DROP POLICY IF EXISTS "Allow authenticated upload" ON storage.objects;
CREATE POLICY "Allow authenticated upload"
  ON storage.objects FOR INSERT
  TO authenticated
  WITH CHECK (bucket_id = 'products');

DROP POLICY IF EXISTS "Allow public read access" ON storage.objects;
CREATE POLICY "Allow public read access"
  ON storage.objects FOR SELECT
  TO public
  USING (bucket_id = 'products');

DROP POLICY IF EXISTS "Allow authenticated delete" ON storage.objects;
CREATE POLICY "Allow authenticated delete"
  ON storage.objects FOR DELETE
  TO authenticated
  USING (bucket_id = 'products');

-- 6. INSERT SAMPLE DATA (Optional)
-- ============================================

INSERT INTO public.products (name, price, stock, image_url) VALUES
  ('Coffee', 25000, 50, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400'),
  ('Tea', 15000, 30, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400'),
  ('Sandwich', 35000, 20, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400'),
  ('Orange Juice', 20000, 40, 'https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=400'),
  ('Chocolate Cake', 45000, 15, 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400')
ON CONFLICT DO NOTHING;

-- ============================================
-- SETUP COMPLETE!
-- ============================================

-- Verify tables created
SELECT 'Tables created successfully!' AS status;
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('products', 'orders', 'order_items');

-- Verify RLS enabled
SELECT tablename, rowsecurity 
FROM pg_tables 
WHERE schemaname = 'public' 
AND tablename IN ('products', 'orders', 'order_items');

-- Count sample products
SELECT COUNT(*) as total_products FROM public.products;

-- ============================================
-- INSTRUCTIONS:
-- ============================================
-- 1. Login to: https://qdeetwuxlqmijwkbolof.supabase.co
-- 2. Click "SQL Editor" in sidebar
-- 3. Click "New Query"
-- 4. Copy and paste this entire file
-- 5. Click "Run" or press Ctrl+Enter
-- 6. Wait for completion (5-10 seconds)
-- 7. Verify in Table Editor that tables exist
-- 8. Create storage bucket 'products' via Storage UI if not auto-created
-- ============================================
