/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Alex
 */
public class ClientThread extends Thread {

    PrintWriter out;
    BufferedReader in;
    Stage stage;
    String[][] board = new String[8][8];
    Map<Character, Image> images = new HashMap<>();
    String selectedSquare = null;
    volatile boolean stop;
    volatile char turn;
    char promote = ' ';
    GraphicsContext gc;
    char playerNr;
    StackPane root;
    StackPane currentStackPane;
    String lastMove;

    ClientThread(StackPane sp, BufferedReader in, PrintWriter out, Stage stage, boolean stop, char turn) {
        this.in = in;
        this.out = out;
        this.stage = stage;
        this.stop = stop;
        this.turn = turn;
        this.root = sp;
    }

    @Override
    public void run() {
        loadImages();
        try {
            String line;
            while ((line = in.readLine()) == null && !stop) {
                //do nothing
            }
            if (stop) {
                return;
            }
            if (line.equals("1")) {
                playerNr = '1';
            } else {
                playerNr = '2';
            }

            while ((line = in.readLine()) == null && !stop) {
                //do nothing
            }
            if (stop) {
                return;
            }
            lastMove = line.substring(line.length() - 5, line.length());
            line = line.substring(0, line.length() - 5);
            turn = line.charAt(0);
            line = line.substring(1, line.length());
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    board[i][j] = String.valueOf(line.charAt(i * 8 + j));
                }
            }
            stage.getScene().setRoot(getBoard(stage));
            while (!stop) {
                while ((line = in.readLine()) == null) {
                    //do nothing
                }
                String result = "";
                if (line.charAt(0) != '1' && line.charAt(0) != '2') {
                    switch (line) {
                        case "victory":
                            result = "You won!";
                            break;
                        case "loss":
                            result = "You lost!";
                            break;
                        case "draw":
                            result = "You drew!";
                            break;
                        default:
                            break;
                    }
                    stop = true;
                    stage.getScene().setRoot(getPostMatchMenu(stage, result));
                    break;
                }
                lastMove = line.substring(line.length() - 5, line.length());
                line = line.substring(0, line.length() - 5);
                turn = line.charAt(0);
                line = line.substring(1, line.length());
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        board[i][j] = String.valueOf(line.charAt(i * 8 + j));
                    }
                }
                drawBoard(gc);
                drawPieces(gc);
            }

        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    private StackPane getBoard(Stage stage) {
        currentStackPane = new StackPane();
        currentStackPane.setAlignment(Pos.CENTER);
        File file = new File("bg.jpg");
        BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString(), 1920, 1080, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        currentStackPane.setBackground(new Background(myBI));
        VBox container = new VBox();
        container.setSpacing(50);
        container.setAlignment(Pos.CENTER);
        Canvas canvas = new Canvas(800, 800);
        HBox buttons = new HBox();
        buttons.setSpacing(50);
        buttons.setAlignment(Pos.CENTER);
        container.getChildren().add(buttons);
        Button resign = new Button();
        resign.setOnMouseClicked(e -> {
            out.println("Resign");
            out.flush();
        });
        resign.setText("Resign");
        resign.setPadding(new Insets(10, 20, 10, 20));
        resign.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        buttons.getChildren().add(resign);
        container.getChildren().add(canvas);
        currentStackPane.getChildren().add(container);
        gc = canvas.getGraphicsContext2D();
        drawBoard(gc);
        drawPieces(gc);
        canvas.setOnMouseClicked(event -> {
            if (playerNr == turn) {
                int row = (int) event.getY() / 100;
                int col = (int) event.getX() / 100;
                if (selectedSquare == null) {
                    if (board[row][col].charAt(0) >= 'A' && board[row][col].charAt(0) <= 'Z' && turn == '1') {
                        selectedSquare = String.valueOf(row) + String.valueOf(col);
                    } else if (board[row][col].charAt(0) >= 'a' && board[row][col].charAt(0) <= 'z' && turn == '2') {
                        selectedSquare = String.valueOf(row) + String.valueOf(col);
                    }
                } else {
                    String newSquare = String.valueOf(row) + String.valueOf(col);
                    if (turn == '1' && board[row][col].charAt(0) >= 'A' && board[row][col].charAt(0) <= 'Z') {
                        selectedSquare = newSquare;
                    } else if (turn == '2' && board[row][col].charAt(0) >= 'a' && board[row][col].charAt(0) <= 'z') {
                        selectedSquare = newSquare;
                    } else {
                        String move = selectedSquare + newSquare;
                        if (board[selectedSquare.charAt(0) - 48][selectedSquare.charAt(1) - 48].toLowerCase().equals("p") && (newSquare.charAt(0) - 48 == 0 || newSquare.charAt(0) - 48 == 7)) {
                            PopUpFrame popup = new PopUpFrame(turn, this);
                            try {
                                Stage newStage = popup.getStage(new Stage());
                                newStage.setAlwaysOnTop(true);
                                newStage.showAndWait();
                            } catch (Exception ex) {
                                System.out.println(ex.toString());
                            }
                            move += promote;
                            promote = ' ';
                        } else {
                            move += ' ';
                        }
                        out.println(move);
                        out.flush();
                    }
                    //selectedSquare = null;
                }
            }
        });
        return currentStackPane;
    }

    private void drawBoard(GraphicsContext gc) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String color;
                if ((i + j) % 2 == 0) {
                    color = "BURLYWOOD";
                } else {
                    color = "CHOCOLATE";
                }
                gc.setFill(Paint.valueOf(color));
                gc.fillRect(j * 100, i * 100, 100, 100);
            }
        }
        if(!(lastMove.equals("     "))) {
            int oldSquareX = lastMove.charAt(0) - 48;
            int oldSquareY = lastMove.charAt(1) - 48;
            int newSquareX = lastMove.charAt(2) - 48;
            int newSquareY = lastMove.charAt(3) - 48;
            gc.setFill(Color.RED);
            gc.fillRect(oldSquareY * 100, oldSquareX * 100, 100, 100);
            gc.fillRect(newSquareY * 100, newSquareX * 100, 100, 100);
        }
    }

    private void drawPieces(GraphicsContext gc) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (!" ".equals(board[i][j])) {
                    gc.drawImage(images.get(board[i][j].charAt(0)), j * 100, i * 100, 100, 100);
                }
            }
        }
    }

    public void loadImages() {
        File file = new File("Pieces\\B_King.png");
        Image image = new Image(file.toURI().toString());
        images.put('k', image);

        file = new File("Pieces\\B_Queen.png");
        image = new Image(file.toURI().toString());
        images.put('q', image);

        file = new File("Pieces\\B_Rook.png");
        image = new Image(file.toURI().toString());
        images.put('r', image);

        file = new File("Pieces\\B_Bishop.png");
        image = new Image(file.toURI().toString());
        images.put('b', image);

        file = new File("Pieces\\B_Knight.png");
        image = new Image(file.toURI().toString());
        images.put('n', image);

        file = new File("Pieces\\B_Pawn.png");
        image = new Image(file.toURI().toString());
        images.put('p', image);

        file = new File("Pieces\\W_King.png");
        image = new Image(file.toURI().toString());
        images.put('K', image);

        file = new File("Pieces\\W_Queen.png");
        image = new Image(file.toURI().toString());
        images.put('Q', image);

        file = new File("Pieces\\W_Rook.png");
        image = new Image(file.toURI().toString());
        images.put('R', image);

        file = new File("Pieces\\W_Bishop.png");
        image = new Image(file.toURI().toString());
        images.put('B', image);

        file = new File("Pieces\\W_Knight.png");
        image = new Image(file.toURI().toString());
        images.put('N', image);

        file = new File("Pieces\\W_Pawn.png");
        image = new Image(file.toURI().toString());
        images.put('P', image);

    }

    private StackPane getPostMatchMenu(Stage stage, String result) {
        Button back = new Button();
        Text txt = new Text(" " + result + " ");
        txt.setFont(Font.font("Verdana", FontPosture.ITALIC, 25));
        VBox textContainer = new VBox();
        textContainer.setMaxSize(130, 100);
        textContainer.setStyle("-fx-background-color: rgba(165, 111, 38, 0.6); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50;");
        textContainer.getChildren().add(txt);
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(root);
        });
        back.setText("Menu");
        back.setPadding(new Insets(10, 20, 10, 20));
        back.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        currentStackPane.getChildren().stream().filter((n) -> (n instanceof VBox)).forEachOrdered((n) -> {
            ((VBox) n).getChildren().stream().filter((m) -> (m instanceof HBox)).forEachOrdered((m) -> {
                Platform.runLater(() -> {
                    ((VBox) n).getChildren().add(textContainer);
                    ((HBox) m).getChildren().add(back);
                });
            });
        });
        return currentStackPane;
    }
}
