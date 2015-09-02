package c;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import javafx.animation.FadeTransition;
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
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.lang.System.currentTimeMillis;

/**
 * Created by denny on 8/6/15.
 */
public class Main extends Application {
    static final int IMG_W = 800;
    static final int IMG_H = 600;

    final int GRID_W=2;
    final int GRID_H=2;

    int imgPos=0;
    File[] images;
    List<Cell> currentCells = new ArrayList<Cell>();
    final GridPane root = new GridPane();


    class Cell{
        Node view;
        String word;
        boolean active=true;
        long inactivateT;

        void inactivate(){
            active = false;
            inactivateT = System.currentTimeMillis();
        }
        void reactivate(){
            active = true;
            inactivateT = 0;
        }

        Cell(Node view, String word) {
            this.view = view;
            this.word = word;
        }
    }

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
        iv2.setFitWidth(IMG_W/GRID_W);
        iv2.setFitHeight(IMG_H/GRID_H);
        iv2.setPreserveRatio(true);
        return iv2;
    }

//    void showImg(BorderPane root, File img) throws IOException{
//        root.setCenter(fileImg(img));
//        String word = word(img);
//        root.setBottom(createBorderedText(word));
//        System.out.println("Expecting speech: "+word);
//    }

    abstract class GridScan{
        GridScan() {
            for( int i=0; i<GRID_W; i++){
                for( int j=0; j<GRID_H; j++ ){
                    File img = images[(imgPos+i+j*GRID_W)%images.length];
                    try {
                        cell(i,j,img);
                    } catch (Exception e) {
                        throw new RuntimeException("",e);
                    }
                }
            }
        }
        abstract void cell(int i, int j, File img) throws Exception;
    }

    void nextImgGroup(){
        imgPos += GRID_H * GRID_W;
        showImgGroup(root);
    }

    void showImgGroup(final GridPane gridpane){
       for( Cell c : currentCells ){
           fadeInOut(c.view, false);
       }
       gridpane.getChildren().removeAll();
       currentCells.clear();
       new GridScan(){
           @Override
           void cell(int i, int j, File img) throws Exception{
               BorderPane bp = new BorderPane();
               bp.setCenter(fileImg(img));
               String word = word(img);
               bp.setBottom(createBorderedText(word));
               System.out.println("Expecting speech: " + word);
               gridpane.add(bp, i+1, j+1);
               GridPane.setHgrow(bp, Priority.ALWAYS);
               GridPane.setVgrow(bp, Priority.ALWAYS);
               currentCells.add(new Cell(bp, word));
           }
       };
    }

    private Node createBorderedText(String txt) {
        Text text = new Text(txt);
        text.setFont(new Font(30));

        StackPane sp = new StackPane(text);
        //sp.setStyle("-fx-border-color: red;");
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
                                Platform.runLater(() -> {
                                    onSpeechWord(txt);
                                });
                        }
                    };
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


        //final StackPane root = new StackPane(fileImg(images[imgPos]));
//        root = new BorderPane();
//        showImg(root, images[imgPos]);
        //root.setGridLinesVisible(true);
        showImgGroup(root);

//        BorderPane mainBorderPane = new BorderPane();
//        mainBorderPane.setCenter(root);
//        StackPane sp = new StackPane(root);
//        sp.setStyle("-fx-border-color: blue;");

        stage.setTitle("AudioCards");
        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.show();

        scene.setOnKeyTyped( (KeyEvent keyEvent) -> {
            int pos = keyEvent.getCharacter().charAt(0) - '1';
            if( pos>=0 && pos<currentCells.size() ){
                hit(pos);
            }
        });

        scene.setOnKeyPressed((KeyEvent keyEvent) -> {
            if( keyEvent.getCode() == KeyCode.BACK_SPACE /* KeyCode.RIGHT */ ){
                goBack();
            }
        });
        stage.setOnCloseRequest(windowEvent -> {
            System.exit(0);
        });
    }

    void onSpeechWord(String txt) {
        if( "back".equals(txt) ){
            goBack();
        }else {
            for (int i = 0; i < currentCells.size(); i++) {
                if (currentCells.get(i).word.equals(txt)) {
                    hit(i);
                }
            }
        }
    }

    void goBack(){
        if( currentCells.stream().allMatch( c -> c.active) ){
            if( imgPos >= GRID_H * GRID_W ) {
                imgPos -= GRID_H * GRID_W;
                showImgGroup(root);
//                Platform.runLater(this::nextImgGroup);

            }
        }else{
            Cell c = currentCells.stream().max((o1, o2) -> new Long(o1.inactivateT).compareTo(o2.inactivateT)).get();
            c.reactivate();
            fadeInOut(c.view, true);
        }
    }

    void fadeInOut(Node node, boolean in){
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        if( in ){
            ft.setFromValue(0);
            ft.setToValue(1.0);
        }else {
            ft.setFromValue(1.0);
            ft.setToValue(0);
        }
        ft.play();
    }

    void hit(int cell){
        Cell c = currentCells.get(cell);
        if( c.active ) {
            fadeInOut(c.view, false);
            c.inactivate();
        }

        if( ! anyActive() ){
            nextImgGroup();
        }
    }

    boolean anyActive(){
        return currentCells.stream().anyMatch( c -> c.active);
    }

//    void nextImage() {
//        try {
//            imgPos = (imgPos+1) % images.length;
//            //root.getChildren().set(0, fileImg(images[imgPos]));
//            showImg(root, images[imgPos]);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args) throws Exception{
        if( args.length==1 ) {
            new SpeechRecognition(args[0]);
        }else{
            launch(args);
        }
    }



}
