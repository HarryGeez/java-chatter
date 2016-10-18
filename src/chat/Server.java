package chat;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by weijiangan on 03/10/2016.
 */
public class Server {
    private static final int CLIENT_QUEUE_CAPACITY = 10;
    private static final Vector<User> users = new Vector<>();
    private static final Vector<User> availToChat = new Vector<>();
    private static final Vector<String[]> agents = new Vector<>();
    private static ArrayBlockingQueue<User> clientQueue;

    public static void main(String[] args) {
        ServerSocket listener;

        clientQueue = new ArrayBlockingQueue<>(CLIENT_QUEUE_CAPACITY);
        try {
            listener = new ServerSocket(8080);
            System.out.println("Server is up and running...");
            while (true) {
                new Handler(listener.accept()).start();
            }
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e);
        }
    }

    private static class Handler extends Thread {
        Socket client;
        User loginUser;
        User clientUser;
        User.Type userType;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        ObjectOutputStream toClient = null;
        Authenticator authenticator;
        Message received;

        Handler(Socket client) {
            this.client = client;
        }

        public void run() {
            try {
                System.out.println("Connection from " + client.getInetAddress().toString() + " accepted.");
                authenticator = new Authenticator();
                in = new ObjectInputStream(client.getInputStream());

                while (true) {
                    loginUser = (User) in.readObject();
                    int signal = authenticator.authenticate(loginUser);
                    out = new ObjectOutputStream(client.getOutputStream());

                    if (signal > 0) {
                        for (User temp : users) {
                            if (temp.getId().equalsIgnoreCase(loginUser.getId()) && signal != 2) {
                                System.out.println(loginUser.getId() + " is already online!");
                                out.writeObject(new Message("server", Integer.toString(-2)));
                                client.close();
                                return;
                            }
                        }
                        out.writeObject(new Message("server", Integer.toString(signal)));
                        userType = (signal == 1) ? User.Type.CLIENT : User.Type.AGENT;
                        loginUser.setOut(out);
                        loginUser.setSocket(client);
                        users.add(loginUser);
                        availToChat.add(loginUser);
                        break;
                    } else {
                        out.writeObject(new Message("server", Integer.toString(signal)));
                    }
                }

                if (userType == User.Type.CLIENT) {
                    System.out.println(loginUser.getId() + client.getInetAddress() + " is a client");
                    clientQueue.put(loginUser);
                    System.out.println(loginUser.getId() + client.getInetAddress() + " is put into queue");

                    received = (Message) in.readObject();
                    System.out.println(loginUser.getId() + client.getInetAddress() + "Received from " + received.getMsg());
                    clientUser = findUser(received.getMsg());
                    if (clientUser == null) {
                        out.writeObject(new Message("server", "Agent unavailable or went offline.", LocalDateTime.now()));
                        client.close();
                        return;
                    }
                    toClient = clientUser.getOut();
                } else {
                    System.out.println(loginUser.getId() + client.getInetAddress() + " is an agent");
                    if (agents.size() == 0) {
                        agents.add(new String[]{loginUser.getId(), null, null});
                    } else {
                        for (int i = 0; i < agents.size(); i++) {
                            if (agents.elementAt(i)[0].equalsIgnoreCase(loginUser.getId())) {
                                if (agents.elementAt(i)[1] != null && agents.elementAt(i)[2] != null) {
                                    out.writeObject(new Message("server", "You are already serving 2 clients. You will be disconnected.", LocalDateTime.now()));
                                    client.close();
                                    return;
                                }
                            }
                            if (i == (agents.size() - 1) && !agents.elementAt(i)[0].equalsIgnoreCase(loginUser.getId())) {
                                agents.add(new String[]{loginUser.getId(), null, null});
                            }
                        }
                    }
                    System.out.println("Agents size is " + agents.size());
                    while (true) {
                        try {
                            System.out.println("Trying to get a client...");
                            clientUser = clientQueue.take();
                            for (int i = 0; i < agents.size(); i++) {
                                if (agents.elementAt(i)[0].equalsIgnoreCase(loginUser.getId())) {
                                    if (agents.elementAt(i)[1] == null) {
                                        agents.elementAt(i)[1] = clientUser.getId();
                                    } else {
                                        agents.elementAt(i)[2] = clientUser.getId();
                                    }
                                }
                            }
                            toClient = clientUser.getOut();
                            System.out.println("Found one!");
                            out.writeObject(new Message("server", "Connected to " + clientUser.getId() + ", you can start chatting now.", LocalDateTime.now()));
                            break;
                        } catch (InterruptedException ie) {
                            System.out.println("Interrupted exception occurred: " + ie);
                        }
                    }
                }

                while (true) {
                    received = (Message) in.readObject();
                    received.setLdt(LocalDateTime.now());
                    if (received.isBroadcast()) {
                        for (int i = 0; i < agents.size(); i++) {
                            if (agents.elementAt(i)[0].equalsIgnoreCase(loginUser.getId())) {
                                findOut(agents.elementAt(i)[1]).writeObject(received);
                                findOut(agents.elementAt(i)[2]).writeObject(received);
                            }
                        }
                    } else {
                        toClient.writeObject(received);
                    }
                }


            } catch (Exception e) {
                System.out.println("Exception occurred: " + e);
                if (userType == User.Type.CLIENT) {
                    if (clientQueue.contains(loginUser)) {
                        clientQueue.remove(loginUser);
                    }
                }
                users.removeElement(clientUser);
            }
        }
    }

    private static User findUser(String username) {
        User usr = null;

        for (int i = 0; i < availToChat.size(); i++) {
            if (availToChat.elementAt(i).getId().equalsIgnoreCase(username)) {
                System.out.println("found user!");
                usr = availToChat.remove(i);
                break;
            }
        }
        return usr;
    }

    private static ObjectOutputStream findOut(String username) {
        ObjectOutputStream out = null;

        for (int i = 0; i < users.size(); i++) {
            if (users.elementAt(i).getId().equalsIgnoreCase(username)) {
                System.out.println("found oos!");
                out = users.get(i).getOut();
                break;
            }
        }
        return out;
    }
}