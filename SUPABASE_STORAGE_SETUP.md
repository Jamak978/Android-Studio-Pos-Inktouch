# Setup Supabase Storage untuk Upload Gambar

## Langkah 1: Buat Storage Bucket

1. Buka Supabase Dashboard: https://qdeetwuxlqmijwkbolof.supabase.co
2. Klik **Storage** di sidebar kiri
3. Klik **New Bucket**
4. Isi form:
   - **Name**: `products`
   - **Public bucket**: ✅ Centang (agar gambar bisa diakses publik)
5. Klik **Create Bucket**

## Langkah 2: Set Storage Policies

1. Setelah bucket dibuat, klik bucket **products**
2. Klik tab **Policies**
3. Klik **New Policy**

### Policy 1: Allow Authenticated Upload
```sql
CREATE POLICY "Allow authenticated users to upload"
ON storage.objects
FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'products');
```

### Policy 2: Allow Public Read
```sql
CREATE POLICY "Allow public read access"
ON storage.objects
FOR SELECT
TO public
USING (bucket_id = 'products');
```

### Policy 3: Allow Authenticated Delete
```sql
CREATE POLICY "Allow authenticated users to delete"
ON storage.objects
FOR DELETE
TO authenticated
USING (bucket_id = 'products');
```

## Langkah 3: Verifikasi Setup

1. Klik **Storage** → **products**
2. Pastikan bucket sudah dibuat
3. Pastikan **Public** badge muncul di bucket

## Cara Menggunakan di App

### Upload Gambar Produk:
1. Buka app InkTouch
2. Tap **Products** tab
3. Tap tombol **+** (FAB)
4. Isi nama, harga, dan stock
5. Tap **Pilih Gambar**
6. Pilih gambar dari galeri
7. Preview gambar akan muncul
8. Tap **SAVE**
9. Gambar akan diupload ke Supabase Storage
10. URL gambar otomatis disimpan ke database

### Edit Produk dengan Gambar Baru:
1. Tap tombol **Edit** pada produk
2. Gambar lama akan ditampilkan
3. Tap **Pilih Gambar** untuk ganti gambar
4. Pilih gambar baru
5. Tap **SAVE**
6. Gambar baru akan diupload dan menggantikan URL lama

## Format URL Gambar

Setelah upload, gambar akan tersimpan dengan format:
```
https://qdeetwuxlqmijwkbolof.supabase.co/storage/v1/object/public/products/product_1234567890.jpg
```

## Troubleshooting

### Upload Gagal?
- Pastikan bucket **products** sudah dibuat
- Pastikan bucket di-set sebagai **Public**
- Pastikan policies sudah dibuat
- Cek koneksi internet
- Cek permission READ_EXTERNAL_STORAGE di device

### Gambar Tidak Muncul?
- Pastikan URL gambar valid
- Cek di Supabase Storage apakah file ter-upload
- Pastikan policy "Allow public read access" aktif

### Permission Denied?
- Buka Settings → Apps → InkTouch
- Berikan permission **Storage** atau **Photos and Media**
- Restart app

## Keuntungan Upload vs URL

### Sebelumnya (Input URL):
- ❌ Harus cari URL gambar dari internet
- ❌ URL bisa expired atau broken
- ❌ Tidak ada kontrol atas gambar
- ❌ Tergantung server eksternal

### Sekarang (Upload Gambar):
- ✅ Pilih gambar langsung dari galeri
- ✅ Gambar tersimpan di Supabase Storage
- ✅ Full kontrol atas gambar
- ✅ Tidak tergantung server eksternal
- ✅ Lebih cepat dan mudah
- ✅ Preview gambar sebelum save

## Catatan Penting

1. **Ukuran File**: Maksimal 50MB per file (default Supabase)
2. **Format**: Mendukung JPG, PNG, GIF, WebP
3. **Nama File**: Otomatis generate dengan timestamp untuk menghindari duplikasi
4. **Storage Limit**: Free tier Supabase = 1GB storage
5. **Bandwidth**: Free tier = 2GB bandwidth per bulan

## SQL untuk Cleanup (Opsional)

Jika ingin hapus semua gambar di storage:
```sql
-- Hati-hati! Ini akan menghapus semua file di bucket products
DELETE FROM storage.objects WHERE bucket_id = 'products';
```

---

**Setup selesai! Sekarang app sudah bisa upload gambar produk! 📸**
