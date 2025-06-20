package GUI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenuStage extends Stage {

    public MainMenuStage() {
        setTitle("Main Menu Bioskop");

        // Judul halaman
        Label titleLabel = new Label("Reservasi Tiket Bioskop");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        //tombol reservasi krn bioskopApp udh jadi stage
        Button reservasiBtn = new Button("Reservasi Kursi");
        reservasiBtn.setPrefWidth(200);
        reservasiBtn.setOnAction(e -> {
            BioskopStage bioskopStage = new BioskopStage(); // BioskopApp extends Stage sekarang
            bioskopStage.show();
            this.close();  // tutup MainMenuPane kalau perlu
        });
        
        //ini tombol lihat booking
        Button lihatBookingBtn = new Button("Lihat Reservasi");
        lihatBookingBtn.setPrefWidth(200);
        lihatBookingBtn.setOnAction(e -> {
            LihatBookingStage lihatBookingApp = new LihatBookingStage();
            lihatBookingApp.show();
            this.close(); // kalau memang mau nutup MainMenuPane
        });

        // Tombol Keluar
        Button exitBtn = new Button("Keluar");
        exitBtn.setPrefWidth(200);
        exitBtn.setOnAction(e -> {
            this.close();
        });

        // Layout VBox
        VBox layout = new VBox(20);
        layout.setStyle("-fx-padding: 30; -fx-alignment: center;");
        layout.getChildren().addAll(titleLabel,
                reservasiBtn,
                lihatBookingBtn,
                exitBtn);

        // Scene
        Scene scene = new Scene(layout, 300, 250);
        setScene(scene);
    }
}

