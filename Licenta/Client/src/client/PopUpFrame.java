/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.File;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 *
 * @author Alex
 */
class PopUpFrame {

    char turn;
    ClientThread sah;
    char prom = ' ';

    PopUpFrame(char turn, ClientThread aThis) {
        
        this.turn = turn;
        sah = aThis;
    }

    public Stage getStage(Stage stage) throws Exception {
        Canvas canvas = new Canvas(400, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        for (int i = 0; i < 8; i++) {
            String color;
            if ((i) % 2 == 0) {
                color = "BURLYWOOD";
            } else {
                color = "CHOCOLATE";
            }
            gc.setFill(Paint.valueOf(color));
            gc.fillRect(i * 100, 0, 100, 100);
        }
        char color;
        if(turn == '1') {
            color = 'w';
        } else {
            color = 'b';
        }
        Group root = new Group(canvas);
        File file = new File("Pieces\\" + color + "_Queen.png");
        Image image = new Image(file.toURI().toString());
        gc.drawImage(image, 0, 0, 100, 100);

        file = new File("Pieces\\" + color + "_Rook.png");
        image = new Image(file.toURI().toString());
        gc.drawImage(image, 100, 0, 100, 100);

        file = new File("Pieces\\" + color + "_Bishop.png");
        image = new Image(file.toURI().toString());
        gc.drawImage(image, 200, 0, 100, 100);

        file = new File("Pieces\\" + color + "_Knight.png");
        image = new Image(file.toURI().toString());
        gc.drawImage(image, 300, 0, 100, 100);

        canvas.setOnMouseClicked(event -> {
            if (turn == '1') {
                if (event.getX() < 100) {
                    prom = 'Q';
                }
                if (event.getX() >= 100 && event.getX() < 200) {
                    prom = 'R';
                }

                if (event.getX() >= 200 && event.getX() < 300) {
                    prom = 'B';
                }

                if (event.getX() > 300) {
                    prom = 'N';
                }
            } else {
                if (event.getX() < 100) {
                    prom = 'q';
                }
                if (event.getX() >= 100 && event.getX() < 200) {
                    prom = 'r';
                }

                if (event.getX() >= 200 && event.getX() < 300) {
                    prom = 'b';
                }

                if (event.getX() > 300) {
                    prom = 'n';
                }
            }
            sah.promote = prom;
            stage.close();
        });

        stage.setScene(new Scene(root, 400, 100));
        return stage;
    }

}
