package space.crab8012.cardgameserver;

import space.crab8012.cardgameplayer.gameobjects.Player;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    static int x = 0;
    public static void main(String[] args){
        while(x < 5){
            simpleObjects();
            x++;
        }
    }
    public static void simpleStrings(){
        try{
            ServerSocket ss = new ServerSocket(8888);
            Socket s = ss.accept();
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            String str = (String)dis.readUTF();
            System.out.println("MSG: " + str);
            dout.writeUTF("SERVER_RESPONSE");
            dout.flush();

            s.close();
            ss.close();
        }catch(Exception e){
            e.printStackTrace();
        }
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
