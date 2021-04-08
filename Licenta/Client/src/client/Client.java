/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Alex
 */
public class Client extends Application {

    Socket socket;
    PrintWriter out;
    BufferedReader in;
    int id;
    String difficulty = "M";
    ClientThread clientThread;
    String colorChosen = "white";
    int modeChosen = 0;
    int modeChosenSingle = 0;
    String[] modes = {"Regular", "Fog of War", "Battle Plan"};
    String[] modesSingle = {"Regular", "Battle Plan"};

    public Client() {
        try {
            InetAddress host = InetAddress.getLocalHost();
            socket = new Socket(host.getHostName(), 8888);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (UnknownHostException ex) {
            System.out.println(ex.toString());
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Chess");
        stage.setFullScreen(true);
        StackPane root = getMainMenu(stage);
        Scene scene = new Scene(root);
        stage.setOnCloseRequest(e -> {
            try {
                out.println("Quit");
                out.flush();
                socket.close();
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        });
        stage.setScene(scene);
        stage.show();
    }

    public StackPane getMainMenu(Stage stage) {
        StackPane root = new StackPane();
        File file = new File("bg.jpg");
        BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString(), 1920, 1080, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));
        Button singlePlayer = new Button();
        singlePlayer.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getSingleMenu(stage));
        });
        singlePlayer.setStyle("-fx-base: rgba(145, 111, 38, 0.89); -fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        singlePlayer.setText("Single Player");
        singlePlayer.setMinSize(250, 75);
        Button multiPlayer = new Button();
        multiPlayer.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMultiplayerMenu(stage));
        });
        multiPlayer.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        multiPlayer.setMinSize(250, 75);
        multiPlayer.setText("Multiplayer");
        Button quit = new Button();
        quit.setOnMouseClicked(e -> {
            out.println("Quit");
            out.flush();
            try {
                socket.close();
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
            stage.close();
        });
        quit.setMinSize(250, 75);
        quit.setText("Quit");
        quit.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        VBox menu = new VBox();
        menu.setMaxSize(400, 500);
        menu.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-color: black;");
        menu.setSpacing(100);
        menu.setAlignment(Pos.CENTER);
        menu.getChildren().add(singlePlayer);
        menu.getChildren().add(multiPlayer);
        menu.getChildren().add(quit);
        root.getChildren().add(menu);
        return root;
    }

    private StackPane getSingleMenu(Stage stage) {
        StackPane root = new StackPane();
        File file = new File("bg.jpg");
        BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString(), 1920, 1080, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));
        VBox diff = new VBox();
        diff.setMaxSize(400, 700);
        diff.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-color: black;");
        diff.setAlignment(Pos.CENTER);
        diff.setSpacing(100);

        Button easy = new Button();
        Button medium = new Button();
        Button hard = new Button();

        easy.setOnMouseClicked(e -> {
            difficulty = "E";
            stage.getScene().setRoot(getPlayCompMenu(stage));
        });
        easy.setStyle("-fx-base: rgba(145, 111, 38, 0.89); -fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        easy.setText("Easy");
        easy.setMinSize(250, 75);

        medium.setOnMouseClicked(e -> {
            difficulty = "M";
            stage.getScene().setRoot(getPlayCompMenu(stage));
        });
        medium.setStyle("-fx-base: rgba(145, 111, 38, 0.89); -fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        medium.setText("Medium");
        medium.setMinSize(250, 75);

        hard.setOnMouseClicked(e -> {
            difficulty = "H";
            stage.getScene().setRoot(getPlayCompMenu(stage));
        });
        hard.setStyle("-fx-base: rgba(145, 111, 38, 0.89); -fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        hard.setText("Hard");
        hard.setMinSize(250, 75);

        Button back = new Button();
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMainMenu(stage));
        });
        back.setMinSize(250, 75);
        back.setText("Back");
        back.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");

        diff.getChildren().addAll(easy, medium, hard, back);
        root.getChildren().add(diff);
        return root;
    }

    private StackPane getMultiplayerMenu(Stage stage) {
        StackPane root = new StackPane();
        File file = new File("bg.jpg");
        BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString(), 1920, 1080, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));
        Button createLobby = new Button();
        createLobby.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getCreateLobbyMenu(stage));
        });
        createLobby.setStyle("-fx-base: rgba(145, 111, 38, 0.89); -fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        createLobby.setText("Create lobby");
        createLobby.setMinSize(250, 75);
        Button joinLobby = new Button();
        joinLobby.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getJoinLobbyMenu(stage));
        });
        joinLobby.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        joinLobby.setMinSize(250, 75);
        joinLobby.setText("Join lobby");
        Button back = new Button();
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMainMenu(stage));
        });
        back.setMinSize(250, 75);
        back.setText("Back");
        back.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");

        VBox menu = new VBox();
        menu.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-color: black;");
        menu.setSpacing(100);
        menu.setMaxSize(400, 500);
        menu.setAlignment(Pos.CENTER);
        menu.getChildren().add(createLobby);
        menu.getChildren().add(joinLobby);
        menu.getChildren().add(back);
        root.getChildren().add(menu);
        return root;
    }

    private StackPane getPlayCompMenu(Stage stage) {
        StackPane root = new StackPane();
        Text txt = new Text();
        txt.setFont(Font.font("Verdana", FontPosture.ITALIC, 25));
        txt.setFill(Color.RED);
        File file = new File("bg.jpg");
        BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString(), 1920, 1080, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));
        VBox menu = new VBox();
        menu.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-color: black;");
        menu.setSpacing(50);
        menu.setMaxSize(400, 500);

        Button back = new Button();
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getSingleMenu(stage));
        });
        back.setMinSize(250, 75);
        back.setText("Back");
        back.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");

        Button play = new Button();
        play.setOnMouseClicked(e -> {
            out.println("Single player " + colorChosen + " " + difficulty + " " + modesSingle[modeChosenSingle].split(" ")[0]);
            out.flush();
            clientThread = new ClientThread(root, in, out, stage);
            clientThread.start();
        });
        play.setMinSize(250, 75);
        play.setText("Play");
        play.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");

        HBox colorChooser = new HBox();
        colorChooser.setAlignment(Pos.CENTER);
        ImageView imageView = new ImageView();
        if (colorChosen.equals("white")) {
            file = new File("Pieces\\W_King.png");
        } else {
            file = new File("Pieces\\B_King.png");
        }
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);
        colorChooser.getChildren().add(imageView);
        colorChooser.setOnMouseClicked(e -> {
            if (colorChosen.equals("white")) {
                colorChosen = "black";
                File kingFile = new File("Pieces\\B_King.png");
                Image kingImage = new Image(kingFile.toURI().toString());
                imageView.setImage(kingImage);
            } else {
                colorChosen = "white";
                File kingFile = new File("Pieces\\W_King.png");
                Image kingImage = new Image(kingFile.toURI().toString());
                imageView.setImage(kingImage);
            }
        });

        HBox modeChooser = new HBox();
        modeChooser.setAlignment(Pos.CENTER);
        modeChooser.setSpacing(50);
        Text mode = new Text();
        mode.setText(modesSingle[modeChosenSingle]);
        mode.setFont(Font.font("Verdana", 24));
        mode.setFill(Color.BLACK);

        Polygon leftTriangle = new Polygon();
        leftTriangle.setStroke(Color.WHITE);
        leftTriangle.setFill(Color.rgb(125, 96, 32));
        leftTriangle.getPoints().addAll(new Double[]{
            0.0, 15.0,
            30.0, 0.0,
            30.0, 30.0});
        leftTriangle.setOnMouseClicked(e -> {
            modeChosenSingle--;
            if (modeChosenSingle < 0) {
                modeChosenSingle = modesSingle.length - 1;
            }
            mode.setText(modesSingle[modeChosenSingle]);
        });

        Polygon rightTriangle = new Polygon();
        rightTriangle.setStroke(Color.WHITE);
        rightTriangle.setFill(Color.rgb(125, 96, 32));
        rightTriangle.getPoints().addAll(new Double[]{
            160.0, 115.0,
            130.0, 100.0,
            130.0, 130.0});
        rightTriangle.setOnMouseClicked(e -> {
            modeChosenSingle++;
            if (modeChosenSingle >= modesSingle.length) {
                modeChosenSingle = 0;
            }
            mode.setText(modesSingle[modeChosenSingle]);
        });

        modeChooser.getChildren().addAll(leftTriangle, mode, rightTriangle);

        menu.getChildren().addAll(colorChooser, modeChooser, play, back);
        menu.setAlignment(Pos.CENTER);
        root.getChildren().add(menu);
        return root;
    }

    private StackPane getCreateLobbyMenu(Stage stage) {
        StackPane root = new StackPane();
        Text txt = new Text();
        txt.setFont(Font.font("Verdana", FontPosture.ITALIC, 25));
        txt.setFill(Color.RED);
        File file = new File("bg.jpg");
        BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString(), 1920, 1080, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));
        VBox menu = new VBox();
        menu.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-color: black;");
        menu.setSpacing(50);
        menu.setMaxSize(400, 700);

        TextField tf = new TextField();
        tf.setMaxWidth(250);
        tf.setPadding(new Insets(15, 0, 15, 0));
        tf.setStyle("-fx-font-weight: bold; -fx-font-size: 24pt;");

        Button back = new Button();
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMultiplayerMenu(stage));
        });
        back.setMinSize(250, 75);
        back.setText("Back");
        back.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");

        Button create = new Button();
        create.setOnMouseClicked(e -> {
            if (isNumeric(tf.getText())) {
                id = Integer.valueOf(tf.getText());
                String line;
                try {
                    out.println("Create lobby " + id + " " + colorChosen + " " + modes[modeChosen].split(" ")[0]);
                    out.flush();
                    line = in.readLine();
                    if (line.equals("Ok")) {
                        menu.getChildren().remove(txt);
                        stage.getScene().setRoot(getWaitingMenu(stage));
                        clientThread = new ClientThread(root, in, out, stage);
                        clientThread.start();
                    } else if (line.equals("Lobby already exists")) {
                        txt.setText(line);
                        menu.getChildren().remove(txt);
                        menu.getChildren().add(txt);
                    }
                } catch (IOException ex) {
                    System.out.println(ex.toString());
                }
            }
        });
        create.setMinSize(250, 75);
        create.setText("Create");
        create.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");

        HBox colorChooser = new HBox();
        colorChooser.setAlignment(Pos.CENTER);
        ImageView imageView = new ImageView();
        if (colorChosen.equals("white")) {
            file = new File("Pieces\\W_King.png");
        } else {
            file = new File("Pieces\\B_King.png");
        }
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);
        colorChooser.getChildren().add(imageView);
        colorChooser.setOnMouseClicked(e -> {
            if (colorChosen.equals("white")) {
                colorChosen = "black";
                File kingFile = new File("Pieces\\B_King.png");
                Image kingImage = new Image(kingFile.toURI().toString());
                imageView.setImage(kingImage);
            } else {
                colorChosen = "white";
                File kingFile = new File("Pieces\\W_King.png");
                Image kingImage = new Image(kingFile.toURI().toString());
                imageView.setImage(kingImage);
            }
        });

        HBox modeChooser = new HBox();
        modeChooser.setAlignment(Pos.CENTER);
        modeChooser.setSpacing(50);
        Text mode = new Text();
        mode.setText("Regular");
        mode.setFont(Font.font("Verdana", 24));
        mode.setFill(Color.BLACK);

        Polygon leftTriangle = new Polygon();
        leftTriangle.setStroke(Color.WHITE);
        leftTriangle.setFill(Color.rgb(125, 96, 32));
        leftTriangle.getPoints().addAll(new Double[]{
            0.0, 15.0,
            30.0, 0.0,
            30.0, 30.0});
        leftTriangle.setOnMouseClicked(e -> {
            modeChosen--;
            if (modeChosen < 0) {
                modeChosen = modes.length - 1;
            }
            mode.setText(modes[modeChosen]);
        });

        Polygon rightTriangle = new Polygon();
        rightTriangle.setStroke(Color.WHITE);
        rightTriangle.setFill(Color.rgb(125, 96, 32));
        rightTriangle.getPoints().addAll(new Double[]{
            160.0, 115.0,
            130.0, 100.0,
            130.0, 130.0});
        rightTriangle.setOnMouseClicked(e -> {
            modeChosen++;
            if (modeChosen >= modes.length) {
                modeChosen = 0;
            }
            mode.setText(modes[modeChosen]);
        });

        modeChooser.getChildren().addAll(leftTriangle, mode, rightTriangle);
        menu.getChildren().addAll(tf, create, modeChooser, colorChooser, back);
        menu.setAlignment(Pos.CENTER);
        root.getChildren().add(menu);
        return root;
    }

    private StackPane getJoinLobbyMenu(Stage stage) {
        StackPane root = new StackPane();
        File file = new File("bg.jpg");
        Text txt = new Text();
        txt.setFont(Font.font("Verdana", FontPosture.ITALIC, 25));
        txt.setFill(Color.RED);
        BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString(), 1920, 1080, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));
        VBox menu = new VBox();
        menu.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-color: black;");
        menu.setSpacing(50);
        menu.setMaxSize(400, 500);

        TextField tf = new TextField();
        tf.setMaxWidth(250);
        tf.setPadding(new Insets(15, 0, 15, 0));
        tf.setStyle("-fx-font-weight: bold; -fx-font-size: 24pt;");

        Button back = new Button();
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMultiplayerMenu(stage));
        });
        back.setMinSize(250, 75);
        back.setText("Back");
        back.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");

        Button join = new Button();
        join.setOnMouseClicked(e -> {
            if (isNumeric(tf.getText())) {
                id = Integer.valueOf(tf.getText());
                String line;
                try {
                    out.println("Join lobby " + id);
                    out.flush();
                    line = in.readLine();
                    if (line.equals("Ok")) {
                        menu.getChildren().remove(txt);
                        clientThread = new ClientThread(root, in, out, stage);
                        clientThread.start();
                    } else if (line.equals("Lobby does not exist") || line.equals("Lobby is already full")) {
                        txt.setText(line);
                        menu.getChildren().remove(txt);
                        menu.getChildren().add(txt);
                    }
                } catch (IOException ex) {
                    System.out.println(ex.toString());
                }
            }
        });
        join.setMinSize(250, 75);
        join.setText("Join");
        join.setStyle("-fx-base: rgba(145, 111, 38, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt;");
        menu.getChildren().addAll(tf, join, back);
        menu.setAlignment(Pos.CENTER);
        root.getChildren().add(menu);
        return root;
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private StackPane getWaitingMenu(Stage stage) {
        StackPane root = new StackPane();
        File file = new File("bg.jpg");
        Text txt = new Text("Waiting for an opponent to join...");
        txt.setFont(Font.font("Verdana", FontPosture.ITALIC, 25));
        BackgroundImage myBI = new BackgroundImage(new Image(file.toURI().toString(), 1920, 1080, false, true),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        root.setBackground(new Background(myBI));
        VBox textContainer = new VBox();
        textContainer.setMaxSize(600, 100);
        textContainer.setStyle("-fx-background-color: rgba(165, 111, 38, 0.7); -fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-color: black;");
        textContainer.setAlignment(Pos.CENTER);
        textContainer.getChildren().add(txt);
        root.getChildren().add(textContainer);
        return root;
    }
}
