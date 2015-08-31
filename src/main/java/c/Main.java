package c;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.currentTimeMillis;

/**
 * Created by denny on 8/6/15.
 */
public class Main extends Application {
    static final int IMG_W = 800;
    static final int IMG_H = 600;

    int imgPos=0;
    File[] images;
    BorderPane root;

    ImageView fileImg(File file) throws IOException{
        ImageView iv2 = new ImageView();
        if( file.getName().endsWith(".gif") ){
            Animation ani = new AnimatedGif(file.getPath(), 2000);
            ani.setCycleCount(999);
            ani.play();
            iv2 = ani.getView();
        }else {
            Image image = new Image(new FileInputStream(file));
            iv2.setImage(image);
        }
        iv2.setSmooth(true);
        iv2.setCache(true);
        iv2.setFitWidth(IMG_W);
        iv2.setFitHeight(IMG_H);
        iv2.setPreserveRatio(true);
        return iv2;
    }

    void showImg(BorderPane root, File img) throws IOException{
        root.setCenter(fileImg(img));
        String word = word(img);
        root.setBottom(createBorderedText(word));
        System.out.println("Expecting speech: "+word);
    }

    private Node createBorderedText(String txt) {
        Text text = new Text(txt);
        text.setFont(new Font(30));

        StackPane sp = new StackPane(text);
        sp.setStyle("-fx-border-color: red;");
        return sp ;
    }

    String word(File img){
        String n = img.getName();
        return n.substring(0, n.indexOf('.'));
    }

    @Override
    public void start(Stage stage) throws Exception {
        images = new File(Main.class.getClassLoader().getResource("img").getFile()).listFiles();

        new Thread(){
            @Override
            public void run() {
                try {
                    new SpeechRecognition(null){
                        @Override
                        void onSpeech(String txt) {
                            if( word(images[imgPos]).equals(txt) ){
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        nextImage();
                                    }
                                });
                            }
                        }
                    };
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


        //final StackPane root = new StackPane(fileImg(images[imgPos]));
        root = new BorderPane();
        showImg(root, images[imgPos]);
        stage.setTitle("AudioCards");
        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.show();

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if( keyEvent.getCode()== KeyCode.RIGHT ){
                    nextImage();
                }
            }
        });
    }

    void nextImage() {
        try {
            imgPos = (imgPos+1) % images.length;
            //root.getChildren().set(0, fileImg(images[imgPos]));
            showImg(root, images[imgPos]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        if( args.length==1 ) {
            new SpeechRecognition(args[0]);
        }else{
            launch(args);
        }
    }



}
