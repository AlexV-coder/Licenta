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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
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
    ClientThread clientThread;
    String colorChosen = "white";
    String games = "";

    int id;
    int modeChosen = 0;
    int modeChosenSingle = 0;
    int diffChosen = 0;

    String[] modes = {"Regular", "Fog of War", "Battle Plan", "King of The Hill"};
    String[] diffs = {"Easy", "Medium", "Hard"};
    String[] modesSingle = {"Regular", "Battle Plan", "King of The Hill"};

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
            stage.getScene().setRoot(getPlayCompMenu(stage));
        });
        singlePlayer.setStyle("-fx-base: rgba(139, 69, 19, 0.89); -fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");
        singlePlayer.setText("Single Player");
        singlePlayer.setMinSize(250, 75);
        Button multiPlayer = new Button();
        multiPlayer.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMultiplayerMenu(stage));
        });
        multiPlayer.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");
        multiPlayer.setMinSize(250, 75);
        multiPlayer.setText("Multiplayer");
        Button quit = new Button();
        quit.setOnMouseClicked(e -> {
            stage.close();
            try {
                out.println("Quit");
                out.flush();
                socket.close();
            } catch (IOException ex) {
                System.out.println(ex.toString());
            }
        });
        quit.setMinSize(250, 75);
        quit.setText("Quit");
        quit.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");
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
        createLobby.setStyle("-fx-base: rgba(139, 69, 19, 0.89); -fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");
        createLobby.setText("Create lobby");
        createLobby.setMinSize(250, 75);
        Button joinLobby = new Button();
        joinLobby.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getJoinLobbyMenu(stage));
        });
        joinLobby.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");
        joinLobby.setMinSize(250, 75);
        joinLobby.setText("Join lobby");
        Button back = new Button();
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMainMenu(stage));
        });
        back.setMinSize(250, 75);
        back.setText("Back");
        back.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");

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
        menu.setMaxSize(400, 600);

        Button back = new Button();
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMainMenu(stage));
        });
        back.setMinSize(250, 75);
        back.setText("Back");
        back.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");

        Button play = new Button();
        play.setOnMouseClicked(e -> {
            out.println("Single player " + colorChosen + " " + diffs[diffChosen].charAt(0) + " " + modesSingle[modeChosenSingle].split(" ")[0]);
            out.flush();
            clientThread = new ClientThread(root, in, out, stage, 'S');
            clientThread.start();
        });
        play.setMinSize(250, 75);
        play.setText("Play");
        play.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");

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
        mode.setFont(Font.font("Verdana", FontPosture.ITALIC, 24));
        mode.setFill(Color.BLACK);

        Polygon leftTriangle = new Polygon();
        leftTriangle.setStroke(Color.WHITE);
        leftTriangle.setFill(Color.rgb(139, 69, 19));
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
        rightTriangle.setFill(Color.rgb(139, 69, 19));
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

        HBox diffChooser = new HBox();
        diffChooser.setAlignment(Pos.CENTER);
        diffChooser.setSpacing(50);
        Text diff = new Text();
        diff.setText(diffs[diffChosen]);
        diff.setFont(Font.font("Verdana", FontPosture.ITALIC, 24));
        diff.setFill(Color.BLACK);

        Polygon leftTriangle2 = new Polygon();
        leftTriangle2.setStroke(Color.WHITE);
        leftTriangle2.setFill(Color.rgb(139, 69, 19));
        leftTriangle2.getPoints().addAll(new Double[]{
            0.0, 15.0,
            30.0, 0.0,
            30.0, 30.0});
        leftTriangle2.setOnMouseClicked(e -> {
            diffChosen--;
            if (diffChosen < 0) {
                diffChosen = diffs.length - 1;
            }
            diff.setText(diffs[diffChosen]);
        });

        Polygon rightTriangle2 = new Polygon();
        rightTriangle2.setStroke(Color.WHITE);
        rightTriangle2.setFill(Color.rgb(139, 69, 19));
        rightTriangle2.getPoints().addAll(new Double[]{
            160.0, 115.0,
            130.0, 100.0,
            130.0, 130.0});
        rightTriangle2.setOnMouseClicked(e -> {
            diffChosen++;
            if (diffChosen >= diffs.length) {
                diffChosen = 0;
            }
            diff.setText(diffs[diffChosen]);
        });

        diffChooser.getChildren().addAll(leftTriangle2, diff, rightTriangle2);

        menu.getChildren().addAll(play, diffChooser, modeChooser, colorChooser, back);
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
        menu.setMaxSize(450, 850);

        TextField tf = new TextField();
        tf.setPromptText("Lobby ID");
        tf.setMaxWidth(250);
        tf.setPadding(new Insets(15, 0, 15, 0));
        tf.setStyle("-fx-font-weight: bold; -fx-font-size: 24pt;");

        TextField tf2 = new TextField();
        tf2.setPromptText("Lobby Name");
        tf2.setMaxWidth(250);
        tf2.setPadding(new Insets(15, 0, 15, 0));
        tf2.setStyle("-fx-font-weight: bold; -fx-font-size: 24pt;");

        Button back = new Button();
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMultiplayerMenu(stage));
        });
        back.setMinSize(250, 75);
        back.setText("Back");
        back.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");

        Button create = new Button();
        create.setOnMouseClicked(e -> {
            if (isNumeric(tf.getText())) {
                id = Integer.valueOf(tf.getText());
                String name = tf2.getText();
                Platform.runLater(() -> {
                    try {
                        out.println("Create lobby " + id + " " + colorChosen + " " + modes[modeChosen].split(" ")[0] + " " + name.replaceAll(" ", "@"));
                        out.flush();
                        String line;
                        line = in.readLine();
                        if (line.equals("Ok")) {
                            menu.getChildren().remove(txt);
                            stage.getScene().setRoot(getWaitingMenu(stage));
                            clientThread = new ClientThread(root, in, out, stage, 'M');
                            clientThread.start();
                        } else if (line.equals("Lobby already exists")) {
                            txt.setText(line);
                            menu.getChildren().remove(txt);
                            menu.getChildren().add(txt);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            } else {
                txt.setText("ID must be a number.");
                menu.getChildren().remove(txt);
                menu.getChildren().add(txt);
            }
        });
        create.setMinSize(250, 75);
        create.setText("Create");
        create.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");

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
        mode.setFont(Font.font("Verdana", FontPosture.ITALIC, 24));
        mode.setFill(Color.BLACK);

        Polygon leftTriangle = new Polygon();
        leftTriangle.setStroke(Color.WHITE);
        leftTriangle.setFill(Color.rgb(139, 69, 19));
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
        rightTriangle.setFill(Color.rgb(139, 69, 19));
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
        menu.getChildren().addAll(tf2, tf, create, modeChooser, colorChooser, back);
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
        menu.setPadding(new Insets(30, 30, 30, 30));
        menu.setMaxSize(1000, 800);
        menu.setAlignment(Pos.TOP_CENTER);
        root.getChildren().add(menu);

        HBox buttons = new HBox();
        buttons.setSpacing(600);
        buttons.setAlignment(Pos.CENTER);
        Button back = new Button();
        back.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getMultiplayerMenu(stage));
        });
        back.setMinSize(200, 50);
        back.setText("Back");
        back.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");

        Button refresh = new Button();
        refresh.setOnMouseClicked(e -> {
            stage.getScene().setRoot(getJoinLobbyMenu(stage));
        });
        refresh.setMinSize(200, 50);
        refresh.setText("Refresh");
        refresh.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");

        buttons.getChildren().addAll(refresh, back);
        menu.getChildren().add(buttons);

        VBox displayGames = new VBox();
        displayGames.setSpacing(100);
        displayGames.setPrefHeight(850);
        displayGames.setAlignment(Pos.TOP_CENTER);
        ScrollPane sp = new ScrollPane();
        sp.setFitToHeight(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        sp.setContent(displayGames);
        menu.getChildren().add(sp);

        Platform.runLater(() -> {
            getGames();
            if (!"".equals(games)) {
                String[] allGames = games.split("/");
                for (String g : allGames) {
                    String ID = g.split(" ")[0];
                    String name = g.split(" ")[1].replaceAll("@", " ");
                    String mod = g.split(" ")[2].replaceAll("@", " ");
                    String p1color = g.split(" ")[3];

                    HBox game = new HBox();
                    game.setAlignment(Pos.CENTER);
                    game.setSpacing(50);

                    HBox lobbyInfo = new HBox();
                    lobbyInfo.setMinSize(800, 200);
                    lobbyInfo.setStyle("-fx-background-color: rgba(139, 69, 19, 1); -fx-background-radius: 50 50 50 50; -fx-border-color: black; -fx-border-radius: 50 50 50 50; -fx-border-width: 3px");

                    VBox nameAndId = new VBox();
                    nameAndId.setSpacing(100);
                    nameAndId.setAlignment(Pos.CENTER);
                    nameAndId.setPrefSize(250, 200);
                    Text t = new Text();
                    t.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
                    t.setText("ID: " + ID);

                    Text t2 = new Text();
                    t2.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
                    t2.setText("Name: " + name);
                    nameAndId.getChildren().addAll(t, t2);

                    VBox modeAndColor = new VBox();
                    modeAndColor.setAlignment(Pos.CENTER);
                    modeAndColor.setSpacing(50);
                    modeAndColor.setPrefSize(550, 200);
                    Text t3 = new Text();
                    t3.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
                    t3.setText("Mode: " + mod);

                    HBox colorChooser = new HBox();
                    colorChooser.setAlignment(Pos.CENTER);
                    ImageView imageView = new ImageView();
                    File file2;
                    if (p1color.equals("white")) {
                        file2 = new File("Pieces\\W_King.png");
                    } else {
                        file2 = new File("Pieces\\B_King.png");
                    }
                    Image image = new Image(file2.toURI().toString());
                    imageView.setImage(image);
                    colorChooser.getChildren().add(imageView);
                    modeAndColor.getChildren().addAll(t3, colorChooser);

                    lobbyInfo.getChildren().addAll(nameAndId, modeAndColor);

                    Button join = new Button();
                    join.setMinSize(100, 50);
                    join.setText("Join");
                    join.setStyle("-fx-base: rgba(139, 69, 19, 0.89);-fx-faint-focus-color: transparent; -fx-focus-color:rgba(125, 96, 32, 1); -fx-font-weight: bold; -fx-font-size: 11pt; -fx-background-radius: 50 50 50 50;");
                    join.setOnMouseClicked(e -> {
                        String line;
                        try {
                            out.println("Join lobby " + ID);
                            out.flush();
                            line = in.readLine();
                            if (line.equals("Ok")) {
                                menu.getChildren().remove(txt);
                                clientThread = new ClientThread(root, in, out, stage, 'M');
                                clientThread.start();
                            } else if (line.equals("Lobby does not exist") || line.equals("Lobby is already full")) {
                                txt.setText(line);
                                menu.getChildren().remove(txt);
                                menu.getChildren().add(txt);
                            }
                        } catch (IOException ex) {
                            System.out.println(ex.toString());
                        }
                    });
                    game.getChildren().addAll(lobbyInfo, join);

                    displayGames.getChildren().add(game);
                }
            }
        });

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

    private void getGames() {
        try {
            out.println("Get lobbies");
            out.flush();
            games = in.readLine();
        } catch (IOException ex) {
            System.out.println("Error communicating with the server");
        }
    }
}
