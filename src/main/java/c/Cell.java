package c;

import javafx.scene.Node;

/**
* Created by denny on 9/3/15.
*/
class Cell {
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
