package space.crab8012.cardgameserver;

import space.crab8012.cardgameplayer.gameobjects.ServerCommand;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;

public class RPSClientHandler extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    ObjectInputStream ois;
    ObjectOutputStream oos;

    SynchronousQueue queue;

    public RPSClientHandler(Socket s,DataInputStream dis, DataOutputStream dos, SynchronousQueue queue){
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
            ServerCommand c = (ServerCommand)queue.take(); //Send GETPLAYER command
            queue.put(ois.readObject()); //Read the player name.
            oos.writeObject(queue.take());//Tries to get a move from the client
            queue.put(ois.readObject()); //Sends the client's move to the server.
            oos.writeObject(queue.take());//Tries to send the client the winning player.
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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

    public void sendFirstPlayer(){
        System.out.println("SENDFIRSTPLAYER TO " + s);
    }
}
