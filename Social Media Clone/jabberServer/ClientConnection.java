package jabberserver;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection implements Runnable {

    private JabberDatabase db;
    private Socket clientSocket;

    PrintWriter toClient = null;
    BufferedReader fromClient = null;

    public ClientConnection(Socket clientSocket, JabberDatabase db) {
        this.clientSocket = clientSocket;
        this.db = db;
        new Thread(this).start();

        //below system out println for testing purposes
        System.out.println("Client connected and new thread started");
    }

    @Override
    public void run(){

        boolean connected = true;

        int clientUserId = 0;

        String clientUsername = null;

        while(connected) {

            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                //no exception printed to console as not always a command given
                //e.printStackTrace();
            }

            JabberMessage request = null;
            try {
                request = (JabberMessage) ois.readObject();
            } catch (IOException e) {
                //no exception printed to console as not always a command given
                //e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            //convert to string and work out what command is being given from client

            //below system out println for testing purposes

            //System.out.println(request.getMessage());

            String requestString = request.getMessage();

            String commandWord = null;

            String stringForCommand = null;

            String[] splitMessageArray = new String[100];

            //calculate number of spaces in message from client

            int spaceCount = 0;

            for (int i = 0; i < requestString.length(); i++) {
                if (requestString.charAt(i) == ' '){
                    spaceCount++;
                }
            }

            //if the spaces is greater than one the message sent from client has data

            if (spaceCount >= 1){

                String[] splitString = requestString.split("\\s+");
                commandWord = splitString[0];
                stringForCommand = splitString[1];
                splitMessageArray = splitString;
            }

            //if no space in message then just consists of action word which can be taken forward

            if (spaceCount == 0){
                commandWord = requestString;
            }

            //initialise object output stream

            ObjectOutputStream oos = null;

            try {
                oos = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            //work out command words and then take appropriate action

            //system out for testing purposes

            //System.out.print("the request being sent from client is: " + commandWord);

            //System.out.print("\n");

            switch (commandWord) {
                case ("signin"):

                    if ((db.getUserID(stringForCommand)) != -1){

                        clientUsername = stringForCommand;

                        clientUserId = db.getUserID(stringForCommand);

                        JabberMessage signedIn = new JabberMessage("signedin");

                        //below system out println for testing purposes

                        System.out.println("Client signin - name: " + clientUsername + " and user id: " + clientUserId);

                        try {
                            oos.writeObject(signedIn);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            oos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        commandWord = null;

                    } else {

                        JabberMessage unknownUser = new JabberMessage("unknown-user");

                        //below system out println for testing purposes

                        System.out.println("Client signin request rejected");

                        try {
                            oos.writeObject(unknownUser);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            oos.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        commandWord = null;
                    }
                    break;
                case("register"):
                    db.addUser(stringForCommand, (stringForCommand + "@gmail.com"));

                    clientUsername = stringForCommand;

                    clientUserId = db.getUserID(stringForCommand);

                    JabberMessage signedIn = new JabberMessage("signedin");

                    //below system out println for testing purposes

                    System.out.println("Client registered then signedin");

                    try {
                        oos.writeObject(signedIn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    commandWord = null;
                    break;
                case("signout"):

                    connected = false;

                    break;
                case("timeline"):

                    ArrayList<ArrayList<String>> usertimeline = new ArrayList<>(db.getTimelineOfUserEx(clientUserId));

                    //below system out println for testing purposes

                    //System.out.println("User requesting timeline is: " + clientUsername);

                    //System.out.println("User id of user requesting timeline is: " + clientUserId);

                    //System.out.println("Timeline data is : " + usertimeline.get(0).get(0));

                    JabberMessage timeline = new JabberMessage("timeline", usertimeline);

                    try {
                        oos.writeObject(timeline);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //below system out println for testing purposes

                    //System.out.println("Timeline for user " + clientUsername + " sent to client");

                    break;
                case("users"):

                    ArrayList<ArrayList<String>> whoToFollow = new ArrayList<>();

                    whoToFollow = db.getUsersNotFollowed(clientUserId);

                    JabberMessage usersToFollow = new JabberMessage("users", whoToFollow);

                    try {
                        oos.writeObject(usersToFollow);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //below system out println for testing purposes

                    //System.out.println("Who to follow data for user " + clientUserId + " sent to client");

                    break;
                case("post"):

                    StringBuilder sb = new StringBuilder();

                    for (int i = 1; i < splitMessageArray.length; i++) {
                        sb.append(splitMessageArray[i]);
                        if (i != splitMessageArray.length-1){
                            sb.append(" ");
                        }
                    }

                    String jabToSend = sb.toString();

                    db.addJab(clientUsername, jabToSend);

                    JabberMessage jabAdded = new JabberMessage("posted");

                    //below system out println for testing purposes

                    System.out.println("Client with username: " + clientUsername + " posted a jab with content: " + jabToSend);

                    try {
                        oos.writeObject(jabAdded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case("like"):

                    db.addLike(clientUserId, (Integer.parseInt(splitMessageArray[1])));

                    JabberMessage jabLiked = new JabberMessage("posted");

                    //below system out println for testing purposes

                    System.out.println("Client with userid: " + clientUserId + " liked jab with jabid: " + (Integer.parseInt(splitMessageArray[1])));

                    try {
                        oos.writeObject(jabLiked);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    commandWord = null;

                    break;
                case("follow"):

                    db.addFollower(clientUserId, splitMessageArray[1]);

                    JabberMessage followedUser = new JabberMessage("posted");

                    try {
                        oos.writeObject(followedUser);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        oos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //below system out println for testing purposes

                    System.out.println("Client with username: " + clientUsername + " followed jabber used with userid: " + splitMessageArray[1]);

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + commandWord);
            }

        }

        //below system out println for testing purposes

        System.out.println("Client successfully signed out, end of thread");

    }

}
