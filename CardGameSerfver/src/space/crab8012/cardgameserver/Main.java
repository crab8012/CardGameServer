package space.crab8012.cardgameserver;

import space.crab8012.cardgameplayer.gameobjects.GameMode;
import space.crab8012.cardgameplayer.gameobjects.GameState;
import space.crab8012.cardgameplayer.gameobjects.Player;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.SynchronousQueue;

public class Main {
    static int x = 0;

    static GameState gs;
    static Scanner commandProcessor;

    static ServerSocket ss;
    public static SynchronousQueue queue;

    public static ArrayList<Thread> threads;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Game Server");
        System.out.println("Creating Queue");
        queue = new SynchronousQueue(true);
        System.out.println("Queue Created");

        threads = new ArrayList();

        System.out.println("Creating Game");
        gs = new GameState();
        gs.setGameName("TEST SERVER GAME");
        gs.setMaxGameSize(2);
        System.out.println("Game Created");

        System.out.println("Waiting for Players");

        Socket s = null;
        ss = new ServerSocket(8888);
        while(gs.getNumConnectedPlayers() < gs.getMaxGameSize()) {
            try {
                s = ss.accept();
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                //Create new thread object
                System.out.println("Player " + s + "Connected");
                Thread t = new ClientHandler(s, dis, dos, queue);
                t.start(); //Start the thread
                gs.addPlayer((Player) queue.take()); // Add the player sent from the client to the gamestate.
                queue.put(gs); // Send the updated gamestate to the client
                threads.add(t);
            } catch (Exception e) {
                s.close();
                e.printStackTrace();
            }
        }

        for (Player player : gs.getPlayers()) {
            System.out.println("----PLAYER RECIEVED----");
            System.out.println("NAME: " + player.getName());
            System.out.println("ICON: " + player.getIcon());
            System.out.println("SCORE: " + player.getScore());
            System.out.println("-----------------------");
        }

        System.out.println("\n\n----SERVER CLOSED----");

        //gameLoop();
    }

    public static void gameLoop(){

        commandProcessor = new Scanner(System.in);
        //Setup Networking threads to add players to the lobby
        while(!commandProcessor.hasNext()){
        }

        try {
            gs.createDeck(); //Create the deck of cards
            gs.dealAllCards(); //Give all players cards
            Player firstPlayer = gs.getFirstPlayer(gs.getPlayers());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        //Threads notify that Player is the first player.
        // Clients compare the recieved Player object to their own.
        // this will signal that it is the client's turn to play.
        // then the game state will be sent to all of the clients
        // The player whose turn it is will modify and return the game state
        // all other players will update their games with the game state and discard it.

        // All clients who wish to call BS will send a gameCall object.
        // The server will interpret this object and run code accordingly.
        // The server will then repeat the send procedure:
        // update game state, send game state to players, send current player

    }

    public static void simpleObjects(){
        try{
            ServerSocket ss = new ServerSocket(8888);
            System.out.println("Server socket waiting for connection on port 8888");

            Socket s = ss.accept();
            System.out.println("Connection from " + s);

            //Get the input stream and the object stream
            InputStream is = s.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            //Get the item from the stream
            Player recievedPlayer = (Player)ois.readObject();

            System.out.println("----PLAYER RECIEVED----");
            System.out.println("Name: " + recievedPlayer.getName());
            System.out.println("Icon: " + recievedPlayer.getIcon());
            System.out.println("Score: " + recievedPlayer.getScore());
            System.out.println("-----------------------");

            ois.close();
            is.close();
            ss.close();
            s.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
