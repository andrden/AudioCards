package c;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

/**
 * Created by denny on 8/6/15.
 */
public class Main {
    private static final String ACOUSTIC_MODEL =
            "resource:/edu/cmu/sphinx/models/en-us/en-us";
    private static final String DICTIONARY_PATH =
            "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
    private static final String GRAMMAR_PATH =
            "resource:/gramm/";

    public static void main(String[] args) throws Exception{
        long t0 = currentTimeMillis();
        System.out.println("Loading models...");

        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath(ACOUSTIC_MODEL);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setGrammarPath(GRAMMAR_PATH);
        configuration.setUseGrammar(true);

        configuration.setGrammarName("menu");

        if( args.length==1 ) {
            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            System.out.println("tprepare = " + (currentTimeMillis() - t0));
            t0 = currentTimeMillis();
            // "/home/denny/Downloads/Record_0003.wav"
            fileRecognize(recognizer, args[0]);
            System.out.println("tprocess = " + (currentTimeMillis() - t0));
        }else{
            LiveSpeechRecognizer lmRecognizer =
                new LiveSpeechRecognizer(configuration);
            System.out.println("tprepare = " + (currentTimeMillis() - t0));
            mikeRecognize(lmRecognizer);
        }
    }

    static void mikeRecognize(LiveSpeechRecognizer recognizer) throws Exception{
        System.out.println("Recognizing from microphone...");
        // Simple recognition with generic model
        recognizer.startRecognition(true);
        SpeechResult result;
        StringBuilder sb = new StringBuilder();
        while ((result = recognizer.getResult()) != null) {
            sb.append(result.getHypothesis()+" ");
            System.out.format("Hypothesis: %s\n", result.getHypothesis());

            System.out.println("List of recognized words and their times:");
            for (WordResult r : result.getWords()) {
                System.out.println(r + " "+r.getConfidence()+ " "+r.getScore());
            }

            System.out.println("Best 3 hypothesis:");
            for (String s : result.getNbest(3))
                System.out.println(s);

        }
        recognizer.stopRecognition();
        System.out.println("Result: "+sb);
    }

    static void fileRecognize(StreamSpeechRecognizer recognizer, String fname) throws Exception{
        System.out.println("Recognizing file: "+fname);
        InputStream stream = new FileInputStream(fname);

        // Simple recognition with generic model
        recognizer.startRecognition(stream);
        SpeechResult result;
        StringBuilder sb = new StringBuilder();
        while ((result = recognizer.getResult()) != null) {
            sb.append(result.getHypothesis()+" ");
        }
        recognizer.stopRecognition();
        System.out.println("Result: " + sb);

    }

}
