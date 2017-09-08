package com.askey.firefly.zwave.control.mqtt;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by chiapin on 2017/7/31.
 */

public class TCPServer extends Thread {

    public static final int SERVERPORT = 48080;
    public static final int MAX_CLIENT = 10;
    private boolean running = false;
    private final String LOG_TAG = "TCPServer";
    private PrintWriter[] mOuts = new PrintWriter[MAX_CLIENT];
    private int[] mClients = new int[MAX_CLIENT];
    private static int avialableClient = 0;
    private OnMessageReceived messageListener;
    public static ServerSocket m_serverSocket = null;

    int clientId = 0;

    /**
     * Constructor of the class
     *
     * @param messageListener
     *            listens for the messages
     */
    public TCPServer(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }

    public void sendMessage(int clientID, String message) {
        for (int index = 0; index < avialableClient; index++) {
            if (mClients[index] == clientID) {
                mOuts[index].println(message);
            }
        }
    }

    /**
     * Method to send the messages from server to client
     */

    @Override
    public void run() {
        super.run();

         running = true;

        try {
            ServerSocket serverSocket = null;
            Socket socket = null;
            Log.i(LOG_TAG,"Connecting...");
            m_serverSocket = new ServerSocket(SERVERPORT);
            while (true) {
                socket = m_serverSocket.accept();
                ChatHandler handler = new ChatHandler(socket);
                handler.start();
            }
        } catch (IOException ex) {
            Log.i(LOG_TAG,"Error");
        }
    }

    public static void close(){
        try {
            m_serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnMessageReceived {
        void messageReceived(int clientID,String message);
    }

    // add for multiclient
    class ChatHandler extends Thread {
        ArrayList handlers = new ArrayList(MAX_CLIENT);

        private Socket socket;
        private BufferedReader read;

        public ChatHandler(Socket socket) {
            try {
                this.socket = socket;
                this.read = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                // Check if there are too many clients.
                if (avialableClient >= MAX_CLIENT) {
                    for (int index = 0; index < avialableClient; index++) {
                        sendMessage(mClients[index],"Client Limit Exceed!!!!!!");
                    }

                } else {
                    mOuts[avialableClient] = new PrintWriter(
                            new BufferedWriter(new OutputStreamWriter(
                                    socket.getOutputStream())), true);
                    mClients[avialableClient] =  socket.getPort();
                    avialableClient++;
                }

            } catch (IOException ex) {
                Logger.getLogger(ChatHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void run() {
            String clientString = null;
            synchronized (handlers) {
                handlers.add(this);
            }
            try {
                synchronized (handlers) {

                    while (running) {
                        clientString = read.readLine();
                        if (clientString != null && messageListener != null) {
                            for (int index = 0; index < handlers.size(); index++) {

                                ChatHandler handler = (ChatHandler) handlers.get(index);
                                clientId =handler.socket.getPort();
                                messageListener.messageReceived(clientId,clientString);

                            }
                        }
                    }
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    read.close();
                    socket.close();
                } catch (IOException ioe) {
                } finally {
                    synchronized (handlers) {
                        handlers.remove(this);
                    }
                }
            }
        }
    }
}
