package jabberserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class JabberServer implements Runnable {

    private static final int PORT_NUMBER = 44444;
    private static ServerSocket serverSocket;
    private static JabberDatabase db;

    public JabberServer() {
        new Thread(this).start();
    }

    public static void main(String argv[]) {
        JabberServer jabberServer = new JabberServer();
    }

    @Override
    public void run() {

        //moved this outside while true loop to see if this fixes error

        try {
            serverSocket = new ServerSocket(PORT_NUMBER);
            serverSocket.setSoTimeout(300);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //below system out println for testing purposes

        System.out.println("Server connected to " + PORT_NUMBER + " now connecting to Jabber Database");

        db = new JabberDatabase();
        db.getConnection();

        //below system out println for testing purposes

        System.out.println("Connection to database is successful");

        while (true){

            Socket clientSocket = null;

            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                //e.printStackTrace();
                //System.out.println("No connection from client after 300ms - timeout");
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (clientSocket != null){
                ClientConnection client = new ClientConnection(clientSocket, new JabberDatabase());
            }
        }
    }


}
