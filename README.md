# Bioskop-Reservation-Applications
BAD_BB82_Bioskop Reservation Applications

Deskripsi singkat :
Reservasi Tempat Duduk Bioskop adalah aplikasi manajemen reservasi tiket bioskop sederhana berbasis javaFx yang memungkinkan penggunanya untuk melakukan pemesanan tiket film, memilih kursi yang tersedia, mengganti data pemesanan, dan membatalkan pemesanan tiket. Aplikasi ini dirancang untuk memberikan kemudahan dalam melakukan pemesanan tiket dan mengurangi antrian di loket bioskop.

Fitur utama: 
1. Formulir Reservasi
 Input data nama pelanggan.
 Pilih film dan jadwal tayang.
 Pilih kursi yang diinginkan, Kursi dengan status tersedia akan berwarna hijau. Kursi yang sudah dipesan akan berwarna merah dan tidak bisa dipilih.
 Validasi input agar data tidak kosong.

2. Reservasi yang dilakukan akan disimpan ke database MySQL melalui koneksi JDBC.
 
3. Update Data Reservasi dan Hapus Data Reservasi
 Riwayat reservasi bisa ditampilkan dan dilihat
 Data reservasi bisa diupdate untuk mengganti data. Satu reservasi hanya bisa mengupdate satu data.
 Data reservasi juga bisa dihapus

3. Cari data reservasi
 Data reservasi bisa dicari berdasarkan nama atau berdasarkan film

4. Notifikasi & Alert
Menampilkan notifikasi jika reservasi berhasil atau gagal.
Alert konfirmasi saat ingin menghapus data.
