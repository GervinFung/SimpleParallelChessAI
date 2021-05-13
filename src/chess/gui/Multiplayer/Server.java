package chess.gui.Multiplayer;


import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

// This class represents the server
public class Server extends JFrame {

    private final JLabel serverStatus;

    private ServerSocket serverSocket;
    private int id;
    private boolean listening;
    private ArrayList<MultiServerThread> playersWaiting;
    private ArrayList<MultiServerThread> players;

    public JLabel showPlayersConnected;

    // Constructor
    public Server(int port) {

        serverSocket = null;
        listening = true;
        id = 0;

        // Setting up the JFrame
        setSize(500, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setTitle("Server");
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        // Setting up the JLabel texts
        serverStatus = new JLabel();
        serverStatus.setFont(serverStatus.getFont().deriveFont(20.0f));
        serverStatus.setAlignmentX(CENTER_ALIGNMENT);

        showPlayersConnected = new JLabel();
        showPlayersConnected.setFont(showPlayersConnected.getFont().deriveFont(20.0f));
        showPlayersConnected.setAlignmentX(CENTER_ALIGNMENT);

        // Adding elements to the frame
        add(Box.createRigidArea(new Dimension(0, 25)));
        add(serverStatus);
        add(Box.createRigidArea(new Dimension(0, 60)));
        add(showPlayersConnected);

        // Listener used when the player decides to exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                // Confirmation text to exit
                final int option = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to close Server ?",
                        "Close Window?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                // If the answer is yes
                if (option == JOptionPane.YES_OPTION) {
                    try {
                        // Close server
                        closeServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        setVisible(true);
        // Start Server
        startServer(port);
    }

    // Getters
    public int getId() {
        return id;
    }

    public ArrayList<MultiServerThread> getPlayers() {
        return players;
    }

    public ArrayList<MultiServerThread> getPlayersWaiting() {
        return playersWaiting;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setPlayers(ArrayList<MultiServerThread> players) {
        this.players = players;
    }

    public void setPlayersWaiting(ArrayList<MultiServerThread> playersWaiting) {
        this.playersWaiting = playersWaiting;
    }

    // Function that starts the server
    private void startServer(int port) {
        // Try to start Server
        try {
            // Setting up variables
            serverSocket = new ServerSocket(port);
            players = new ArrayList<>();
            playersWaiting = new ArrayList<>();

            serverStatus.setForeground(Color.GREEN);
            serverStatus.setText("Server is online..");
            showPlayersConnected.setText("Users connected : " + players.size());

            // start running
            run();

        } catch (IOException e) {
            // If there has been an error
            serverStatus.setForeground(Color.RED);
            serverStatus.setText("Could not start server, please try again");
            showPlayersConnected.setText("");

        }
    }

    // Server waiting to accept client requests
    public void run() {

        MultiServerThread MultiServerThread1;
        while (listening) {
            try {
                // Try accepting clients
                MultiServerThread1 = new MultiServerThread(this, serverSocket.accept());
                MultiServerThread1.start();
            } catch (IOException e) {
                listening = false;
            }

        }
    }

    // Closing the Server
    public void closeServer() throws IOException {
        int i = 0;

        // Exiting all players
        while (i < players.size()) {
            players.get(i).exit(false);
            i++;
        }

        serverSocket.close();
        System.exit(0);
    }

    public static void main(String[] args) {

        // Calling Server
        try {
            new Server(8080);
        } catch (Exception exception) {
            System.err.println("Error occurred ... Closing program");
            System.exit(-1);
        }


    }
}
