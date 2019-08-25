package space.crab8012.cardgameserver;

import space.crab8012.cardgameplayer.gameobjects.Enums;
import space.crab8012.cardgameplayer.gameobjects.ServerCommand;
import space.crab8012.cardgameplayer.payloads.GameWinnerPayload;
import space.crab8012.cardgameplayer.payloads.Payload;
import space.crab8012.cardgameplayer.payloads.PlayerMovePayload;
import space.crab8012.cardgameplayer.payloads.PlayerObjectPayload;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RPSClientHandler extends Thread {
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private final Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    //The expected Payloads
    private GameWinnerPayload gameWinnerPayload = null;
    private PlayerMovePayload playerMovePayload = null;
    private PlayerObjectPayload playerObjectPayload = null;

    //The commands to send
    private ServerCommand getPlayerCommand = new ServerCommand(Enums.COMMANDS.GETPLAYER.name());
    private ServerCommand getMoveCommand = new ServerCommand(Enums.COMMANDS.GETMOVE.name());
    private ServerCommand sendWinnerCommand = new ServerCommand(Enums.COMMANDS.SENDWINNER.name());

    ConcurrentLinkedQueue<ServerCommand> queue;

    public RPSClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, ConcurrentLinkedQueue<ServerCommand> queue){
        this.queue = queue;
        this.dis = dis;
        this.dos = dos;
        this.s = s;
        try {
            ois = new ObjectInputStream(this.dis);
            oos = new ObjectOutputStream(this.dos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {
            //Sending SendPlayer Command
            oos.writeObject(getPlayerCommand);
            while(playerObjectPayload == null){
                Payload tempPayload = ((ServerCommand)ois.readObject()).getPayload();
                if(tempPayload != null) {
                    if (tempPayload instanceof PlayerObjectPayload) {
                        playerObjectPayload = (PlayerObjectPayload) tempPayload;
                        System.out.println("Gotten Player Object");
                    } else {
                        oos.writeObject(getPlayerCommand);
                    }
                }
            }

            //Sending GetPlayerMove command
            oos.writeObject(getMoveCommand);
            while(playerMovePayload == null){
                Payload tempPayload = ((ServerCommand)ois.readObject()).getPayload();
                if(tempPayload != null) {
                    if (tempPayload instanceof PlayerMovePayload) {
                        playerMovePayload = (PlayerMovePayload) tempPayload;
                        System.out.println("Gotten Player Move");
                    } else {
                        oos.writeObject(getMoveCommand);
                    }
                }
            }

            System.out.println("Waiting for Game Winner");
            waitForGameWinner();

            System.out.println("Sending Game Winner");
            sendWinnerCommand.setCommand(Enums.COMMANDS.SENDWINNER.name());
            sendWinnerCommand.setPayload(gameWinnerPayload);
            oos.writeObject(new ServerCommand(Enums.COMMANDS.SENDWINNER.name(), gameWinnerPayload));

            ServerCommand quitCommand = new ServerCommand(Enums.COMMANDS.QUIT.name());
            oos.writeObject(quitCommand);


        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            ois.close();
            oos.close();
            dis.close();
            dos.close();
            s.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void waitForGameWinner(){
        while(gameWinnerPayload == null){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(gameWinnerPayload == null) {
            waitForGameWinner();
        }
    }

    public void sendWinner(GameWinnerPayload payload){
        this.gameWinnerPayload = payload;
    }

    public PlayerObjectPayload getPlayerObject(){
        return this.playerObjectPayload;
    }

    public PlayerMovePayload getPlayerMovePayload(){
        return this.playerMovePayload;
    }
}