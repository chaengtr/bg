package main;

import dao.BookingDao;
import dao.FirebaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static FirebaseConnection connection;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        primaryStage.setTitle("Board Game Cafe");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
        primaryStage.setResizable(false);
    }

    public static void main(String[] args) {
        connection = new FirebaseConnection();
        connection.connect();

//        BookingDao dao = new BookingDao();
//        dao.deleteOldItemListener();

        launch(args);
    }
}
