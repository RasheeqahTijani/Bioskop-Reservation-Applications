	
	package GUI;
	import javafx.application.Application;
	import javafx.geometry.Insets;
	import javafx.scene.Scene;
	import javafx.scene.control.*;
	import javafx.scene.layout.*;
	import javafx.stage.Stage;
	import java.sql.*;
	import java.time.LocalDate;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.stream.Collectors;
	import DB.Connector;
		
		public class BioskopStage extends Stage {
			//komponen GUI
		    ComboBox<String> filmCombo, studioCombo;
		    DatePicker tanggalPicker;
		    ComboBox<String> jamCombo;
		    TextField namaField;
		    GridPane kursiPane;
		    Button reservasiBtn, kembaliBtn;
		    List<Button> kursiButtons = new ArrayList<>();
		    Connection conn;
		    
		    //buat ngedit
		    private boolean isEditMode = false;
		    private Booking existingBooking;
	
		
		    //ini buat ngatur UI & Logic
		    public BioskopStage() {
		        connectDB();
	
		        VBox root = new VBox(10);
		        root.setPadding(new Insets(10));
	
		        //Baris 1: Film & Tanggal
		        HBox atasBox = new HBox(10);
		        filmCombo = new ComboBox<>();
		        loadFilm();
		        tanggalPicker = new DatePicker();
		        atasBox.getChildren().addAll(
		            new Label("Film:"), filmCombo,
		            new Label("Tanggal:"), tanggalPicker
		        );
	
		        // Baris 2: Jam, Studio, Lihat Kursi
		        HBox tengahBox = new HBox(10);
		        jamCombo = new ComboBox<>();
		        loadJamComboBox();
		        studioCombo = new ComboBox<>();
		        loadStudio();
		        Button lihatKursiBtn = new Button("Lihat Kursi");
		        lihatKursiBtn.setOnAction(e -> loadKursi());
	
		        tengahBox.getChildren().addAll(
		            new Label("Jam:"), jamCombo,
		            new Label("Studio:"), studioCombo,
		            lihatKursiBtn
		        );
	
		        //Baris 3: Pane Kursi dengan ScrollPane dan pembungkus HBox untuk geser kanan
		        kursiPane = new GridPane();
		        kursiPane.setHgap(5);
		        kursiPane.setVgap(5);
	
		        // Set ukuran preferensi grid kursi
		        kursiPane.setPrefWidth(400);
		        kursiPane.setPrefHeight(400);
	
		        ScrollPane kursiScroll = new ScrollPane(kursiPane);
		        kursiScroll.setFitToWidth(true);
		        kursiScroll.setPrefViewportWidth(600);
		        kursiScroll.setPrefViewportHeight(400);
		        kursiScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		        kursiScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
	
		        // Bungkus ScrollPane dalam HBox untuk memberi padding kiri (geser kanan)
		        HBox kursiWrapper = new HBox(kursiScroll);
		        kursiWrapper.setPadding(new Insets(5, 5, 5, 5)); 
	
		        // Baris 4: Nama & Reservasi
		        HBox bawahBox = new HBox(10);
		        namaField = new TextField();
		        namaField.setPromptText("Nama Pemesan");
		        reservasiBtn = new Button("Reservasi");
		        reservasiBtn.setOnAction(e -> reservasiKursi());
		        
		     	// Tombol Kembali
		        kembaliBtn = new Button("Kembali");
		        kembaliBtn.setOnAction(e -> {
		            this.close();  // tutup window bioskop
	
		            MainMenuStage mainMenu = new MainMenuStage();
		            mainMenu.show();
		        });
	
		        //masukkan semuanya ke bawahBox
		        bawahBox.getChildren().addAll(
		        		new Label("Nama:"), namaField, reservasiBtn, kembaliBtn
		        		);
		        
		        
		        //Masukkan semua ke root VBox
		        root.getChildren().addAll(
		            atasBox,
		            tengahBox,
		            kursiWrapper,  // pakai pembungkus HBox untuk geser kanan
		            bawahBox
		        );
		        
		        
		        Scene scene = new Scene(root, 500, 500);
		        setScene(scene);
		        setTitle("Reservasi Kursi Bioskop");
		    }
		      
		    void connectDB() {
		        conn = Connector.getConnection();
		    }
		    
		    //method untuk konek ke MySQL
		    public Connection getConnection() {
		        try {
		            Class.forName("com.mysql.cj.jdbc.Driver");
		            return DriverManager.getConnection("jdbc:mysql://localhost:3306/bioskopappdb", "root", "");
		        } catch (Exception e) {
		            e.printStackTrace();
		            return null;
		        }
		    }
		    
		    //method untuk menampilkan judul film
		    void loadFilm() {
		        try {
		        	conn = getConnection();
		
		            Statement stmt = conn.createStatement();
		            ResultSet rs = stmt.executeQuery("SELECT judul_film FROM film");
		            while (rs.next()) {
		                filmCombo.getItems().add(rs.getString("judul_film"));
		            }
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
		    }
		    
		    //method untuk menampilkan jam
		    private void loadJamComboBox() {
		        try {
		        	conn = getConnection();
		            String sql = "SELECT DISTINCT jam FROM jadwal ORDER BY jam";
		            PreparedStatement stmt = conn.prepareStatement(sql);
		            ResultSet rs = stmt.executeQuery();
	
		            jamCombo.getItems().clear();  // kosongin dulu biar ngga dobel
	
		            while (rs.next()) {
		                String jam = rs.getString("jam");
		                jamCombo.getItems().add(jam);
		            }
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
		    }
		    
		    //method untuk menampilkan studio
		    void loadStudio() {
		        try (Connection conn = getConnection()) {
		            String sql = "SELECT nama_studio FROM studio ORDER BY id_studio";
		            PreparedStatement ps = conn.prepareStatement(sql);
		            ResultSet rs = ps.executeQuery();
	
		            studioCombo.getItems().clear();
		            while (rs.next()) {
		                studioCombo.getItems().add(rs.getString("nama_studio"));
		            }
	
		            // Set default pilih studio pertama kalau ada
		            if (!studioCombo.getItems().isEmpty()) {
		                studioCombo.getSelectionModel().selectFirst();
		            }
	
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
		    }
	
		    //method untuk menampilkan kursi
		    void loadKursi() {
		        kursiPane.getChildren().clear();
		        kursiButtons.clear();
	
		        try (Connection conn = getConnection()) {
		            int jadwalId = getJadwalId();
		            
		            if (jadwalId == -1) {
		                showAlert("Peringatan", "Jadwal belum lengkap atau tidak ditemukan."
		                		+ " Pastikan film, jam, tanggal, dan studio dipilih dengan benar.", Alert.AlertType.WARNING);
		                return; // stop proses load kursi
		            }
		            
		            String selectedStudioName = studioCombo.getValue();
		            int idStudio = -1;
		            String sqlStudio = "SELECT id_studio FROM studio WHERE nama_studio = ?";
		            try (PreparedStatement psStudio = conn.prepareStatement(sqlStudio)) {
		                psStudio.setString(1, selectedStudioName);
		                try (ResultSet rsStudio = psStudio.executeQuery()) {
		                    if (rsStudio.next()) {
		                        idStudio = rsStudio.getInt("id_studio");
		                    } else {
		                        throw new SQLException("Studio tidak ditemukan!");
		                    }
		                }
		            }
	
		            String sql = "SELECT k.id_kursi, k.no_kursi, " +
		                         "CASE WHEN r.id_kursi IS NOT NULL THEN 'Terpesan' ELSE 'Tersedia' END AS status " +
		                         "FROM kursi k " +
		                         "LEFT JOIN reservation r ON k.id_kursi = r.id_kursi AND r.id_jadwal = ? " +
		                         "WHERE k.id_studio = ? " +
		                         "ORDER BY LEFT(k.no_kursi, 1), CAST(SUBSTRING(k.no_kursi, 2) AS UNSIGNED)";
	
		            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
		                stmt.setInt(1, jadwalId);
		                stmt.setInt(2, idStudio);
	
		                try (ResultSet rs = stmt.executeQuery()) {
		                    kursiPane.setHgap(5);
		                    kursiPane.setVgap(5);
	
		                    int kolom = 0;
		                    int baris = 0;
		                    int maxKolom = 10; // misal per baris 10 kursi
	
		                    while (rs.next()) {
		                        int idKursi = rs.getInt("id_kursi");
		                        String nomorKursi = rs.getString("no_kursi");
		                        String status = rs.getString("status");
	
		                        Button b = new Button(nomorKursi);
		                        b.setId(String.valueOf(idKursi));
		                        b.setPrefWidth(50);
		                        b.setPrefHeight(50);
	
		                        if (status.equals("Terpesan")) {
		                            b.setStyle("-fx-background-color: #F44336");
		                            b.setDisable(true);
		                            b.setUserData(false);
		                        } else {
		                            b.setStyle("-fx-background-color: #4CAF50");
		                            b.setUserData(false);
	
		                            b.setOnAction(e -> {
		                                boolean isSelected = (boolean) b.getUserData();
		                                if (isSelected) {
		                                    b.setStyle("-fx-background-color: #4CAF50");
		                                    b.setUserData(false);
		                                } else {
		                                    b.setStyle("-fx-background-color: #FFEB3B");
		                                    b.setUserData(true);
		                                }
		                            });
		                        }
	
		                        kursiButtons.add(b);
		                        kursiPane.add(b, kolom, baris);  // masukkan ke GridPane posisi kolom, baris
	
		                        kolom++;
		                        if (kolom >= maxKolom) {
		                            kolom = 0;
		                            baris++;
		                        }
		                    }
		                }
		            }
	
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
		    }
		    
		    //bikin method pilih kursi
		    void pilihKursi(Button btn) {
		    	
		        if (!btn.getStyle().contains("#4CAF50")) {
	                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
	            }else {            	
	            	btn.setStyle("-fx-background-color: #FFEB3B; -fx-text-fill: black;");
	            }
		    }
		    
		    //cobain reservasi kursi yang bisa update 
		    void reservasiKursi() {
		        String nama = namaField.getText();
		        if (nama.isEmpty()) {
		            showAlert("Peringatan", "Nama pemesan wajib diisi!", Alert.AlertType.WARNING);
		            return;
		        }

		        if (filmCombo.getValue() == null) {
		            showAlert("Peringatan", "Pilih film terlebih dahulu!", Alert.AlertType.WARNING);
		            return;
		        }

		        if (jamCombo.getValue() == null) {
		            showAlert("Peringatan", "Pilih jam tayang terlebih dahulu!", Alert.AlertType.WARNING);
		            return;
		        }

		        if (tanggalPicker.getValue() == null) {
		            showAlert("Peringatan", "Tanggal wajib diisi!", Alert.AlertType.WARNING);
		            return;
		        }

		        if (studioCombo.getValue() == null) {
		            showAlert("Peringatan", "Pilih studio terlebih dahulu!", Alert.AlertType.WARNING);
		            return;
		        }

		        List<Button> kursiDipilih = kursiButtons.stream()
		                .filter(b -> b.getStyle().toLowerCase().contains("#ffeb3b"))
		                .collect(Collectors.toList());

		        if (kursiDipilih.isEmpty()) {
		            showAlert("Peringatan", "Pilih kursi terlebih dahulu!", Alert.AlertType.WARNING);
		            return;
		        }

		        Connection conn = null;
		        try {
		            conn = getConnection();
		            conn.setAutoCommit(false); // Mulai transaksi

		            int jadwalId = getJadwalId();
		            if (jadwalId == -1) {
		                conn.rollback();
		                return;
		            }

		            if (isEditMode && existingBooking != null) {
		                int reservationId = existingBooking.getIdReservasi();

		                // Ambil kursi existing
		                List<Integer> existingSeats = new ArrayList<>();
		                String getExistingSeatsSql = "SELECT id_kursi FROM reservation WHERE id_reservasi = ?";
		                try (PreparedStatement stmt = conn.prepareStatement(getExistingSeatsSql)) {
		                    stmt.setInt(1, reservationId);
		                    try (ResultSet rs = stmt.executeQuery()) {
		                        while (rs.next()) {
		                            existingSeats.add(rs.getInt(1));
		                        }
		                    }
		                }

		                List<Integer> idKursiDipilih = new ArrayList<>();
		                String checkSql = "SELECT COUNT(*) FROM reservation WHERE id_jadwal = ? AND id_kursi = ? AND id_reservasi != ?";
		                String insertSql = "INSERT INTO reservation (id_reservasi, id_jadwal, nama_pemesan, tanggal_pesan, id_kursi) VALUES (?, ?, ?, ?, ?)";
		                String updateSql = "UPDATE reservation SET id_jadwal = ?, nama_pemesan = ?, tanggal_pesan = ? WHERE id_reservasi = ? AND id_kursi = ?";

		                for (Button b : kursiDipilih) {
		                    int idKursi = Integer.parseInt(b.getId());

		                    // Cek ketersediaan kursi
		                    try (PreparedStatement cekStmt = conn.prepareStatement(checkSql)) {
		                        cekStmt.setInt(1, jadwalId);
		                        cekStmt.setInt(2, idKursi);
		                        cekStmt.setInt(3, reservationId);
		                        try (ResultSet rs = cekStmt.executeQuery()) {
		                            rs.next();
		                            if (rs.getInt(1) > 0) {
		                                showAlert("Peringatan", "Kursi " + b.getText() + " sudah dipesan!", Alert.AlertType.WARNING);
		                                continue;
		                            }
		                        }
		                    }

		                    idKursiDipilih.add(idKursi);

		                    if (existingSeats.contains(idKursi)) {
		                        // Update
		                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
		                            updateStmt.setInt(1, jadwalId);
		                            updateStmt.setString(2, nama);
		                            updateStmt.setDate(3, Date.valueOf(LocalDate.now()));
		                            updateStmt.setInt(4, reservationId);
		                            updateStmt.setInt(5, idKursi);
		                            updateStmt.executeUpdate();
		                        }
		                    } else {
		                        // Insert
		                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
		                            insertStmt.setInt(1, reservationId);
		                            insertStmt.setInt(2, jadwalId);
		                            insertStmt.setString(3, nama);
		                            insertStmt.setDate(4, Date.valueOf(LocalDate.now()));
		                            insertStmt.setInt(5, idKursi);
		                            insertStmt.executeUpdate();
		                        } catch (SQLIntegrityConstraintViolationException ex) {
		                            conn.rollback();
		                            Alert alert = new Alert(Alert.AlertType.ERROR);
		                            alert.setTitle("Gagal Reservasi");
		                            alert.setHeaderText("Hanya bisa mengubah satu kursi");
		                            alert.setContentText("Silakan gunakan reservasi lain atau periksa data.");
		                            alert.showAndWait();
		                            return;
		                        }
		                    }
		                }

		                // Hapus kursi yang tidak dipilih dari existing
		                List<Integer> seatsToRemove = existingSeats.stream()
		                        .filter(seat -> !idKursiDipilih.contains(seat))
		                        .collect(Collectors.toList());

		                if (!seatsToRemove.isEmpty()) {
		                    String deleteSql = "DELETE FROM reservation WHERE id_reservasi = ? AND id_kursi IN (" +
		                            seatsToRemove.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
		                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
		                        deleteStmt.setInt(1, reservationId);
		                        deleteStmt.executeUpdate();
		                    }
		                }

		                // Cek kalau tidak ada kursi dipilih, rollback
		                if (idKursiDipilih.isEmpty()) {
		                    showAlert("Peringatan", "Tidak ada kursi yang berhasil diperbarui!", Alert.AlertType.WARNING);
		                    conn.rollback();
		                    return;
		                }

		                // Sukses
		                showAlert("Sukses", "Data reservasi berhasil diperbarui!", Alert.AlertType.INFORMATION);

		            } else {
		                // Insert reservasi baru
		                int reservationId;
		                String getMaxIdSql = "SELECT COALESCE(MAX(id_reservasi), 0) + 1 FROM reservation";
		                try (Statement stmt = conn.createStatement();
		                     ResultSet rs = stmt.executeQuery(getMaxIdSql)) {
		                    reservationId = rs.next() ? rs.getInt(1) : 1;
		                }

		                String insertSql = "INSERT INTO reservation (id_reservasi, id_jadwal, nama_pemesan, tanggal_pesan, id_kursi) VALUES (?, ?, ?, ?, ?)";
		                int kursiBerhasil = 0;

		                for (Button b : kursiDipilih) {
		                    int idKursi = Integer.parseInt(b.getId());

		                    // Cek ketersediaan
		                    String cekSql = "SELECT COUNT(*) FROM reservation WHERE id_jadwal = ? AND id_kursi = ?";
		                    try (PreparedStatement cekStmt = conn.prepareStatement(cekSql)) {
		                        cekStmt.setInt(1, jadwalId);
		                        cekStmt.setInt(2, idKursi);
		                        try (ResultSet rs = cekStmt.executeQuery()) {
		                            rs.next();
		                            if (rs.getInt(1) > 0) {
		                                showAlert("Peringatan", "Kursi " + b.getText() + " sudah dipesan!", Alert.AlertType.WARNING);
		                                continue;
		                            }
		                        }
		                    }

		                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
		                        insertStmt.setInt(1, reservationId);
		                        insertStmt.setInt(2, jadwalId);
		                        insertStmt.setString(3, nama);
		                        insertStmt.setDate(4, Date.valueOf(LocalDate.now()));
		                        insertStmt.setInt(5, idKursi);
		                        insertStmt.executeUpdate();
		                        kursiBerhasil++;
		                    }
		                }

		                if (kursiBerhasil == 0) {
		                    showAlert("Peringatan", "Tidak ada kursi yang berhasil dipesan!", Alert.AlertType.WARNING);
		                    conn.rollback();
		                    return;
		                }

		                showAlert("Sukses", "Reservasi berhasil!", Alert.AlertType.INFORMATION);
		            }

		            conn.commit();
		            loadKursi();
		            namaField.clear();

		        } catch (SQLException e) {
		            if (conn != null) {
		                try {
		                    conn.rollback();
		                } catch (SQLException ex) {
		                    ex.printStackTrace();
		                }
		            }
		            e.printStackTrace();
		            showAlert("Error", "Reservasi gagal: " + e.getMessage(), Alert.AlertType.ERROR);
		            
		        } finally {
		            if (conn != null) {
		                try {
		                    conn.setAutoCommit(true);
		                    conn.close();
		                } catch (SQLException ex) {
		                    ex.printStackTrace();
		                }
		            }
		        }
		    }


		    int getJadwalId() {
		        try {
		        	if (filmCombo.getValue() == null) {
		                showAlert("Peringatan", "Silakan pilih film terlebih dahulu!", Alert.AlertType.WARNING);
		                return -1;
		            }
		        	if (tanggalPicker.getValue() == null) {
		                showAlert("Peringatan", "Silakan pilih tanggal terlebih dahulu!", Alert.AlertType.WARNING);
		                return -1;
		            }
		            if (jamCombo.getValue() == null) {
		                showAlert("Peringatan", "Silakan pilih jam terlebih dahulu!", Alert.AlertType.WARNING);
		                return -1;
		            }
		            
		            if (studioCombo.getValue() == null) {
		                showAlert("Peringatan", "Silakan pilih studio terlebih dahulu!", Alert.AlertType.WARNING);
		                return -1;
		            }
	
		            String selectedStudioName = studioCombo.getValue();
	
		            // Cari id_studio dari nama studio yang dipilih
		            int idStudio = -1;
		            try (Connection conn = getConnection()) {
		                String sqlStudio = "SELECT id_studio FROM studio WHERE nama_studio = ?";
		                PreparedStatement psStudio = conn.prepareStatement(sqlStudio);
		                psStudio.setString(1, selectedStudioName);
		                ResultSet rsStudio = psStudio.executeQuery();
		                if (rsStudio.next()) {
		                    idStudio = rsStudio.getInt("id_studio");
		                } else {
		                    showAlert("Peringatan", "Studio tidak ditemukan!", Alert.AlertType.WARNING);
		                    return -1;
		                }
	
		                // Cari jadwal dengan id_studio yang ditemukan
		                String sql = "SELECT j.id_jadwal FROM jadwal j JOIN film f ON j.id_film = f.id_film " +
		                             "WHERE j.tanggal = ? AND j.jam = ? AND f.judul_film = ? AND j.id_studio = ?";
		                PreparedStatement ps = conn.prepareStatement(sql);
		                ps.setDate(1, java.sql.Date.valueOf(tanggalPicker.getValue()));
		                ps.setString(2, jamCombo.getValue());
		                ps.setString(3, filmCombo.getValue());
		                ps.setInt(4, idStudio);
	
		                ResultSet rs = ps.executeQuery();
		                if (rs.next()) {
		                    return rs.getInt("id_jadwal");
		                } else {
		                    showAlert("Peringatan", "Jadwal tidak ditemukan!", Alert.AlertType.WARNING);
		                    return -1;
		                }
		            }
		        } catch (SQLException e) {
		            e.printStackTrace();
		            showAlert("Error", "Terjadi kesalahan database: " + e.getMessage(), Alert.AlertType.ERROR);
		            return -1;
		        }
		    }
		    
		    //tambahin buat edit mode
		    public void setEditMode(boolean isEditMode) {
		        this.isEditMode = isEditMode;
		    }
	
		    public void setExistingBooking(Booking booking) {
		        this.existingBooking = booking;
		        namaField.setText(booking.getNama());
		        filmCombo.setValue(booking.getFilm());
		        studioCombo.setValue(booking.getStudio());
		        tanggalPicker.setValue(LocalDate.parse(booking.getTanggal()));
		        jamCombo.setValue(booking.getJam());
	
		        // Hapus dulu reservasi kursinya di DB biar ke-refresh
		        releaseSelectedSeats(booking.getIdReservasi());
	
		        loadKursi();
	
		        // Set warna kuning buat kursi yang sebelumnya dipilih (kalau masih mau ditandain)
		        setSelectedSeats(booking.getKursi());
		    }
		    
		    //hapus kursi yang sudah direservasi
		    void releaseSelectedSeats(int reservationId) {
		        try (Connection conn = getConnection()) {
		            String sql = "DELETE FROM reservation WHERE id_reservasi = ?";
		            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
		                stmt.setInt(1, reservationId);
		                stmt.executeUpdate();
		            }
		        } catch (SQLException e) {
		            e.printStackTrace();
		        }
		    }
	
		    // kursi yang udah reserved
		    void setSelectedSeats(String kursiDipilih) {
		        String[] kursiTerpilih = kursiDipilih.split(",");
		        for (Button b : kursiButtons) {
		            for (String kursi : kursiTerpilih) {
		                if (b.getText().equals(kursi.trim())) {
		                    b.setStyle("-fx-background-color: #FFEB3B;");
		                }
		            }
		        }
		    }
		    
		    //menampilkan alert
		    void showAlert(String title, String message, Alert.AlertType type) {
		        Alert alert = new Alert(type);
		        alert.setTitle(title);
		        alert.setHeaderText(null);
		        alert.setContentText(message);
		        alert.showAndWait();
		    }
		}
