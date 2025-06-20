package DB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

public class Connector {
    private static final String URL = "jdbc:mysql://localhost:3306/bioskopappdb";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
            return null;
        }
    }

    public void deleteBooking(int idReservasi) {
        String query = "DELETE FROM reservation WHERE id_reservasi = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idReservasi);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //method alternatif yang dibuat untuk mengambil atau membuat jadwal baru
    public int getOrCreateJadwal(String tanggal, String jam, String film, String studio) {
        String selectQuery = "SELECT j.id_jadwal FROM jadwal j JOIN film f ON j.id_film = f.id_film JOIN studio s ON j.id_studio = s.id_studio WHERE j.tanggal = ? AND j.jam = ? AND f.judul_film = ? AND s.nama_studio = ?";
        String insertQuery = "INSERT INTO jadwal (id_film, id_studio, tanggal, jam) VALUES ((SELECT id_film FROM film WHERE judul_film = ?), (SELECT id_studio FROM studio WHERE nama_studio = ?), ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {

            // Cek apakah jadwal sudah ada
            selectStmt.setString(1, tanggal);
            selectStmt.setString(2, jam);
            selectStmt.setString(3, film);
            selectStmt.setString(4, studio);

            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_jadwal");
            } else {
                // Kalau nggak ada  buat baru
                insertStmt.setString(1, film);
                insertStmt.setString(2, studio);
                insertStmt.setString(3, tanggal);
                insertStmt.setString(4, jam);
                insertStmt.executeUpdate();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Gagal membuat jadwal baru.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    //method alternatif yang dibuat untuk update booking
    public void updateBooking(int idReservasi, int idJadwal) {
        String query = "UPDATE reservation SET id_jadwal = ? WHERE id_reservasi = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, idJadwal);
            pstmt.setInt(2, idReservasi);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //method alternatif yang dibuat untuk update booking
    public void updateKursiBooking(int idReservasi, String noKursiBaru) {
        String queryCariIdKursi = "SELECT id_kursi FROM kursi WHERE no_kursi = ?";
        String queryUpdateReservasi = "UPDATE reservation SET id_kursi = ? WHERE id_reservasi = ?";
        
        try (Connection conn = getConnection()) {
            // Cari id_kursi dari no_kursi
            int idKursi = -1;
            try (PreparedStatement psCari = conn.prepareStatement(queryCariIdKursi)) {
                psCari.setString(1, noKursiBaru);
                try (ResultSet rs = psCari.executeQuery()) {
                    if (rs.next()) {
                        idKursi = rs.getInt("id_kursi");
                    } else {
                        // no_kursi tidak ditemukan
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Nomor kursi tidak ditemukan");
                            alert.setContentText("Nomor kursi " + noKursiBaru + " tidak ada di database.");
                            alert.showAndWait();
                        });
                        return; // keluar dari method
                    }
                }
            }

            // Update reservation dengan id_kursi baru
            try (PreparedStatement psUpdate = conn.prepareStatement(queryUpdateReservasi)) {
                psUpdate.setInt(1, idKursi);
                psUpdate.setInt(2, idReservasi);
                int rowsUpdated = psUpdate.executeUpdate();
                
                if (rowsUpdated > 0) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Sukses");
                        alert.setHeaderText("Update berhasil");
                        alert.setContentText("Nomor kursi berhasil diupdate.");
                        alert.showAndWait();
                    });
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Gagal");
                        alert.setHeaderText("Update gagal");
                        alert.setContentText("Data reservasi tidak ditemukan.");
                        alert.showAndWait();
                    });
                }
            }
            
        } catch (SQLException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Kesalahan saat update data");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }
    
  //method alternatif yang dibuat untuk update booking
    public void updateJadwalBooking(int idReservasi, int idJadwal) {
        String query = "UPDATE reservation SET id_jadwal = ? WHERE id_reservasi = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idJadwal);
            pstmt.setInt(2, idReservasi);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

  //method alternatif yang dibuat untuk update booking
    public ObservableList<String> getListFilm() {
        ObservableList<String> list = FXCollections.observableArrayList();
        String query = "SELECT judul_film FROM Film";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(rs.getString("judul_film"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    //method alternatif yang dibuat untuk mengambil data studio
    public ObservableList<String> getListStudio() {
        ObservableList<String> list = FXCollections.observableArrayList();
        String query = "SELECT nama_studio FROM Studio";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(rs.getString("nama_studio"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    //method alternatif yang dibuat untuk mengambil data kursi
    public List<String> getListKursiTersedia(String idStudio, String tanggal) {
        List<String> kursiTersedia = new ArrayList<>();
        Connection conn = getConnection();
        try {
            String query = "SELECT no_kursi FROM kursi "
                         + "WHERE id_studio = ? "
                         + "AND id_kursi NOT IN ("
                         + "SELECT id_kursi FROM reservation WHERE id_studio = ? AND tanggal_pesan = ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, idStudio);
            ps.setString(2, idStudio);
            ps.setString(3, tanggal);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                kursiTersedia.add(rs.getString("no_kursi"));
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Gagal Update Data Kursi!");
            alert.setContentText("Pesan Error: " + e.getMessage());
            alert.showAndWait();
        }
        return kursiTersedia;
    }

  //method alternatif yang dibuat untuk mengambil data studio
    public String getIdStudioByNama(String namaStudio) {
        String idStudio = "";
        Connection conn = getConnection();
        try {
            String query = "SELECT id_studio FROM studio WHERE nama_studio = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, namaStudio);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                idStudio = rs.getString("id_studio");
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idStudio;
    }



}
