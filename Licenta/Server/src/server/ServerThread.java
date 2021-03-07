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

/**
 *
 * @author Alex
 */
public class ServerThread extends Thread {

    private final Socket socket;
    private volatile List<Game> games = Collections.synchronizedList(new ArrayList<>());
    private volatile List<Player> players = Collections.synchronizedList(new ArrayList<>());
    private final Player player;
    private volatile Game game = null;

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
                while (((line = in.readLine()) == null)) {
                    //wait
                }
                if ((line == null) || line.equals("Quit")) {
                    socket.close();
                    break;
                } else if (line.contains("Create lobby")) {
                    createLobby(line, in, out);
                } else if (line.contains("Join lobby")) {
                    joinLobby(line, in, out);
                } else if (line.contains(("Single player"))) {
                    createSinglePlayerGame(line, in, out);
                }
            } catch (IOException ex) {
                break;
            }

        }
    }

    private void createLobby(String line, BufferedReader in, PrintWriter out) {
        this.game = new Game();
        int id = Integer.valueOf(line.split(" ")[2]);
        String colorChosen = line.split(" ")[3];
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
        if (colorChosen.equals("black")) {
            game.player2Color = "white";
        } else {
            game.player2Color = "black";
        }
        game.player1 = player;
        Utility.arrayToBitboards(game.board, game.bitboards);
        synchronized (games) {
            games.add(game);
        }
        while (game.player2 == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                System.out.println(ex.toString());
            }
        }
        try {
            startMultiplayerGame(in, out);
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
                        games.get(i).gameOn = true;
                        game = games.get(i);
                    }
                }
            }
            startMultiplayerGame(in, out);
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        }
    }

    private void startMultiplayerGame(BufferedReader in, PrintWriter out) throws InterruptedException {

        if (player == game.player1) {
            if ("black".equals(game.player1Color)) {
                out.println("2");
                out.flush();
                sendBlackBoard(out, game.board);
            } else {
                out.println("1");
                out.flush();
                sendWhiteBoard(out, game.board);
            }
        } else {
            if ("black".equals(game.player1Color)) {
                out.println("1");
                out.flush();
                sendWhiteBoard(out, game.board);
            } else {
                out.println("2");
                out.flush();
                sendBlackBoard(out, game.board);
            }
        }
        while (game.gameOn) {
            if (player == game.player1) {
                try {
                    if ("black".equals(game.player1Color)) {
                        playBlack(in, out);
                    } else {
                        playWhite(in, out);
                    }
                } catch (IOException ex) {
                    game.gameOn = false;
                    game.winner = game.player2Color;
                }
            } else if (player == game.player2) {
                try {
                    if ("black".equals(game.player2Color)) {
                        playBlack(in, out);
                    } else {
                        playWhite(in, out);
                    }
                } catch (IOException ex) {
                    game.gameOn = false;
                    game.winner = game.player1Color;
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

    private void sendWhiteBoard(PrintWriter out, String[][] board) {
        String brd = "";
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                brd += board[i][j];
            }
        }
        out.println(game.turn + brd + game.lastMove);
        out.flush();
    }

    private void sendBlackBoard(PrintWriter out, String[][] board) {
        String brd = "";
        for (int i = 7; i >= 0; i--) {
            for (int j = 7; j >= 0; j--) {
                brd += board[i][j];
            }
        }
        if (game.lastMove.equals("     ")) {
            out.println(game.turn + brd + game.lastMove);
        } else {
            out.println(game.turn + brd + convertToOtherSide(game.lastMove));
        }
        out.flush();
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
        while (game.turn != '1' && game.gameOn) {
            Thread.sleep(100);
        }
        if (!game.gameOn) {
            return;
        }
        sendWhiteBoard(out, game.board);
        legalMoves = MoveGenerator.generateMovesW(game.bitboards);
        int gameOver = whiteLost(legalMoves);
        if (gameOver == -1) {
            game.winner = "black";
            game.gameOn = false;
            return;
        } else if (gameOver == 1) {
            game.winner = "draw";
            game.gameOn = false;
            return;
        }
        boolean legal = false;
        while (!legal) {
            while (((move = in.readLine()) == null)) {
                //wait
            }
            if (move.equals("Quit") || move.equals("Resign")) {
                game.gameOn = false;
                game.winner = "black";
                break;
            }
            for (int i = 0; i < legalMoves.length(); i += 5) {
                if (move.equals(legalMoves.substring(i, i + 5))) {
                    MoveGenerator.makeMove(game.bitboards, move);
                    Utility.bitboardsToArray(game.board, game.bitboards);
                    game.turn = '2';
                    game.lastMove = move;
                    sendWhiteBoard(out, game.board);
                    legal = true;
                    break;
                }
            }
        }
    }

    private void playBlack(BufferedReader in, PrintWriter out) throws InterruptedException, IOException {
        String move;
        String legalMoves;
        while (game.turn != '2' && game.gameOn) {
            Thread.sleep(100);
        }
        if (!game.gameOn) {
            return;
        }
        sendBlackBoard(out, game.board);
        legalMoves = MoveGenerator.generateMovesB(game.bitboards);
        int gameOver = blackLost(legalMoves);
        if (gameOver == -1) {
            game.winner = "white";
            game.gameOn = false;
            return;
        } else if (gameOver == 1) {
            game.winner = "draw";
            game.gameOn = false;
            return;
        }
        boolean legal = false;
        while (!legal) {
            while (((move = in.readLine()) == null)) {
                //wait
            }
            if (move.equals("Quit") || move.equals("Resign")) {
                game.gameOn = false;
                game.winner = "white";
                break;
            }
            move = convertToOtherSide(move);
            for (int i = 0; i < legalMoves.length(); i += 5) {
                if (move.equals(legalMoves.substring(i, i + 5))) {
                    MoveGenerator.makeMove(game.bitboards, move);
                    Utility.bitboardsToArray(game.board, game.bitboards);
                    game.turn = '1';
                    game.lastMove = move;
                    sendBlackBoard(out, game.board);
                    legal = true;
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
        synchronized (games) {
            if (games.stream().anyMatch((g) -> (g.id == id))) {
                return true;
            }
        }
        return false;
    }

    private boolean lobbyIsFull(int id) {
        synchronized (games) {
            if (games.stream().anyMatch((g) -> (g.id == id && g.player2 != null))) {
                return true;
            }
        }
        return false;
    }

    private void createSinglePlayerGame(String line, BufferedReader in, PrintWriter out) {
        String diff = line.split(" ")[3];
        String colorChosen = line.split(" ")[2];
        this.game = new Game();
        game.player1Color = colorChosen;
        if (colorChosen.equals("black")) {
            game.player2Color = "white";
        } else {
            game.player2Color = "black";
        }
        game.player1 = player;
        game.gameOn = true;
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
        PC pc = new PC(game.bitboards, side, depth);
        synchronized (games) {
            games.add(game);
        }
        try {
            startSinglePlayerGame(in, out, pc);
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
        } finally {
            synchronized (games) {
                games.remove(game);
            }
        }
    }

    private void startSinglePlayerGame(BufferedReader in, PrintWriter out, PC pc) throws InterruptedException {
        if ("black".equals(game.player1Color)) {
            out.println("2");
            out.flush();
            sendBlackBoard(out, game.board);
        } else {
            out.println("1");
            out.flush();
            sendWhiteBoard(out, game.board);
        }

        while (game.gameOn) {
            if (game.turn == '1' && game.player1Color.equals("white")) {
                try {
                    playWhite(in, out);
                } catch (IOException ex) {
                    game.gameOn = false;
                    game.winner = game.player2Color;
                    break;
                }
            } else if (game.turn == '2' && game.player1Color.equals("black")) {
                try {
                    playBlack(in, out);
                } catch (IOException ex) {
                    game.gameOn = false;
                    game.winner = game.player2Color;
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
                    game.gameOn = false;
                    break;
                } else if (gameOver == 1) {
                    game.winner = "draw";
                    game.gameOn = false;
                    break;
                }
                //pc.bitboards = new Bitboards(game.bitboards);
                if (!(game.lastMove.equals("     "))) {
                    MoveGenerator.makeMove(pc.bitboards, game.lastMove);
                }
                String pcMove = pc.move();
                MoveGenerator.makeMove(game.bitboards, pcMove);
                Utility.bitboardsToArray(game.board, game.bitboards);
                game.lastMove = pcMove;
                if (game.turn == '1') {
                    game.turn = '2';
                } else {
                    game.turn = '1';
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
}
