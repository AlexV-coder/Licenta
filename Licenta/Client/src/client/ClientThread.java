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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
    char pieceSelected;
    boolean stop;
    char turn;
    char promote = ' ';
    GraphicsContext gc;
    char playerNr;
    StackPane root;
    StackPane currentStackPane;
    String lastMove;
    int points;
    String customBoard[][];
    List<String> history;

    ClientThread(StackPane sp, BufferedReader in, PrintWriter out, Stage stage) {
        this.in = in;
        this.out = out;
        this.stage = stage;
        this.root = sp;
        points = 39;
        lastMove = "-----";
        customBoard = new String[][]{
            {" ", " ", " ", " ", " ", " ", " ", " "},
            {" ", " ", " ", " ", " ", " ", " ", " "}};
        pieceSelected = ' ';
        stop = false;
        history = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            loadImages();
            String line;
            line = in.readLine();
            if (line.equals("1")) {
                playerNr = '1';
            } else {
                playerNr = '2';
            }

            line = in.readLine();
            if (checkBattleMode(line)) {
                while (true) {
                    line = in.readLine();
                    if (line.length() == 70) {
                        break;
                    } else {
                        if (line.equals("Valid")) {
                            removeText();
                            deactivateButton();
                        } else {
                            showText(line);
                        }
                    }
                }
            }
            selectedSquare = null;
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
                line = in.readLine();
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
                addToHistory(lastMove);
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
                showHistory();

            }

        } catch (IOException ex) {
            System.out.println("First:");
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
        container.setSpacing(25);
        container.setAlignment(Pos.CENTER);

        HBox boardAndHistory = new HBox();

        StackPane boardTexture = new StackPane();
        boardTexture.setPrefSize(875, 875);
        boardTexture.setMaxSize(875, 875);
        file = new File("texture.jpg");
        BackgroundImage myBI2 = new BackgroundImage(new Image(file.toURI().toString(), 875, 875, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        boardTexture.setBackground(new Background(myBI2));
        Canvas canvas = new Canvas(800, 800);
        boardTexture.getChildren().add(canvas);

        StackPane showHistory = new StackPane();
        showHistory.setPrefSize(200, 800);
        showHistory.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-color: black;");
        Text txt = new Text();
        txt.setFont(Font.font("Verdana", FontPosture.ITALIC, 17));
        showHistory.setAlignment(Pos.TOP_CENTER);
        showHistory.getChildren().add(txt);

        boardAndHistory.getChildren().addAll(showHistory, boardTexture);
        boardAndHistory.setSpacing(50);
        boardAndHistory.setAlignment(Pos.CENTER);

        HBox buttons = new HBox();
        buttons.setSpacing(50);
        buttons.setAlignment(Pos.CENTER);
        container.getChildren().add(buttons);
        Button resign = new Button();
        resign.setOnMouseClicked(e -> {
            out.println("Resign");
            out.flush();
        });

        resign.setPadding(new Insets(10, 10, 10, 10));
        resign.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");

        ImageView imageView = new ImageView();
        file = new File("resign.png");
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);
        resign.setGraphic(imageView);

        buttons.getChildren().add(resign);
        container.getChildren().add(boardAndHistory);
        currentStackPane.getChildren().add(container);
        gc = canvas.getGraphicsContext2D();
        drawBoard(gc);
        drawPieces(gc);
        canvas.setOnMouseClicked(event -> {
            try {
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
                    }
                }
            } catch (Exception ex) {
                System.out.println("Second:");
                System.out.println(ex.toString());
            }
        });
        return currentStackPane;
    }

    private void drawBoard(GraphicsContext gc) {
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if ((i + j) % 2 == 0) {
                        gc.setFill(Color.rgb(210, 105, 30));
                    } else {
                        gc.setFill(Color.rgb(255, 222, 173));
                    }
                    gc.fillRect(j * 100, i * 100, 100, 100);
                }
            }
            if (!(lastMove.equals("-----"))) {
                int oldSquareX = lastMove.charAt(0) - 48;
                int oldSquareY = lastMove.charAt(1) - 48;
                int newSquareX = lastMove.charAt(2) - 48;
                int newSquareY = lastMove.charAt(3) - 48;
                gc.setFill(Color.RED);
                gc.fillRect(oldSquareY * 100, oldSquareX * 100, 100, 100);
                gc.fillRect(newSquareY * 100, newSquareX * 100, 100, 100);
            }
        } catch (Exception ex) {
            System.out.println("Third");
            System.out.println(ex.toString());
        }
    }

    private void drawPieces(GraphicsContext gc) {
        try {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (board[i][j].equals("0")) {
                        if ((i + j) % 2 == 0) {
                            gc.setFill(Color.rgb((int) (210 * 0.15), (int) (105 * 0.15), (int) (30 * 0.15)));
                        } else {
                            gc.setFill(Color.rgb((int) (255 * 0.15), (int) (222 * 0.15), (int) (173 * 0.15)));
                        }
                        gc.fillRect(j * 100, i * 100, 100, 100);
                    } else if (!" ".equals(board[i][j])) {
                        gc.drawImage(images.get(board[i][j].charAt(0)), j * 100, i * 100, 100, 100);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Fourth");
            System.out.println(ex.toString());
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
        try {
            Button back = new Button();
            Text txt = new Text(" " + result + " ");
            txt.setFont(Font.font("Verdana", FontPosture.ITALIC, 25));
            VBox textContainer = new VBox();
            textContainer.setMaxSize(130, 100);
            textContainer.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50;");
            textContainer.getChildren().add(txt);
            back.setOnMouseClicked(e -> {
                stage.getScene().setRoot(root);
            });
            ImageView imageView = new ImageView();
            File file = new File("back.png");
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            back.setGraphic(imageView);
            back.setPadding(new Insets(10, 10, 10, 10));
            back.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
            currentStackPane.getChildren().stream().filter((n) -> (n instanceof VBox)).forEachOrdered((n) -> {
                ((VBox) n).getChildren().stream().filter((m) -> (m instanceof HBox)).forEachOrdered((Node m) -> {
                    if (((HBox) m).getChildren().size() == 1) {
                        Platform.runLater(() -> {
                            ((VBox) n).getChildren().add(textContainer);
                            ((HBox) m).getChildren().add(back);
                        });
                    }
                });
            });
        } catch (Exception ex) {
            System.out.println("Fifth:");
            System.out.println(ex.toString());
        }
        return currentStackPane;
    }

    private StackPane getBattlePlan(Stage stage) {
        currentStackPane = new StackPane();
        currentStackPane.setAlignment(Pos.CENTER);
        File file = new File("bg.jpg");
        BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString(), 1920, 1080, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        currentStackPane.setBackground(new Background(myBI));
        VBox container = new VBox();
        container.setSpacing(25);
        container.setAlignment(Pos.CENTER);

        StackPane boardTexture = new StackPane();
        boardTexture.setPrefSize(875, 875);
        boardTexture.setMaxSize(875, 875);
        boardTexture.setAlignment(Pos.CENTER);
        file = new File("texture.jpg");
        BackgroundImage myBI2 = new BackgroundImage(new Image(file.toURI().toString(), 875, 875, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        boardTexture.setBackground(new Background(myBI2));
        Canvas canvas1 = new Canvas(800, 800);
        GraphicsContext gc1 = canvas1.getGraphicsContext2D();
        canvas1.setOnMouseClicked(e -> {
            int row = (int) e.getY() / 100;
            int col = (int) e.getX() / 100;
            if (row >= 6 && pieceSelected != ' ' && customBoard[row - 6][col].equals(" ")) {
                if (pieceSelected == 'K' || pieceSelected == 'k') {
                    customBoard[row - 6][col] = String.valueOf(pieceSelected);
                } else if ((pieceSelected == 'Q' || pieceSelected == 'q') && points >= 9) {
                    points -= 9;
                    customBoard[row - 6][col] = String.valueOf(pieceSelected);
                } else if ((pieceSelected == 'R' || pieceSelected == 'r') && points >= 5) {
                    points -= 5;
                    customBoard[row - 6][col] = String.valueOf(pieceSelected);
                } else if ((pieceSelected == 'B' || pieceSelected == 'b') && points >= 3) {
                    points -= 3;
                    customBoard[row - 6][col] = String.valueOf(pieceSelected);
                } else if ((pieceSelected == 'N' || pieceSelected == 'n') && points >= 3) {
                    points -= 3;
                    customBoard[row - 6][col] = String.valueOf(pieceSelected);
                } else if ((pieceSelected == 'P' || pieceSelected == 'p') && points >= 1) {
                    points -= 1;
                    customBoard[row - 6][col] = String.valueOf(pieceSelected);
                }
            } else if (row >= 6 && customBoard[row - 6][col].toLowerCase().charAt(0) >= 'a' && customBoard[row - 6][col].toLowerCase().charAt(0) <= 'z') {
                selectedSquare = String.valueOf(row - 6) + String.valueOf(col);
            }
            showPoints();
            drawCustomBoard(gc1);
        });
        boardTexture.getChildren().add(canvas1);
        VBox pieces = new VBox();
        pieces.setPrefSize(200, 800);
        pieces.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-color: black;");
        pieces.setSpacing(25);
        addPieceComponents(pieces);
        drawBoardBattle(gc1);
        HBox boardContainer = new HBox();
        boardContainer.setSpacing(50);
        boardContainer.getChildren().addAll(pieces, boardTexture);
        boardContainer.setAlignment(Pos.CENTER);
        Button submit = new Button();
        ImageView imageView = new ImageView();
        file = new File("checkmark.png");
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);
        submit.setGraphic(imageView);
        submit.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        submit.setPadding(new Insets(10, 10, 10, 10));
        submit.setOnMouseClicked(e -> {
            String sendBoard = "";
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 8; j++) {
                    sendBoard += customBoard[i][j];
                }
            }
            out.println(sendBoard);
            out.flush();
        });
        Button delete = new Button();
        ImageView imageView2 = new ImageView();
        file = new File("delete.png");
        image = new Image(file.toURI().toString());
        imageView2.setImage(image);
        delete.setGraphic(imageView2);
        delete.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        delete.setPadding(new Insets(10, 10, 10, 10));
        delete.setOnMouseClicked(e -> {
            if (selectedSquare != null) {
                int i = selectedSquare.charAt(0) - 48;
                int j = selectedSquare.charAt(1) - 48;
                switch (customBoard[i][j].toLowerCase()) {
                    case "q":
                        points += 9;
                        break;
                    case "r":
                        points += 5;
                        break;
                    case "b":
                        points += 3;
                        break;
                    case "n":
                        points += 3;
                        break;
                    case "p":
                        points += 1;
                        break;
                    default:
                        break;
                }
                customBoard[i][j] = " ";
                showPoints();
                drawCustomBoard(gc1);
            }
        });
        HBox buttons = new HBox();
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(50);
        buttons.getChildren().addAll(submit, delete);
        container.getChildren().addAll(boardContainer, buttons);
        currentStackPane.getChildren().add(container);
        return currentStackPane;
    }

    private boolean checkBattleMode(String line) throws IOException {
        if (line.equals("Battle")) {
            stage.getScene().setRoot(getBattlePlan(stage));
            return true;
        }
        return false;
    }

    private void addPieceComponents(VBox pieces) {
        VBox textContainer = new VBox();
        textContainer.setAlignment(Pos.CENTER);
        Text txt1 = new Text("Points");
        Text txt2 = new Text(String.valueOf(points));
        txt1.setFont(Font.font("Verdana", FontPosture.ITALIC, 24));
        txt2.setFont(Font.font("Verdana", FontPosture.ITALIC, 24));
        textContainer.getChildren().addAll(txt1, txt2);
        pieces.getChildren().add(textContainer);
        if (playerNr == '1') {
            pieces.getChildren().add(getPiece('K', "0"));
            pieces.getChildren().add(getPiece('Q', "9"));
            pieces.getChildren().add(getPiece('R', "5"));
            pieces.getChildren().add(getPiece('B', "3"));
            pieces.getChildren().add(getPiece('N', "3"));
            pieces.getChildren().add(getPiece('P', "1"));
        } else {
            pieces.getChildren().add(getPiece('k', "0"));
            pieces.getChildren().add(getPiece('q', "9"));
            pieces.getChildren().add(getPiece('r', "5"));
            pieces.getChildren().add(getPiece('b', "3"));
            pieces.getChildren().add(getPiece('n', "3"));
            pieces.getChildren().add(getPiece('p', "1"));
        }
    }

    private HBox getPiece(char p, String pts) {
        HBox piece = new HBox();
        piece.setAlignment(Pos.CENTER);
        Text txt = new Text(pts);
        txt.setTextAlignment(TextAlignment.CENTER);
        txt.setFont(Font.font("Verdana", FontPosture.ITALIC, 24));
        HBox pieceImage = new HBox();
        pieceImage.getChildren().add(new ImageView(images.get(p)));
        pieceImage.setOnMouseClicked(e -> {
            pieceSelected = p;
        });
        piece.getChildren().addAll(pieceImage, txt);
        return piece;
    }

    private void drawBoardBattle(GraphicsContext gc) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i < 6) {
                    if ((i + j) % 2 == 0) {
                        gc.setFill(Color.rgb((int) (210 * 0.15), (int) (105 * 0.15), (int) (30 * 0.15)));
                    } else {
                        gc.setFill(Color.rgb((int) (255 * 0.15), (int) (222 * 0.15), (int) (173 * 0.15)));
                    }

                    gc.fillRect(j * 100, i * 100, 100, 100);
                } else {
                    if ((i + j) % 2 == 0) {
                        gc.setFill(Color.rgb((int) (210), (int) (105), (int) (30)));
                    } else {
                        gc.setFill(Color.rgb((int) (255), (int) (222), (int) (173)));
                    }
                    gc.fillRect(j * 100, i * 100, 100, 100);
                }
            }
        }
    }

    private void drawCustomBoard(GraphicsContext gc1) {
        drawBoardBattle(gc1);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                if (customBoard[i][j].toLowerCase().charAt(0) >= 'a' && customBoard[i][j].toLowerCase().charAt(0) <= 'z') {
                    gc1.drawImage(images.get(customBoard[i][j].charAt(0)), j * 100, (i + 6) * 100, 100, 100);
                }
            }
        }
    }

    private void showHistory() {
        currentStackPane.getChildren().stream().filter((n) -> (n instanceof VBox)).forEachOrdered((n) -> {
            ((VBox) n).getChildren().stream().filter((m) -> (m instanceof HBox)).forEachOrdered((Node m) -> {
                if (((HBox) m).getChildren().size() == 2) {
                    ((HBox) m).getChildren().stream().filter((k) -> (k instanceof StackPane)).forEachOrdered((Node k) -> {
                        ((StackPane) k).getChildren().stream().filter((t) -> (t instanceof Text)).forEachOrdered((Node t) -> {
                            Platform.runLater(() -> {
                                String txtToShow = "\n";
                                for (int i = 0; i < history.size(); i++) {
                                    String s = history.get(i);
                                    if (s.length() == 10) {
                                        txtToShow += (i + 1) + ". " + s.substring(0, 5) + " - " + s.substring(5, 10) + "\n\n";
                                    } else {
                                        txtToShow += (i + 1) + ". " + s.substring(0, 5);
                                    }
                                }
                                ((Text) t).setText(txtToShow);
                            });
                        });
                    });
                }
            });
        });
    }

    private void showText(String line) {
        Text txt = new Text(" " + line + " ");
        txt.setFont(Font.font("Verdana", FontPosture.ITALIC, 25));
        VBox textContainer = new VBox();
        textContainer.setMaxSize(130, 100);
        textContainer.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50;");
        textContainer.getChildren().add(txt);
        removeText();
        currentStackPane.getChildren().stream().filter((n) -> (n instanceof VBox)).forEachOrdered((n) -> {
            Platform.runLater(() -> {
                ((VBox) n).getChildren().add(textContainer);
            });
        });
    }

    private void removeText() {
        currentStackPane.getChildren().stream().filter((n) -> (n instanceof VBox)).forEachOrdered((n) -> {
            ((VBox) n).getChildren().stream().filter((m) -> (m instanceof VBox)).forEachOrdered((m) -> {
                Platform.runLater(() -> {
                    ((VBox) n).getChildren().remove(m);
                });
            });
        });
    }

    private void deactivateButton() {
        currentStackPane.getChildren().stream().filter((n) -> (n instanceof VBox)).forEachOrdered((n) -> {
            ((VBox) n).getChildren().stream().filter((m) -> (m instanceof HBox)).forEachOrdered((m) -> {
                ((HBox) m).getChildren().stream().filter((k) -> (k instanceof Button)).forEachOrdered((k) -> {
                    Platform.runLater(() -> {
                        ((Button) k).setDisable(true);
                    });
                });
            });
        });
    }

    private void showPoints() {
        currentStackPane.getChildren().stream().filter((n) -> (n instanceof VBox)).forEachOrdered((n) -> {
            ((VBox) n).getChildren().stream().filter((m) -> (m instanceof HBox)).forEachOrdered((m) -> {
                ((HBox) m).getChildren().stream().filter((k) -> (k instanceof VBox)).forEachOrdered((k) -> {
                    ((VBox) k).getChildren().stream().filter((l) -> (l instanceof VBox)).forEachOrdered((l) -> {
                        ((VBox) l).getChildren().stream().filter((o) -> (o instanceof Text)).forEachOrdered((o) -> {
                            Platform.runLater(() -> {
                                if (!((Text) o).getText().equals("Points")) {
                                    ((Text) o).setText(String.valueOf(points));
                                }
                            });
                        });
                    });
                });
            });
        });
    }

    static String moveToNotation(String move) {
        String moveString = "";
        moveString += "" + (char) (move.charAt(1) + 49);
        moveString += "" + ('8' - move.charAt(0));
        moveString += "" + (char) (move.charAt(3) + 49);
        moveString += "" + ('8' - move.charAt(2));
        moveString += move.charAt(4);
        return moveString;
    }

    private String convertToOtherSide(String move) {
        String convertedMove;
        int x0 = move.charAt(0) - 48;
        int y0 = move.charAt(1) - 48;
        int x1 = move.charAt(2) - 48;
        int y1 = move.charAt(3) - 48;
        x0 = 7 - x0;
        y0 = 7 - y0;
        x1 = 7 - x1;
        y1 = 7 - y1;
        convertedMove = String.valueOf(x0) + String.valueOf(y0) + String.valueOf(x1) + String.valueOf(y1) + move.charAt(4);
        return convertedMove;
    }

    private void addToHistory(String lm) {
        if (!"-----".equals(lm)) {
            if (playerNr == '1') {
                if (!history.isEmpty() && history.get(history.size() - 1).length() == 5) {
                    history.set(history.size() - 1, history.get(history.size() - 1) + moveToNotation(lm));
                } else {
                    history.add(moveToNotation(lm));
                }
            } else {
                if (!history.isEmpty() && history.get(history.size() - 1).length() == 5) {
                    history.set(history.size() - 1, history.get(history.size() - 1) + moveToNotation(convertToOtherSide(lm)));
                } else {
                    history.add(moveToNotation(convertToOtherSide(lm)));
                }
            }
        } else if(!history.isEmpty() || playerNr == '2'){
            if (!history.isEmpty() && history.get(history.size() - 1).length() == 5) {
                history.set(history.size() - 1, history.get(history.size() - 1) + " ??? ");
            } else {
                history.add(" ??? ");
            }
        }
    }
}
