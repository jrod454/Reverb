package com.jrod.reverb;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application {
    private FXMLLoader loader;

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        GlobalScreen.registerNativeHook();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("app.fxml"));
        primaryStage.setTitle("Reverb");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        GlobalScreen.unregisterNativeHook();
        super.stop();
    }
}