package chess.gui.Multiplayer;

import chess.gui.Table;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

// This class represents a player (client)
public class MultiServerThread extends Thread {


    private final Socket clientSocket;
    private MultiServerThread opponent;
    private final Server server;
    private final PrintWriter out;
    private final int id;
    private Table table;
    private WaitingRoom waitingRoom;

    // Constructor
    public MultiServerThread(Server Server, Socket clientSocket) throws IOException {
        super("MailMultiServerThread");
        this.clientSocket = clientSocket;
        this.server = Server;

        table = null;
        opponent = null;
        waitingRoom = null;

        out = new PrintWriter(clientSocket.getOutputStream(), true);

        id = server.getId();
        server.setId(server.getId() + 1);

        server.getPlayers().add(this);
        server.showPlayersConnected.setText("Users connected : " + server.getPlayers().size());

    }

    //Getters
    public Socket getClientSocket() {
        return clientSocket;
    }

    public MultiServerThread getOpponent() {
        return opponent;
    }

    public PrintWriter getOut() {
        return out;
    }

    public int getPlayersId() {
        return id;
    }

    public Table getTable() {
        return table;
    }

    public WaitingRoom getWaitingRoom() {
        return waitingRoom;
    }

    //Setters
    public void setOpponent(MultiServerThread opponent) {
        this.opponent = opponent;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void setWaitingRoom(WaitingRoom waitingRoom) {
        this.waitingRoom = waitingRoom;
    }

    // Run method starts the procedure for the client to play
    public void run() {

        try {
            findOpponent();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Finding an opponent
    public void findOpponent() throws IOException {
        MultiServerThread temp;
        if (server.getPlayersWaiting().size() > 0) {
            server.getPlayersWaiting().get(0).waitingRoom.dispose(); // Closing players "waiting" frame
            temp = server.getPlayersWaiting().get(0);
            server.getPlayersWaiting().remove(0); // Removing Player from waiting list
            play(this, temp);

        } else {
            server.getPlayersWaiting().add(this);
            waitingRoom = new WaitingRoom(this);// Adding player to waiting list
        }

    }

    // Start game between player 1 and player 2
    public void play(MultiServerThread player1, MultiServerThread player2) {

        setUpFrames(player1, player2); // setting up frames
        player2.table.setMouseEnabled(false); // enabling player 1 to play

    }

    // Setting up the frames of the two players
    public void setUpFrames(MultiServerThread player1, MultiServerThread player2) {

        // Each player gets a new Game
        player1.table = new Table();
        player2.table = new Table();

        player1.waitingRoom = null;
        player2.waitingRoom = null;

        // Setting both players status to  true -> 'online'
        player1.table.setStatus(true);
        player2.table.setStatus(true);

        // Removing extra JMenu
        player1.table.gameMenu.setVisible(false);
        player2.table.gameMenu.setVisible(false);

        player1.table.preferenceMenu.setVisible(false);
        player2.table.preferenceMenu.setVisible(false);

        player1.table.optionMenu.setVisible(false);
        player2.table.optionMenu.setVisible(false);

        // Flipping second players Gameboard
        flipBoard(player2.table);

        player1.setOpponent(player2);
        player2.setOpponent(player1);

        // Setting variables
        player1.table.setMultiServerThread(player1);
        player2.table.setMultiServerThread(player2);

        // Starting the game
        player1.table.start();
        player2.table.start();

    }

    // Flipping players board
    private void flipBoard(Table player) {
        player.setBoardDirection(Table.BoardDirection.FLIPPED);
        player.getBoardPanel().drawBoard(player.getGameBoard());
    }

    // Exiting player
    public void exit(boolean check) throws IOException {

        MultiServerThread temp;
        int i = 0;

        // Running through the player list
        while (i < server.getPlayers().size()) {

            // Finding the player that wants to exit
            if (server.getPlayers().get(i).getPlayersId() == id) {

                // If the player is active (is currently playing)
                if (server.getPlayers().get(i).waitingRoom == null) {

                    // Preparing to close player
                    server.getPlayers().get(i).table.setStatus(false);
                    server.getPlayers().get(i).table.getGameFrame().dispose();
                    server.getPlayers().get(i).table.getGameTimerPanel().setTerminateTimer(true);
                    server.getPlayers().get(i).getOut().println("exit");
                    server.getPlayers().get(i).clientSocket.close();

                    // If the players opponent is active, exit opponent
                    if (server.getPlayers().get(i).getOpponent().getTable().getStatus() && check) {
                        temp = server.getPlayers().get(i).getOpponent();
                        JOptionPane.showMessageDialog(null,"Your opponent has left the game.\n       You Won !!!");
                        server.getPlayers().remove(i);
                        temp.exit(true);
                    } else {
                        // else, remove only this player
                        server.getPlayers().remove(i);
                    }


                } else {
                    // If the player decides to leave, while being in a waiting room
                    server.getPlayers().get(i).getOut().println("exit");
                    server.getPlayers().get(i).clientSocket.close();
                    server.getPlayers().remove(i);
                    server.getPlayersWaiting().remove(0);
                }
                // updating servers text
                server.showPlayersConnected.setText("Users connected : " + server.getPlayers().size());

            }

            i++;

        }

    }
}