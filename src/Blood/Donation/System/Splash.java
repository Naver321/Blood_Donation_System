package Blood.Donation.System;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Splash extends Application {
    @Override
    public void start(Stage splashStage) {
        Image splashImage = new Image(getClass().getResourceAsStream("/icons/splash1.gif"));
        ImageView imageView = new ImageView(splashImage);


        imageView.setFitWidth(1280);
        imageView.setFitHeight(720);
        imageView.setPreserveRatio(true);

        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 1280, 720);
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(scene);
        splashStage.show();


        PauseTransition delay = new PauseTransition(Duration.seconds(6.5));
        delay.setOnFinished(event -> {
            splashStage.close();
            new Login().start(new Stage());
        });
        delay.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
