# Panduan Instalasi Logo InkTouch

## Langkah-langkah:

1. **Simpan gambar logo** yang telah Anda upload ke folder drawable:
   - Buka folder: `app/src/main/res/drawable/`
   - **HAPUS** file `logo_inktouch.xml` (file placeholder sementara)
   - Simpan gambar logo dengan nama: `logo_inktouch.png`

2. **Format gambar yang disarankan:**
   - Format: PNG dengan background transparan (atau putih)
   - Ukuran: Lebar sekitar 400-600px, tinggi sekitar 160-240px
   - Rasio: Sekitar 2.5:1 (lebar:tinggi)

3. **Cara menyimpan gambar:**
   
   **Opsi A - Manual:**
   - Copy gambar logo dari chat/download
   - Paste ke folder `app/src/main/res/drawable/`
   - Rename menjadi `logo_inktouch.png`
   
   **Opsi B - Melalui Android Studio:**
   - Klik kanan pada folder `drawable` di Android Studio
   - Pilih `Show in Explorer` (Windows) atau `Reveal in Finder` (Mac)
   - Copy-paste gambar logo ke folder tersebut
   - Rename menjadi `logo_inktouch.png`

4. **Rebuild project:**
   - Di Android Studio, pilih `Build > Clean Project`
   - Kemudian `Build > Rebuild Project`

## Perubahan yang telah dibuat:

✅ Layout login (`activity_login.xml`) sudah diupdate dengan ImageView untuk logo
✅ Logo akan muncul di bagian atas halaman login, menggantikan text "InkTouch"
✅ Ukuran logo: 200dp x 80dp dengan scaleType fitCenter

## Catatan:

- Logo akan ditampilkan dengan latar belakang primary color (biru)
- Pastikan logo memiliki kontras yang baik dengan background biru
- Jika logo memiliki background putih, itu akan terlihat bagus di halaman login
