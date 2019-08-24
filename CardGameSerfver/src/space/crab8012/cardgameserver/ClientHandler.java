package space.crab8012.cardgameserver;

import space.crab8012.cardgameplayer.gameobjects.ServerCommand;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;

public class ClientHandler extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    ObjectInputStream ois;
    ObjectOutputStream oos;

    SynchronousQueue queue;

    public ClientHandler(Socket s,DataInputStream dis, DataOutputStream dos, SynchronousQueue queue){
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
            ServerCommand c = (ServerCommand)queue.take();


            queue.put(ois.readObject()); //Reads from client the Player Object
            oos.writeObject(queue.take());//Sends to client the GameState Object
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
