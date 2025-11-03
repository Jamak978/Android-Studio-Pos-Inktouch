# Panduan Instalasi Icon Aplikasi InkTouch

## Status Saat Ini
✅ Background icon sudah diubah menjadi putih
✅ Foreground icon sudah dibuat dengan desain InkTouch sederhana
⚠️ Untuk hasil terbaik, gunakan gambar logo asli

## Cara Terbaik: Menggunakan Android Studio Image Asset Studio

### Langkah 1: Siapkan Logo
1. Simpan gambar logo InkTouch yang Anda upload
2. Pastikan format PNG dengan background transparan atau putih
3. Ukuran minimal: 512x512 pixels (untuk hasil terbaik)

### Langkah 2: Buat Icon dengan Image Asset Studio
1. Di Android Studio, klik kanan pada folder `res`
2. Pilih **New > Image Asset**
3. Pada jendela yang muncul:
   - **Icon Type**: Launcher Icons (Adaptive and Legacy)
   - **Name**: ic_launcher
   - **Foreground Layer**:
     - Source Asset: Image
     - Path: Browse dan pilih logo InkTouch Anda
     - Trim: Yes
     - Resize: Sesuaikan agar logo terlihat proporsional (biasanya 60-80%)
   - **Background Layer**:
     - Source Asset: Color
     - Color: #FFFFFF (putih)
   - **Legacy**:
     - Centang "Generate Legacy Icon"
     - Centang "Generate Round Icon"

4. Klik **Next** lalu **Finish**

### Langkah 3: Hasil
Image Asset Studio akan otomatis membuat semua ukuran icon:
- `mipmap-mdpi/ic_launcher.png` (48x48)
- `mipmap-hdpi/ic_launcher.png` (72x72)
- `mipmap-xhdpi/ic_launcher.png` (96x96)
- `mipmap-xxhdpi/ic_launcher.png` (144x144)
- `mipmap-xxxhdpi/ic_launcher.png` (192x192)
- Plus versi round untuk setiap ukuran

## Cara Alternatif: Manual (Jika tidak menggunakan Android Studio)

### Ukuran Icon yang Dibutuhkan:

Anda perlu membuat logo dalam berbagai ukuran dan menyimpannya di folder yang sesuai:

1. **mipmap-mdpi** (48x48 pixels)
2. **mipmap-hdpi** (72x72 pixels)
3. **mipmap-xhdpi** (96x96 pixels)
4. **mipmap-xxhdpi** (144x144 pixels)
5. **mipmap-xxxhdpi** (192x192 pixels)

### Nama File:
- `ic_launcher.png` - Icon persegi
- `ic_launcher_round.png` - Icon bulat

### Langkah Manual:
1. Resize logo ke ukuran yang sesuai menggunakan image editor (Photoshop, GIMP, dll)
2. Export sebagai PNG dengan background putih
3. Copy file ke folder mipmap yang sesuai
4. Ganti file `.webp` yang ada dengan file `.png` baru Anda

## Verifikasi

Setelah mengganti icon:
1. **Clean Project**: Build > Clean Project
2. **Rebuild Project**: Build > Rebuild Project
3. **Uninstall app** dari device/emulator
4. **Run app** lagi untuk melihat icon baru

## Tips untuk Logo yang Bagus

1. **Kesederhanaan**: Logo harus terlihat jelas di ukuran kecil
2. **Kontras**: Pastikan logo memiliki kontras yang baik dengan background
3. **Safe Zone**: Jangan letakkan elemen penting di tepi, karena Android akan crop untuk adaptive icon
4. **Testing**: Test di berbagai launcher (Google Launcher, Samsung, dll) karena bentuk icon bisa berbeda

## Troubleshooting

**Q: Icon tidak berubah setelah install ulang?**
A: Hapus app data dan cache, atau restart device

**Q: Icon terlihat terpotong?**
A: Kurangi resize percentage di Image Asset Studio (coba 60-70%)

**Q: Icon blur/pixelated?**
A: Gunakan gambar sumber dengan resolusi lebih tinggi (minimal 512x512)

## File yang Sudah Dimodifikasi

- ✅ `drawable/ic_launcher_background.xml` - Background putih
- ✅ `drawable/ic_launcher_foreground.xml` - Logo InkTouch sederhana
- ⚠️ File di folder `mipmap-*` masih menggunakan icon default (perlu diganti manual atau via Image Asset Studio)
