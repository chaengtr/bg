package management;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PageManagement {

    public void changePage(Stage stage, String pagePath) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(pagePath));
        try {
            Scene scene = new Scene(loader.load(), 900, 600);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("change page");
        }
    }
}
