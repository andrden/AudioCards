package c;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by denny on 8/6/15.
 */
public class Main extends Application {
    UI ui;

    @Override
    public void start(Stage stage) throws Exception {
        File[] images = Files.walk(new File(Main.class.getClassLoader().getResource("img").getFile()).toPath())
                .filter(p -> p.toFile().isFile())
                .map(p -> p.toFile())
                .toArray(File[]::new);

        //File[] images = new File(Main.class.getClassLoader().getResource("img").getFile()).listFiles();

        ui = new UI(images, stage);

        new Thread(){
            @Override
            public void run() {
                try {
                    new SpeechRecognition(null){
                        @Override
                        void onSpeech(String txt) {
                                Platform.runLater(() -> {
                                    ui.onSpeechWord(txt);
                                });
                        }
                    };
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }



    public static void main(String[] args) throws Exception{
        if( args.length==1 ) {
            new SpeechRecognition(args[0]);
        }else{
            launch(args);
        }
    }



}
