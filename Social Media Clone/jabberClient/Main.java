package jabberserver;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("Jabber.fxml")));
        primaryStage.setTitle("JABBER");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("Stylesheet.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        //
    }


    public static void main(String[] args) {
        launch(args);

    }
}
