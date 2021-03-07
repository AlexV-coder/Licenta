/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Alex
 */
public class Server {

    static volatile List<Socket> sockets = new ArrayList<>();
    static volatile List<Game> games = Collections.synchronizedList(new ArrayList<>());
    static volatile List<Player> players = Collections.synchronizedList(new ArrayList<>());
    static final int PORT = 8888;

    public static void main(String args[]) {
        ServerSocket serverSocket = null;
        System.out.println("Awaiting connections...");
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println(e);
        }
        if (serverSocket != null) {
            while (true) {
                try {
                    sockets.add(serverSocket.accept());
                    System.out.println("Connection successful.");
                } catch (IOException e) {
                    System.out.println("I/O error: " + e);
                }
                new ServerThread(games, players, sockets.get(sockets.size() - 1)).start();
            }
        }
    }
}
