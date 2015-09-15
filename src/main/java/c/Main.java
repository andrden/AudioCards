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
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by denny on 8/6/15.
 */
public class Main extends Application {
    UI ui;

    @Override
    public void start(Stage stage) throws Exception {
        File imgDir = new File(Main.class.getClassLoader().getResource("img").getFile());
        List<File> subDirs = Arrays.<File>asList(imgDir.listFiles());
        Collections.shuffle(subDirs);
        List<File> allImgList = new ArrayList<>();
        for( File dir : subDirs ){
            File[] oneDir = dir.listFiles();
            allImgList.addAll(Arrays.<File>asList(oneDir));
        }
        System.out.println("Total images: "+allImgList.size());


//        File[] images = Files.walk(imgDir.toPath())
//                .filter(p -> p.toFile().isFile())
//                .map(p -> p.toFile())
//                .toArray(File[]::new);

        makeMenuGram(allImgList.stream().map( f -> Model.word(f)).toArray(String[]::new) );
        ui = new UI(allImgList, stage);

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

    static void makeMenuGram(String[] words) throws IOException{
        String tpl = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("gramm/menu.gram"));
        String repl = Arrays.<String>asList(words).stream().collect(Collectors.joining(" | ","", " | "));
        String gram = tpl.replace("[[INSERT_HERE]]", repl);
        Files.write(new File("/tmp/audiocardsmenu.gram").toPath(), gram.getBytes());
    }


    public static void main(String[] args) throws Exception{
        if( args.length==1 ) {
            new SpeechRecognition(args[0]);
        }else{
            launch(args);
        }
    }



}
