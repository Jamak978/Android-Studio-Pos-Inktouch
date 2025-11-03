# Panduan Fitur Export Laporan

## Fitur yang Ditambahkan

### 1. Export ke Excel (.xlsx)
Fitur untuk mengekspor semua data transaksi ke file Excel dengan format yang rapi dan profesional.

### 2. Share via WhatsApp
Fitur untuk membagikan laporan transaksi dalam format text melalui WhatsApp.

## Cara Menggunakan

### Export ke Excel

1. **Buka halaman History** di aplikasi
2. **Klik tombol "Excel"** (hijau) di pojok kanan atas
3. **Izinkan permission storage** jika diminta
4. File Excel akan disimpan di: `Documents/InkTouch/Laporan_Transaksi_[timestamp].xlsx`
5. Anda akan mendapat notifikasi dengan opsi untuk membuka file

### Share via WhatsApp

1. **Buka halaman History** di aplikasi
2. **Klik tombol "WA"** (hijau WhatsApp) di pojok kanan atas
3. WhatsApp akan terbuka dengan laporan yang sudah terformat
4. **Pilih kontak atau grup** untuk mengirim laporan
5. **Kirim** pesan

## Format Laporan Excel

File Excel yang dihasilkan berisi:

| No | Nomor Order | Tanggal | Customer | Items | Subtotal | Tunai | Kembalian |
|----|-------------|---------|----------|-------|----------|-------|-----------|
| 1  | ORD-001     | 15/10/2025 | John | Product A x2 | Rp 50,000 | Rp 100,000 | Rp 50,000 |

**Fitur Excel:**
- ✅ Header dengan styling (bold, background abu-abu)
- ✅ Border pada semua cell
- ✅ Format currency untuk harga (Rp)
- ✅ Auto-size columns
- ✅ Summary row dengan total pendapatan
- ✅ Nama file dengan timestamp

## Format Laporan WhatsApp

Laporan WhatsApp berisi:

```
*LAPORAN TRANSAKSI INKTOUCH*
================================

*Order #1*
No. Order: ORD-001
Tanggal: 15/10/2025 14:30
Customer: John Doe
Items:
  - Product A x2 @ Rp 25.000
  - Product B x1 @ Rp 15.000
Subtotal: Rp 65.000
--------------------------------

*RINGKASAN*
Total Transaksi: 5
Total Pendapatan: Rp 325.000

_Laporan dibuat oleh InkTouch POS_
```

## Permissions yang Diperlukan

### Android 11 ke atas (API 30+)
- `MANAGE_EXTERNAL_STORAGE` - Untuk menyimpan file Excel
- Aplikasi akan meminta izin "Manage All Files" di Settings

### Android 10 ke bawah
- `WRITE_EXTERNAL_STORAGE` - Untuk menyimpan file
- `READ_EXTERNAL_STORAGE` - Untuk membaca file
- Permission akan diminta otomatis saat pertama kali export

## Lokasi File

File Excel disimpan di:
```
/storage/emulated/0/Documents/InkTouch/Laporan_Transaksi_[timestamp].xlsx
```

Contoh:
```
/storage/emulated/0/Documents/InkTouch/Laporan_Transaksi_20251015_143025.xlsx
```

## Dependencies yang Ditambahkan

```gradle
// Apache POI for Excel export
implementation 'org.apache.poi:poi:5.2.3'
implementation 'org.apache.poi:poi-ooxml:5.2.3'
```

## File yang Dimodifikasi/Dibuat

### File Baru:
1. `ExcelExporter.java` - Utility class untuk export Excel dan generate WhatsApp message
2. `file_paths.xml` - FileProvider configuration

### File Dimodifikasi:
1. `build.gradle` - Menambahkan dependency Apache POI
2. `AndroidManifest.xml` - Menambahkan permissions dan FileProvider
3. `fragment_history.xml` - Menambahkan tombol Export dan Share
4. `HistoryFragment.java` - Menambahkan logic export dan share

## Troubleshooting

### File tidak tersimpan
- Pastikan permission storage sudah diberikan
- Cek apakah folder Documents/InkTouch bisa dibuat
- Untuk Android 11+, pastikan "All files access" diizinkan

### WhatsApp tidak terbuka
- Pastikan WhatsApp terinstall di device
- Cek apakah WhatsApp sudah diupdate ke versi terbaru

### File Excel tidak bisa dibuka
- Install aplikasi Excel (Microsoft Excel, WPS Office, Google Sheets)
- File format: .xlsx (Excel 2007+)

### Permission ditolak
- Buka Settings > Apps > InkTouch > Permissions
- Aktifkan Storage/Files permission

## Tips Penggunaan

1. **Export secara berkala** untuk backup data transaksi
2. **Share via WhatsApp** untuk laporan harian ke owner/manager
3. **Buka file Excel** di Google Sheets untuk analisis lebih lanjut
4. **Simpan file Excel** di cloud storage (Google Drive, Dropbox) untuk keamanan

## Fitur Mendatang (Opsional)

- [ ] Filter laporan berdasarkan tanggal
- [ ] Export ke PDF
- [ ] Email laporan
- [ ] Grafik penjualan di Excel
- [ ] Export per produk
- [ ] Scheduled automatic export
