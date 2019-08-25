package space.crab8012.cardgameserver;

import com.sun.security.ntlm.Server;
import space.crab8012.cardgameplayer.gameobjects.*;
import space.crab8012.cardgameplayer.payloads.GameWinnerPayload;
import space.crab8012.cardgameplayer.payloads.PlayerMovePayload;
import space.crab8012.cardgameplayer.payloads.PlayerObjectPayload;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

public class Main {
    static int x = 0;

    static GameState gs;
    static Scanner commandProcessor;

    static ServerSocket ss;
    public static ConcurrentLinkedQueue<ServerCommand> queue;

    public static ArrayList<RPSClientHandler> clients;

    public static ArrayList<PlayerMovePayload> playerMoves = new ArrayList();

    public static void main(String[] args) throws IOException {
        rpsServer();
    }

    public static void rpsServer() throws IOException{
        System.out.println("Starting Game Server");
        System.out.println("Creating Queue");
        queue = new ConcurrentLinkedQueue<ServerCommand>();
        System.out.println("Queue Created");

        clients = new ArrayList();

        System.out.println("Creating Game");
        gs = new GameState();
        gs.setGameName("Rock Paper Scissors");
        gs.setMaxGameSize(2); //Creates a 2 Player Game.
        System.out.println("Game Created");

        System.out.println("Waiting for Players");

        Socket s = null;
        ss = new ServerSocket(8888);
        while(clients.size() < gs.getMaxGameSize()) {
            try {
                s = ss.accept();
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                //Create new thread object
                System.out.println("Client" + s.getInetAddress().getAddress().toString() + "Connected");
                RPSClientHandler t = new RPSClientHandler(s, dis, dos, queue);

                t.start(); //Start the thread
                System.out.println("Thread Started");
                clients.add(t);
            } catch (Exception e) {
                if(s != null){
                    s.close();
                }
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (RPSClientHandler c : clients) {
            PlayerMovePayload p = c.getPlayerMovePayload();

            System.out.println("----PLAYER RECIEVED----");
            System.out.println("NAME: " + p.getPlayer().getName());
            System.out.println("ICON: " + p.getPlayer().getIcon());
            System.out.println("SCORE: " + p.getPlayer().getScore());
            System.out.println("-----------------------");

            playerMoves.add(p);
        }

        PlayerMovePayload winner = null;
        for(PlayerMovePayload payload : playerMoves){
            if(winner == null){
                winner = payload;
            }else{
                //Command Payload: {Player Object, Player Move}
                Enums.RPSMOVES newPlayerMove = payload.getMove();
                Enums.RPSMOVES winnerPlayerMove = winner.getMove();
                if((newPlayerMove == Enums.RPSMOVES.PAPER && winnerPlayerMove == Enums.RPSMOVES.ROCK) || (newPlayerMove == Enums.RPSMOVES.ROCK && winnerPlayerMove == Enums.RPSMOVES.SCISSORS) || (newPlayerMove == Enums.RPSMOVES.SCISSORS && winnerPlayerMove == Enums.RPSMOVES.PAPER)){
                    winner = payload;
                }
            }
        }

        GameWinnerPayload winnerPayload = new GameWinnerPayload(winner.getPlayer(), winner.getMove());

        for (RPSClientHandler t : clients) {
            t.sendWinner(winnerPayload);
        }

        System.out.println("\n\n----GAME OVER----");

        //gameLoop();
    }

    /*
    public static void cardgameserver() throws IOException{
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
                ServerCommand getPlayerCommand = new ServerCommand(COMMANDS.GETPLAYER.name(), null);
                queue.put(getPlayerCommand);
                //gs.addPlayer((Player) queue.take()); // Add the player sent from the client to the gamestate.
                ArrayList<Object> payload = new ArrayList();
                payload.add(gs);
                ServerCommand updateGameStateCommand = new ServerCommand(COMMANDS.UPDATEGAMESTATE.name(), payload);
                //queue.put(gs); // Send the updated gamestate to the client
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
    */
}
