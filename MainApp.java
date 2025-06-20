	
	package app;
	import GUI.MainMenuStage;
	import javafx.application.Application;
	import javafx.stage.Stage;
	
	public class MainApp extends Application {
	    @Override
	    public void start(Stage primaryStage) throws Exception {
	        MainMenuStage mainMenu = new MainMenuStage();
	        mainMenu.show();
	    }
	
	    public static void main(String[] args) {
	        launch(args);
	    }
	}
