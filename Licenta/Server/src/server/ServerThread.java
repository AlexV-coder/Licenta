/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static server.MoveGenerator.CENTRE;
import static server.MoveGenerator.generateMovesBFog;
import static server.MoveGenerator.generateMovesWFog;

/**
 *
 * @author Alex
 */
public class ServerThread extends Thread {

    private final Socket socket;
    private volatile List<Game> games = Collections.synchronizedList(new ArrayList<>());
    private volatile List<Player> players = Collections.synchronizedList(new ArrayList<>());
    private final Player player;
    private volatile Game game;

    public ServerThread(List<Game> games, List<Player> players, Socket clientSocket) {
        this.socket = clientSocket;
        synchronized (games) {
            this.games = games;
        }
        synchronized (players) {
            this.players = players;
            player = new Player();
            player.id = players.size();
            players.add(player);
        }
    }

    @Override
    public void run() {
        BufferedReader in;
        PrintWriter out;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                line = in.readLine();
                if ((line == null) || line.equals("Quit")) {
                    socket.close();
                    break;
                } else if (line.contains("Create lobby")) {
                    createLobby(line, in, out);
                } else if (line.contains("Join lobby")) {
                    joinLobby(line, in, out);
                } else if (line.contains("Get lobbies")) {
                    sendLobbies(in, out);
                } else if (line.contains(("Single player"))) {
                    createSinglePlayerGame(line, in, out);
                }
            } catch (IOException ex) {
                break;
            }

        }
        players.remove(player);
    }

    private void createLobby(String line, BufferedReader in, PrintWriter out) {
        this.game = new Game();
        int id = Integer.valueOf(line.split(" ")[2]);
        String colorChosen = line.split(" ")[3];
        String mode = line.split(" ")[4];
        String name = line.split(" ")[5];
        if (lobbyExists(id)) {
            out.println("Lobby already exists");
            out.flush();
            return;
        } else {
            out.println("Ok");
            out.flush();
        }
        game.id = id;
        game.player1Color = colorChosen;
        game.mode = mode;
        game.name = name;
        if (colorChosen.equals("black")) {
            game.player2Color = "white";
        } else {
            game.player2Color = "black";
        }
        game.player1 = player;
        if (mode.equals("Battle")) {
            game.resetBoard();
        }
        Utility.arrayToBitboards(game.board, game.bitboards);
        games.add(game);
        while (game.player2 == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                System.out.println(ex.toString());
            }
        }
        try {
            switch (mode) {
                case "Regular":
                    startMultiplayerGame(in, out);
                    break;
                case "Fog":
                    startMultiplayerGameFog(in, out);
                    break;
                case "Battle":
                    startMultiplayerGameBattle(in, out);
                    break;
                case "King":
                    startMultiplayerGameKing(in, out);
                    break;
                default:
                    break;
            }
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        } finally {
            games.remove(game);
        }
    }

    private void joinLobby(String line, BufferedReader in, PrintWriter out) {
        try {
            int id = Integer.valueOf(line.split(" ")[2]);
            if (!lobbyExists(id)) {
                out.println("Lobby does not exist");
                out.flush();
                return;
            } else if (lobbyIsFull(id)) {
                out.println("Lobby is already full");
                out.flush();
                return;
            } else {
                out.println("Ok");
                out.flush();
            }
            synchronized (games) {
                for (int i = 0; i < games.size(); i++) {
                    if (games.get(i).id == id) {
                        games.get(i).player2 = player;
                        games.get(i).gameOn.set(true);
                        game = games.get(i);
                    }
                }
            }
            switch (game.mode) {
                case "Regular":
                    startMultiplayerGame(in, out);
                    break;
                case "Fog":
                    startMultiplayerGameFog(in, out);
                    break;
                case "Battle":
                    startMultiplayerGameBattle(in, out);
                    break;
                case "King":
                    startMultiplayerGameKing(in, out);
                default:
                    break;
            }
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }
    }

    private void startMultiplayerGame(BufferedReader in, PrintWriter out) throws InterruptedException {

        if (player == game.player1) {
            if ("black".equals(game.player1Color)) {
                out.println("2");
                out.flush();
                sendBlackBoard(out, game.board, '1');
            } else {
                out.println("1");
                out.flush();
                sendWhiteBoard(out, game.board, '1');
            }
        } else {
            if ("black".equals(game.player1Color)) {
                out.println("1");
                out.flush();
                sendWhiteBoard(out, game.board, '1');
            } else {
                out.println("2");
                out.flush();
                sendBlackBoard(out, game.board, '1');
            }
        }
        while (game.gameOn.get()) {
            if (player == game.player1) {
                try {
                    if ("black".equals(game.player1Color)) {
                        playBlack(in, out);
                    } else {
                        playWhite(in, out);
                    }
                } catch (IOException ex) {
                    game.winner = game.player2Color;
                    game.gameOn.set(false);
                }
            } else if (player == game.player2) {
                try {
                    if ("black".equals(game.player2Color)) {
                        playBlack(in, out);
                    } else {
                        playWhite(in, out);
                    }
                } catch (IOException ex) {
                    game.winner = game.player1Color;
                    game.gameOn.set(false);
                }
            }
        }
        String result;
        if (game.winner.equals("draw")) {
            result = "draw";
        } else {
            if (game.player1 == player) {
                if (game.winner.equals(game.player1Color)) {
                    result = "victory";
                } else {
                    result = "loss";
                }
            } else {
                if (game.winner.equals(game.player2Color)) {
                    result = "victory";
                } else {
                    result = "loss";
                }
            }
        }
        out.println(result);
        out.flush();
    }

    private void sendWhiteBoard(PrintWriter out, String[][] board, char t) {
        String brd = "";
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                brd += board[i][j];
            }
        }
        out.println(t + brd + game.lastMove);
        out.flush();
    }

    private void sendBlackBoard(PrintWriter out, String[][] board, char t) {
        String brd = "";
        for (int i = 7; i >= 0; i--) {
            for (int j = 7; j >= 0; j--) {
                brd += board[i][j];
            }
        }
        if (game.lastMove.equals("-----")) {
            out.println(t + brd + game.lastMove);
            out.flush();
        } else {
            out.println(t + brd + convertToOtherSide(game.lastMove));
            out.flush();
        }
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

    private void playWhite(BufferedReader in, PrintWriter out) throws InterruptedException, IOException {
        String move;
        String legalMoves;
        while (!game.turn.get() && game.gameOn.get()) {
            Thread.sleep(100);
        }
        if (!game.gameOn.get()) {
            return;
        }
        sendWhiteBoard(out, game.board, '1');
        legalMoves = MoveGenerator.generateMovesW(game.bitboards);
        int gameOver = whiteLost(legalMoves);
        if (gameOver == -1) {
            game.winner = "black";
            game.gameOn.set(false);
            return;
        } else if (gameOver == 1) {
            game.winner = "draw";
            game.gameOn.set(false);
            return;
        }
        boolean legal = false;
        while (!legal) {
            move = in.readLine();
            if (move.equals("Quit") || move.equals("Resign")) {
                game.winner = "black";
                game.gameOn.set(false);
                break;
            }
            if (move.equals("Suggest")) {
                Bitboards tempBits = new Bitboards(game.bitboards);
                PC tempPc = new PC(tempBits, -1, 8, game.mode);
                String suggestedMove = tempPc.move();
                out.println("Suggestion");
                out.flush();
                out.println(suggestedMove);
                out.flush();
                continue;
            }
            for (int i = 0; i < legalMoves.length(); i += 5) {
                if (move.equals(legalMoves.substring(i, i + 5))) {
                    MoveGenerator.makeMove(game.bitboards, move);
                    Utility.bitboardsToArray(game.board, game.bitboards);
                    game.lastMove = move;
                    sendWhiteBoard(out, game.board, '2');
                    legal = true;
                    game.turn.set(false);
                    break;
                }
            }
        }
    }

    private void playBlack(BufferedReader in, PrintWriter out) throws InterruptedException, IOException {
        String move;
        String legalMoves;
        while (game.turn.get() && game.gameOn.get()) {
            Thread.sleep(100);
        }
        if (!game.gameOn.get()) {
            return;
        }
        sendBlackBoard(out, game.board, '2');
        legalMoves = MoveGenerator.generateMovesB(game.bitboards);
        int gameOver = blackLost(legalMoves);
        if (gameOver == -1) {
            game.winner = "white";
            game.gameOn.set(false);
            return;
        } else if (gameOver == 1) {
            game.winner = "draw";
            game.gameOn.set(false);
            return;
        }
        boolean legal = false;
        while (!legal) {
            move = in.readLine();
            if (move.equals("Quit") || move.equals("Resign")) {
                game.winner = "white";
                game.gameOn.set(false);
                break;
            }
            if (move.equals("Suggest")) {
                Bitboards tempBits = new Bitboards(game.bitboards);
                PC tempPc = new PC(tempBits, 1, 8, game.mode);
                String suggestedMove = tempPc.move();
                out.println("Suggestion");
                out.flush();
                out.println(suggestedMove);
                out.flush();
                continue;
            }
            move = convertToOtherSide(move);
            for (int i = 0; i < legalMoves.length(); i += 5) {
                if (move.equals(legalMoves.substring(i, i + 5))) {
                    MoveGenerator.makeMove(game.bitboards, move);
                    Utility.bitboardsToArray(game.board, game.bitboards);
                    game.lastMove = move;
                    sendBlackBoard(out, game.board, '1');
                    legal = true;
                    game.turn.set(true);
                    break;
                }
            }
        }
    }

    private int whiteLost(String legalMoves) {
        if (legalMoves.isEmpty()) {
            if (MoveGenerator.attackerBoardW(game.bitboards, game.bitboards.WK) == 0) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    private int blackLost(String legalMoves) {
        if (legalMoves.isEmpty()) {
            if (MoveGenerator.attackerBoardB(game.bitboards, game.bitboards.BK) == 0) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    private boolean lobbyExists(int id) {
        return games.stream().anyMatch((g) -> (g.id == id));
    }

    private boolean lobbyIsFull(int id) {
        return games.stream().anyMatch((g) -> (g.id == id && g.player2 != null));
    }

    private void createSinglePlayerGame(String line, BufferedReader in, PrintWriter out) {
        String mode = line.split(" ")[4];
        String diff = line.split(" ")[3];
        String colorChosen = line.split(" ")[2];
        this.game = new Game();
        game.player1Color = colorChosen;
        if (colorChosen.equals("black")) {
            game.player2Color = "white";
        } else {
            game.player2Color = "black";
        }
        if (mode.equals("Battle")) {
            game.resetBoard();
        }
        game.player1 = player;
        game.player2 = new Player();
        game.mode = mode;
        game.gameOn.set(true);
        Utility.arrayToBitboards(game.board, game.bitboards);
        int side, depth;
        if (game.player1Color.equals("white")) {
            side = 1;
        } else {
            side = -1;
        }
        switch (diff) {
            case "E":
                depth = 4;
                break;
            case "M":
                depth = 6;
                break;
            default:
                depth = 8;
                break;
        }
        PC pc = new PC(game.bitboards, side, depth, mode);
        games.add(game);
        try {
            if (mode.equals("King")) {
                startSinglePlayerGameKing(in, out, pc);
            } else {
                startSinglePlayerGame(in, out, pc);
            }
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        } finally {
            games.remove(game);
        }
    }

    private void startSinglePlayerGame(BufferedReader in, PrintWriter out, PC pc) throws InterruptedException {
        if ("black".equals(game.player1Color)) {
            out.println("2");
            out.flush();
        } else {
            out.println("1");
            out.flush();
        }
        if (game.mode.equals("Battle")) {
            if (game.player2Color.equals("white")) {
                setWhiteBoard(pc.generateWhiteBoard());
            } else {
                setBlackBoard(pc.generateBlackBoard());
            }
            out.println("Battle");
            out.flush();
            try {
                waitSinglePieces(in, out);
                pc = new PC(game.bitboards, pc.side, pc.maxDepth, game.mode);
            } catch (IOException ex) {
                game.winner = game.player2Color;
                game.player1SetPieces.set(true);
                game.gameOn.set(false);
                return;
            }
        }

        if ("black".equals(game.player1Color)) {
            sendBlackBoard(out, game.board, '1');
        } else {
            sendWhiteBoard(out, game.board, '1');
        }

        while (game.gameOn.get()) {
            if (game.turn.get() && game.player1Color.equals("white")) {
                try {
                    playWhite(in, out);
                } catch (IOException ex) {
                    game.winner = game.player2Color;
                    game.gameOn.set(false);
                    break;
                }
            } else if (!game.turn.get() && game.player1Color.equals("black")) {
                try {
                    playBlack(in, out);
                } catch (IOException ex) {
                    game.winner = game.player2Color;
                    game.gameOn.set(false);
                    break;
                }
            } else {
                String legalMoves;
                int gameOver;
                if (game.player1Color.equals("white")) {
                    legalMoves = MoveGenerator.generateMovesB(game.bitboards);
                    gameOver = blackLost(legalMoves);
                } else {
                    legalMoves = MoveGenerator.generateMovesW(game.bitboards);
                    gameOver = whiteLost(legalMoves);
                }
                if (gameOver == -1) {
                    game.winner = game.player1Color;
                    game.gameOn.set(false);
                    break;
                } else if (gameOver == 1) {
                    game.winner = "draw";
                    game.gameOn.set(false);
                    break;
                }
                if (!(game.lastMove.equals("-----"))) {
                    MoveGenerator.makeMove(pc.bitboards, game.lastMove);
                }
                String pcMove = pc.move();
                MoveGenerator.makeMove(game.bitboards, pcMove);
                Utility.bitboardsToArray(game.board, game.bitboards);
                game.lastMove = pcMove;
                if (game.turn.get()) {
                    game.turn.set(false);
                } else {
                    game.turn.set(true);
                }
            }
        }
        String result;
        if (game.winner.equals("draw")) {
            result = "draw";
        } else {
            if (game.player1Color.equals(game.winner)) {
                result = "victory";
            } else {
                result = "loss";
            }
        }
        out.println(result);
        out.flush();

    }

    private void startSinglePlayerGameKing(BufferedReader in, PrintWriter out, PC pc) throws InterruptedException {
        if ("black".equals(game.player1Color)) {
            out.println("2");
            out.flush();
        } else {
            out.println("1");
            out.flush();
        }

        if ("black".equals(game.player1Color)) {
            sendBlackBoard(out, game.board, '1');
        } else {
            sendWhiteBoard(out, game.board, '1');
        }

        while (game.gameOn.get()) {
            if (game.turn.get() && game.player1Color.equals("white")) {
                try {
                    playWhiteKing(in, out);
                } catch (IOException ex) {
                    game.winner = game.player2Color;
                    game.gameOn.set(false);
                    break;
                }
            } else if (!game.turn.get() && game.player1Color.equals("black")) {
                try {
                    playBlackKing(in, out);
                } catch (IOException ex) {
                    game.winner = game.player2Color;
                    game.gameOn.set(false);
                    break;
                }
            } else {
                String legalMoves;
                int gameOver;
                if (game.player1Color.equals("white")) {
                    legalMoves = MoveGenerator.generateMovesB(game.bitboards);
                    gameOver = blackLostKing(legalMoves);
                } else {
                    legalMoves = MoveGenerator.generateMovesW(game.bitboards);
                    gameOver = whiteLostKing(legalMoves);
                }
                if (gameOver == -1) {
                    game.winner = game.player1Color;
                    game.gameOn.set(false);
                    break;
                } else if (gameOver == 1) {
                    game.winner = "draw";
                    game.gameOn.set(false);
                    break;
                }
                if (!(game.lastMove.equals("-----"))) {
                    MoveGenerator.makeMove(pc.bitboards, game.lastMove);
                }
                String pcMove = pc.move();
                MoveGenerator.makeMove(game.bitboards, pcMove);
                Utility.bitboardsToArray(game.board, game.bitboards);
                game.lastMove = pcMove;
                if (game.turn.get()) {
                    game.turn.set(false);
                } else {
                    game.turn.set(true);
                }
            }
        }
        String result;
        if (game.winner.equals("draw")) {
            result = "draw";
        } else {
            if (game.player1Color.equals(game.winner)) {
                result = "victory";
            } else {
                result = "loss";
            }
        }
        out.println(result);
        out.flush();

    }

    private void startMultiplayerGameFog(BufferedReader in, PrintWriter out) throws InterruptedException {
        if (player == game.player1) {
            if ("black".equals(game.player1Color)) {
                out.println("2");
                out.flush();
                sendBlackBoardFog(out, game.board, '1');
            } else {
                out.println("1");
                out.flush();
                sendWhiteBoardFog(out, game.board, '1');
            }
        } else {
            if ("black".equals(game.player1Color)) {
                out.println("1");
                out.flush();
                sendWhiteBoardFog(out, game.board, '1');
            } else {
                out.println("2");
                out.flush();
                sendBlackBoardFog(out, game.board, '1');
            }
        }
        while (game.gameOn.get()) {
            if (player == game.player1) {
                try {
                    if ("black".equals(game.player1Color)) {
                        playBlackFog(in, out);
                    } else {
                        playWhiteFog(in, out);
                    }
                } catch (IOException ex) {
                    game.winner = game.player2Color;
                    game.gameOn.set(false);
                }
            } else if (player == game.player2) {
                try {
                    if ("black".equals(game.player2Color)) {
                        playBlackFog(in, out);
                    } else {
                        playWhiteFog(in, out);
                    }
                } catch (IOException ex) {
                    game.winner = game.player1Color;
                    game.gameOn.set(false);
                }
            }
        }
        String result;
        if (game.winner.equals("draw")) {
            result = "draw";
        } else {
            if (game.player1 == player) {
                if (game.winner.equals(game.player1Color)) {
                    result = "victory";
                } else {
                    result = "loss";
                }
            } else {
                if (game.winner.equals(game.player2Color)) {
                    result = "victory";
                } else {
                    result = "loss";
                }
            }
        }
        out.println(result);
        out.flush();
    }

    private void sendWhiteBoardFog(PrintWriter out, String[][] board, char t) {
        String legalMoves = generateMovesWFog(game.bitboards);
        List<String> legalSquares = new ArrayList<>();
        for (int i = 0; i < legalMoves.length(); i += 5) {
            legalSquares.add(legalMoves.substring(i + 2, i + 4));
        }
        String brd = "";
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String square = String.valueOf(i) + String.valueOf(j);
                if ((board[i][j].charAt(0) >= 'A' && board[i][j].charAt(0) <= 'Z') || legalSquares.indexOf(square) != -1) {
                    brd += board[i][j];
                } else {
                    brd += "0";
                }
            }
        }
        if (t == '2') {
            out.println(t + brd + game.lastMove);
            out.flush();
        } else {
            out.println(t + brd + "-----");
            out.flush();
        }
    }

    private void sendBlackBoardFog(PrintWriter out, String[][] board, char t) {
        String legalMoves = generateMovesBFog(game.bitboards);
        List<String> legalSquares = new ArrayList<>();
        for (int i = 0; i < legalMoves.length(); i += 5) {
            legalSquares.add(legalMoves.substring(i + 2, i + 4));
        }
        String brd = "";
        for (int i = 7; i >= 0; i--) {
            for (int j = 7; j >= 0; j--) {
                String square = String.valueOf(i) + String.valueOf(j);
                if ((board[i][j].charAt(0) >= 'a' && board[i][j].charAt(0) <= 'z') || legalSquares.indexOf(square) != -1) {
                    brd += board[i][j];
                } else {
                    brd += "0";
                }
            }
        }
        if (t == '1') {
            if (game.lastMove.equals("-----")) {
                out.println(t + brd + game.lastMove);
                out.flush();
            } else {
                out.println(t + brd + convertToOtherSide(game.lastMove));
                out.flush();
            }
        } else {
            out.println(t + brd + "-----");
            out.flush();
        }
    }

    private void playWhiteFog(BufferedReader in, PrintWriter out) throws InterruptedException, IOException {
        String move;
        String legalMoves;
        while (!game.turn.get() && game.gameOn.get()) {
            Thread.sleep(100);
        }
        if (!game.gameOn.get()) {
            return;
        }
        sendWhiteBoardFog(out, game.board, '1');
        legalMoves = generateMovesWFog(game.bitboards);
        int gameOver = whiteLostFog();
        if (gameOver == -1) {
            game.winner = "black";
            game.gameOn.set(false);
            return;
        } else if (gameOver == 1) {
            game.winner = "draw";
            game.gameOn.set(false);
            return;
        }
        boolean legal = false;
        while (!legal) {
            move = in.readLine();
            if (move.equals("Quit") || move.equals("Resign")) {
                game.winner = "black";
                game.gameOn.set(false);
                break;
            }
            for (int i = 0; i < legalMoves.length(); i += 5) {
                if (move.equals(legalMoves.substring(i, i + 5))) {
                    MoveGenerator.makeMove(game.bitboards, move);
                    Utility.bitboardsToArray(game.board, game.bitboards);
                    game.lastMove = move;
                    sendWhiteBoardFog(out, game.board, '2');
                    legal = true;
                    game.turn.set(false);
                    break;
                }
            }
        }
    }

    private void playBlackFog(BufferedReader in, PrintWriter out) throws InterruptedException, IOException {
        String move;
        String legalMoves;
        while (game.turn.get() && game.gameOn.get()) {
            Thread.sleep(100);
        }
        if (!game.gameOn.get()) {
            return;
        }
        sendBlackBoardFog(out, game.board, '2');
        legalMoves = generateMovesBFog(game.bitboards);
        int gameOver = blackLostFog();
        if (gameOver == -1) {
            game.winner = "white";
            game.gameOn.set(false);
            return;
        } else if (gameOver == 1) {
            game.winner = "draw";
            game.gameOn.set(false);
            return;
        }
        boolean legal = false;
        while (!legal) {
            move = in.readLine();
            if (move.equals("Quit") || move.equals("Resign")) {
                game.winner = "white";
                game.gameOn.set(false);
                break;
            }
            move = convertToOtherSide(move);
            for (int i = 0; i < legalMoves.length(); i += 5) {
                if (move.equals(legalMoves.substring(i, i + 5))) {
                    MoveGenerator.makeMove(game.bitboards, move);
                    Utility.bitboardsToArray(game.board, game.bitboards);
                    game.lastMove = move;
                    sendBlackBoardFog(out, game.board, '1');
                    legal = true;
                    game.turn.set(true);
                    break;
                }
            }
        }
    }

    private int whiteLostFog() {
        if (game.bitboards.WK == 0) {
            return -1;
        }
        return 0;
    }

    private int blackLostFog() {
        if (game.bitboards.BK == 0) {
            return -1;
        }
        return 0;
    }

    private void startMultiplayerGameBattle(BufferedReader in, PrintWriter out) throws InterruptedException {
        if (player == game.player1) {
            if ("black".equals(game.player1Color)) {
                out.println("2");
                out.flush();
            } else {
                out.println("1");
                out.flush();
            }
        } else {
            if ("black".equals(game.player1Color)) {
                out.println("1");
                out.flush();
            } else {
                out.println("2");
                out.flush();
            }
        }
        out.println("Battle");
        out.flush();
        try {
            waitPieces(in, out);
        } catch (IOException ex) {
            if (player == game.player1) {
                game.winner = game.player2Color;
                game.player1SetPieces.set(true);
            } else {
                game.winner = game.player1Color;
                game.player2SetPieces.set(true);
            }
            game.gameOn.set(false);
            return;
        }
        if (player == game.player1) {
            if ("black".equals(game.player1Color)) {
                sendBlackBoard(out, game.board, '1');
            } else {
                sendWhiteBoard(out, game.board, '1');
            }
        } else {
            if ("black".equals(game.player1Color)) {
                sendWhiteBoard(out, game.board, '1');
            } else {
                sendBlackBoard(out, game.board, '1');
            }
        }
        while (game.gameOn.get()) {
            if (player == game.player1) {
                try {
                    if ("black".equals(game.player1Color)) {
                        playBlack(in, out);
                    } else {
                        playWhite(in, out);
                    }
                } catch (IOException ex) {
                    game.winner = game.player2Color;
                    game.gameOn.set(false);
                }
            } else if (player == game.player2) {
                try {
                    if ("black".equals(game.player2Color)) {
                        playBlack(in, out);
                    } else {
                        playWhite(in, out);
                    }
                } catch (IOException ex) {
                    game.winner = game.player1Color;
                    game.gameOn.set(false);
                }
            }
        }
        String result;
        if (game.winner.equals("draw")) {
            result = "draw";
        } else {
            if (game.player1 == player) {
                if (game.winner.equals(game.player1Color)) {
                    result = "victory";
                } else {
                    result = "loss";
                }
            } else {
                if (game.winner.equals(game.player2Color)) {
                    result = "victory";
                } else {
                    result = "loss";
                }
            }
        }
        out.println(result);
        out.flush();
    }

    private void waitPieces(BufferedReader in, PrintWriter out) throws IOException, InterruptedException {
        boolean legal = false;
        String line;
        while (!legal) {
            line = in.readLine();
            if (line.equals("Quit")) {
                if (player == game.player1) {
                    if (game.player1Color.equals("white")) {
                        game.winner = "black";
                    } else {
                        game.winner = "white";
                    }
                    game.player1SetPieces.set(true);
                } else {
                    if (game.player2Color.equals("white")) {
                        game.winner = "black";
                    } else {
                        game.winner = "white";
                    }
                    game.player2SetPieces.set(true);
                }
                game.gameOn.set(false);
                return;
            }
            if (player == game.player1) {
                if (game.player1Color.equals("white")) {
                    if (isLegalCompositionWhite(line, out)) {
                        setWhiteBoard(line);
                        legal = true;
                        game.player1SetPieces.set(true);
                    }
                } else if (isLegalCompositionBlack(line, out)) {
                    setBlackBoard(line);
                    legal = true;
                    game.player1SetPieces.set(true);
                }
            } else {
                if (game.player2Color.equals("black")) {
                    if (isLegalCompositionBlack(line, out)) {
                        setBlackBoard(line);
                        legal = true;
                        game.player2SetPieces.set(true);
                    }
                } else if (isLegalCompositionWhite(line, out)) {
                    setWhiteBoard(line);
                    legal = true;
                    game.player2SetPieces.set(true);
                }
            }
        }
        while (!game.player1SetPieces.get() || !game.player2SetPieces.get()) {
            Thread.sleep(100);
        }
        if ((player == game.player1 && game.player1Color.equals("white")) || (player == game.player2 && game.player2Color.equals("white"))) {
            Utility.arrayToBitboards(game.board, game.bitboards);
        }
    }

    private void setWhiteBoard(String line) {
        synchronized (game.board) {
            for (int i = 6; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    game.board[i][j] = String.valueOf(line.charAt((i - 6) * 8 + j));
                }
            }
        }
    }

    private void setBlackBoard(String line) {
        line = new StringBuilder(line.substring(8, line.length())).reverse().toString() + new StringBuilder(line.substring(0, 8)).reverse().toString();
        synchronized (game.board) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 8; j++) {
                    game.board[i][j] = String.valueOf(line.charAt(i * 8 + j));
                }
            }
        }
    }

    private boolean isLegalCompositionWhite(String line, PrintWriter out) {
        boolean king = false;
        for (int i = 0; i < line.length(); i++) {
            if ("K".equals(String.valueOf(line.charAt(i)))) {
                if (king) {
                    out.println("Only one king is allowed.");
                    out.flush();
                    return false;
                }
                king = true;
                if (i < 8) {
                    out.println("King must be on the first rank.");
                    out.flush();
                    return false;
                } else if (String.valueOf(line.charAt(i - 8)).equals(" ")) {
                    out.println("King must have a piece in front of it.");
                    out.flush();
                    return false;
                }
            }
            if ("P".equals(String.valueOf(line.charAt(i)))) {
                if (i >= 8) {
                    out.println("Pawns must be on the second rank.");
                    out.flush();
                    return false;
                }
            }
        }
        if (!king) {
            out.println("You must have a king piece.");
            out.flush();
            return false;
        }
        out.println("Valid");
        out.flush();
        return true;
    }

    private boolean isLegalCompositionBlack(String line, PrintWriter out) {
        line = line.substring(8, line.length()) + line.substring(0, 8);
        boolean king = false;
        for (int i = 0; i < line.length(); i++) {
            if ("k".equals(String.valueOf(line.charAt(i)))) {
                if (king) {
                    out.println("Only one king is allowed.");
                    out.flush();
                    return false;
                }
                king = true;
                if (i >= 8) {
                    out.println("King must be on the eighth rank.");
                    out.flush();
                    return false;
                } else if (String.valueOf(line.charAt(i + 8)).equals(" ")) {
                    out.println("King must have a piece in front of it.");
                    out.flush();
                    return false;
                }
            }
            if ("p".equals(String.valueOf(line.charAt(i)))) {
                if (i < 8) {
                    out.println("Pawns must be on the seventh rank.");
                    out.flush();
                    return false;
                }
            }
        }
        if (!king) {
            out.println("You must have a king piece.");
            out.flush();
            return false;
        }
        out.println("Valid");
        out.flush();
        return true;
    }

    private void waitSinglePieces(BufferedReader in, PrintWriter out) throws IOException, InterruptedException {
        boolean legal = false;
        String line;
        while (!legal) {
            line = in.readLine();
            if (line.equals("Quit")) {
                if (game.player1Color.equals("white")) {
                    game.winner = "black";
                } else {
                    game.winner = "white";
                }
                game.player1SetPieces.set(true);
                game.gameOn.set(false);
                return;
            }
            if (game.player1Color.equals("white")) {
                if (isLegalCompositionWhite(line, out)) {
                    setWhiteBoard(line);
                    legal = true;
                    game.player1SetPieces.set(true);
                }
            } else if (isLegalCompositionBlack(line, out)) {
                setBlackBoard(line);
                legal = true;
                game.player1SetPieces.set(true);
            }

        }
        Utility.arrayToBitboards(game.board, game.bitboards);
    }

    private void startMultiplayerGameKing(BufferedReader in, PrintWriter out) throws InterruptedException {

        if (player == game.player1) {
            if ("black".equals(game.player1Color)) {
                out.println("2");
                out.flush();
                sendBlackBoard(out, game.board, '1');
            } else {
                out.println("1");
                out.flush();
                sendWhiteBoard(out, game.board, '1');
            }
        } else {
            if ("black".equals(game.player1Color)) {
                out.println("1");
                out.flush();
                sendWhiteBoard(out, game.board, '1');
            } else {
                out.println("2");
                out.flush();
                sendBlackBoard(out, game.board, '1');
            }
        }
        while (game.gameOn.get()) {
            if (player == game.player1) {
                try {
                    if ("black".equals(game.player1Color)) {
                        playBlackKing(in, out);
                    } else {
                        playWhiteKing(in, out);
                    }
                } catch (IOException ex) {
                    game.winner = game.player2Color;
                    game.gameOn.set(false);
                }
            } else if (player == game.player2) {
                try {
                    if ("black".equals(game.player2Color)) {
                        playBlackKing(in, out);
                    } else {
                        playWhiteKing(in, out);
                    }
                } catch (IOException ex) {
                    game.winner = game.player1Color;
                    game.gameOn.set(false);
                }
            }
        }
        String result;
        if (game.winner.equals("draw")) {
            result = "draw";
        } else {
            if (game.player1 == player) {
                if (game.winner.equals(game.player1Color)) {
                    result = "victory";
                } else {
                    result = "loss";
                }
            } else {
                if (game.winner.equals(game.player2Color)) {
                    result = "victory";
                } else {
                    result = "loss";
                }
            }
        }
        out.println(result);
        out.flush();
    }

    private void playWhiteKing(BufferedReader in, PrintWriter out) throws InterruptedException, IOException {
        String move;
        String legalMoves;
        while (!game.turn.get() && game.gameOn.get()) {
            Thread.sleep(100);
        }
        if (!game.gameOn.get()) {
            return;
        }
        sendWhiteBoard(out, game.board, '1');
        legalMoves = MoveGenerator.generateMovesW(game.bitboards);
        int gameOver = whiteLostKing(legalMoves);
        if (gameOver == -1) {
            game.winner = "black";
            game.gameOn.set(false);
            return;
        } else if (gameOver == 1) {
            game.winner = "draw";
            game.gameOn.set(false);
            return;
        }
        boolean legal = false;
        while (!legal) {
            move = in.readLine();
            if (move.equals("Quit") || move.equals("Resign")) {
                game.winner = "black";
                game.gameOn.set(false);
                break;
            }
            if (move.equals("Suggest")) {
                Bitboards tempBits = new Bitboards(game.bitboards);
                PC tempPc = new PC(tempBits, -1, 8, game.mode);
                String suggestedMove = tempPc.move();
                out.println("Suggestion");
                out.flush();
                out.println(suggestedMove);
                out.flush();
                continue;
            }
            for (int i = 0; i < legalMoves.length(); i += 5) {
                if (move.equals(legalMoves.substring(i, i + 5))) {
                    MoveGenerator.makeMove(game.bitboards, move);
                    Utility.bitboardsToArray(game.board, game.bitboards);
                    game.lastMove = move;
                    sendWhiteBoard(out, game.board, '2');
                    legal = true;
                    game.turn.set(false);
                    break;
                }
            }
        }
    }

    private void playBlackKing(BufferedReader in, PrintWriter out) throws InterruptedException, IOException {
        String move;
        String legalMoves;
        while (game.turn.get() && game.gameOn.get()) {
            Thread.sleep(100);
        }
        if (!game.gameOn.get()) {
            return;
        }
        sendBlackBoard(out, game.board, '2');
        legalMoves = MoveGenerator.generateMovesB(game.bitboards);
        int gameOver = blackLostKing(legalMoves);
        if (gameOver == -1) {
            game.winner = "white";
            game.gameOn.set(false);
            return;
        } else if (gameOver == 1) {
            game.winner = "draw";
            game.gameOn.set(false);
            return;
        }
        boolean legal = false;
        while (!legal) {
            move = in.readLine();
            if (move.equals("Quit") || move.equals("Resign")) {
                game.winner = "white";
                game.gameOn.set(false);
                break;
            }
            if (move.equals("Suggest")) {
                Bitboards tempBits = new Bitboards(game.bitboards);
                PC tempPc = new PC(tempBits, 1, 8, game.mode);
                String suggestedMove = tempPc.move();
                out.println("Suggestion");
                out.flush();
                out.println(suggestedMove);
                out.flush();
                continue;
            }
            move = convertToOtherSide(move);
            for (int i = 0; i < legalMoves.length(); i += 5) {
                if (move.equals(legalMoves.substring(i, i + 5))) {
                    MoveGenerator.makeMove(game.bitboards, move);
                    Utility.bitboardsToArray(game.board, game.bitboards);
                    game.lastMove = move;
                    sendBlackBoard(out, game.board, '1');
                    legal = true;
                    game.turn.set(true);
                    break;
                }
            }
        }
    }

    private int whiteLostKing(String legalMoves) {
        if ((CENTRE & game.bitboards.BK) != 0) {
            return -1;
        }
        if (legalMoves.isEmpty()) {
            if (MoveGenerator.attackerBoardW(game.bitboards, game.bitboards.WK) == 0) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    private int blackLostKing(String legalMoves) {
        if ((CENTRE & game.bitboards.WK) != 0) {
            return -1;
        }
        if (legalMoves.isEmpty()) {
            if (MoveGenerator.attackerBoardB(game.bitboards, game.bitboards.BK) == 0) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    private void sendLobbies(BufferedReader in, PrintWriter out) {
        String openGames = "";
        synchronized (games) {
            for (Game g : games) {
                if (g.player2 == null) {
                    String mod;
                    if (g.mode.equals("King")) {
                        mod = "King@of@The@Hill";
                    } else if(g.mode.equals("Fog")) {
                        mod = "Fog@of@War";
                    } else if(g.mode.equals("Battle")) {
                        mod = "Battle@Plan";
                    } else {
                        mod = "Regular";
                    }
                    openGames += g.id + " " + g.name + " " + mod + " " + g.player1Color + "/";
                }
            }
        }
        out.println(openGames);
        out.flush();
    }
}
