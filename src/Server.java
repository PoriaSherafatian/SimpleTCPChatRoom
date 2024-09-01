import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutDown();
        }

    }

    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutDown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
                for (ConnectionHandler ch : connections) {
                    ch.shutdown();
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickName;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {

                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter your nickName");
                nickName = in.readLine();
                System.out.println(nickName + " is connected! ");
                broadcast(nickName + " joined the chat! ");

                //That FUCKING loop for waiting to get massage from client...

                String message;

                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickName + " rename themSelf to " + messageSplit[1]);
                            System.out.println(nickName + " rename themSelf to " + messageSplit[1]);
                            nickName = messageSplit[1];
                            out.println("Successfully renamed " + nickName + " to " + messageSplit[1]);
                        } else {
                            out.println("no nickName provided!");
                        }
                    } else if (message.startsWith("/quit")) {
                        broadcast(nickName + " left the chat!");
                        shutdown();
                    } else {
                        broadcast(nickName + " : " + message);
                    }
                }

            } catch (IOException e) {
                shutdown();
            }

        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}


// All code written by Poria Sherafatian to demonstrate my skills in Java back-end development !!!