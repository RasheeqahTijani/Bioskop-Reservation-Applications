	package GUI;
	
	import java.sql.*;
	import java.time.LocalDate;
	import DB.Connector;
	import javafx.application.Application;
	import javafx.collections.*;
	import javafx.geometry.Insets;
	import javafx.scene.*;
	import javafx.scene.control.*;
	import javafx.scene.control.cell.PropertyValueFactory;
	import javafx.scene.layout.*;
	import javafx.stage.Stage;

	public class LihatBookingStage extends Stage {
	
	    Connector connector = new Connector();
	
	    public LihatBookingStage() {
	        setTitle("Lihat Booking");
	
	        Label titleLabel = new Label("Daftar Reservasi Kursi Bioskop");
	        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
	        
	        //membuat table
	        TableView<Booking> table = new TableView<>();
	
	        TableColumn<Booking, String> namaCol = new TableColumn<>("Nama");
	        namaCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
	
	        TableColumn<Booking, String> filmCol = new TableColumn<>("Film");
	        filmCol.setCellValueFactory(new PropertyValueFactory<>("film"));
	
	        TableColumn<Booking, String> studioCol = new TableColumn<>("Studio");
	        studioCol.setCellValueFactory(new PropertyValueFactory<>("studio"));
	
	        TableColumn<Booking, String> seatCol = new TableColumn<>("Kursi");
	        seatCol.setCellValueFactory(new PropertyValueFactory<>("kursi"));
	
	        TableColumn<Booking, String> tanggalCol = new TableColumn<>("Tanggal");
	        tanggalCol.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
	
	        TableColumn<Booking, String> jamCol = new TableColumn<>("Jam ");
	        jamCol.setCellValueFactory(new PropertyValueFactory<>("jam"));
	        
	        //masukkan data
	        table.getColumns().addAll(namaCol, filmCol, studioCol, seatCol, tanggalCol, jamCol);
	        table.setItems(getBookingList());

	
	        // tombol cari film berdasarkan nama
	        Button cariNamaBtn = new Button("Cari Nama");
	        cariNamaBtn.setOnAction(e -> {
	            TextInputDialog dialog = new TextInputDialog();
	            dialog.setTitle("Cari Reservasi");
	            dialog.setHeaderText("Masukkan nama:");
	            dialog.showAndWait().ifPresent(name -> {
	                table.setItems(getBookingList().filtered(b -> b.getNama().toLowerCase().contains(name.toLowerCase())));
	            });
	        });
	        
	        //tombol cari film berdasarkan judul
	        Button cariFilmBtn = new Button("Cari Film");
	        cariFilmBtn.setOnAction(e -> {
	            TextInputDialog dialog = new TextInputDialog();
	            dialog.setTitle("Cari Reservasi");
	            dialog.setHeaderText("Masukkan judul film:");
	            dialog.showAndWait().ifPresent(film -> {
	                table.setItems(getBookingList().filtered(b -> b.getFilm().toLowerCase().contains(film.toLowerCase())));
	            });
	        });
	
	        // tombol update
	        Button updateBtn = new Button("Update Reservasi");
	        updateBtn.setOnAction(e -> {
	            Booking selected = table.getSelectionModel().getSelectedItem();
	            if (selected != null) {
	            	//close lihat booking pane
	            	LihatBookingStage.this.close();
	            	
	                // Panggil form reservasi yang sudah ada
	                BioskopStage formReservasi = new BioskopStage();
	
	                // Set mode edit + data existing
	                formReservasi.setEditMode(true);
	                formReservasi.setExistingBooking(selected);
	
	                // Tampilkan form
	                formReservasi.showAndWait();
	
	                // Setelah close, refresh table
	                table.setItems(getBookingList());
	
	            } else {
	                showAlert("Pilih Reservasi yang ingin di-update.");
	            }
	        });
	
	
	        // tombol delete
	        Button deleteBtn = new Button("Delete Reservasi");
	        deleteBtn.setOnAction(e -> {
	            Booking selected = table.getSelectionModel().getSelectedItem();
	            if (selected != null) {
	                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
	                        "Yakin ingin menghapus Reservasi ini?", ButtonType.YES, ButtonType.NO);
	                confirm.showAndWait();
	
	                if (confirm.getResult() == ButtonType.YES) {
	                    connector.deleteBooking(selected.getIdReservasi());
	                    table.setItems(getBookingList());
	                }
	            } else {
	                showAlert("Pilih Reservasi yang ingin dihapus.");
	            }
	        });
	
	        // tombol kembali
	        Button kembaliBtn = new Button("Kembali");
	        kembaliBtn.setOnAction(e -> {
	        	// MainMenuPane extends Stage sekarang
	            MainMenuStage mainMenu = new MainMenuStage(); 
	            mainMenu.show();
	            this.close();
	        });
	
	        HBox tombolBox = new HBox(10, cariNamaBtn, cariFilmBtn, updateBtn, deleteBtn);
	        tombolBox.setPadding(new Insets(10));
	        tombolBox.setStyle("-fx-alignment: center;");
	
	        VBox layout = new VBox(15, titleLabel, table, tombolBox, kembaliBtn);
	        layout.setPadding(new Insets(20));
	        layout.setStyle("-fx-alignment: center;");
	
	        Scene scene = new Scene(layout, 500, 500);
	        setScene(scene);
	    }
	    
	    //method untuk mengambil data booking
	    public ObservableList<Booking> getBookingList() {
	        ObservableList<Booking> bookingList = FXCollections.observableArrayList();
	
	        String query = "SELECT r.id_reservasi, r.nama_pemesan, f.judul_film, s.nama_studio, k.no_kursi, j.tanggal, j.jam " +
	                "FROM reservation r " +
	                "JOIN jadwal j ON r.id_jadwal = j.id_jadwal " +
	                "JOIN film f ON j.id_film = f.id_film " +
	                "JOIN studio s ON j.id_studio = s.id_studio " +
	                "JOIN kursi k ON r.id_kursi = k.id_kursi";
	
	        try (Connection conn = connector.getConnection();
	             Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery(query)) {
	
	            while (rs.next()) {
	                Booking b = new Booking(
	                        rs.getInt("id_reservasi"),
	                        rs.getString("nama_pemesan"),
	                        rs.getString("judul_film"),
	                        rs.getString("nama_studio"),
	                        rs.getString("no_kursi"),
	                        rs.getString("tanggal"),
	                        rs.getString("jam")
	                );
	                bookingList.add(b);
	            }
	            
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return bookingList;
	    }
	    
	    //alert
	    private void showAlert(String message) {
	        Alert alert = new Alert(Alert.AlertType.INFORMATION);
	        alert.setTitle("Peringatan");
	        alert.setHeaderText(null);
	        alert.setContentText(message);
	        alert.showAndWait();
	    }
	}
