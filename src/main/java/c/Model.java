package c;

import java.io.File;

/**
 * Created by denny on 9/7/15.
 */
public class Model {
    static String word(File img){
        String n = img.getName();
        return n.substring(0, n.indexOf('.'));
    }
}
