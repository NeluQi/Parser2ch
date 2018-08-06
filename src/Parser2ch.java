/*My fist java program :)*/


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/* @author Nelu*/
public class Parser2ch extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLMain.fxml"));
          root.setStyle("-fx-background-radius: 8;" +
                "-fx-background-color: rgb(45, 45, 50), rgb(60, 60, 65);" + 
                "-fx-background-insets: 0, 0 1 1 0;");
            Scene scene = new Scene(root, Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle("2ch");
            stage.show();
            
        
    }
    public static void main(String[] args) {
        launch(args);
    }
    
}
