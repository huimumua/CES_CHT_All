package com.askey.firefly.zwave.control.net;

import android.util.Log;

import com.askey.firefly.zwave.control.utils.Const;

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

    public static final int MAX_CLIENT = 10;
    private boolean running = false;
    private final String LOG_TAG = "TCPServer";
    private ArrayList<PrintWriter> mOuts = new ArrayList<>();
    private ArrayList<Integer> mClients = new ArrayList<>();
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
        int index = mClients.indexOf(clientID);
        Log.i(LOG_TAG,"TCP sendMessage : ["+clientID+"]:"+message+" #index="+index);
        if (index>=0) {
            mOuts.get(index).println(message);
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
            Log.i(LOG_TAG,"START TCP server");
            m_serverSocket = new ServerSocket(Const.TCP_PORT);
            while (true) {
                socket = m_serverSocket.accept();
                ChatHandler handler = new ChatHandler(socket);
                handler.start();
            }
        } catch (IOException ex) {
            Log.e(LOG_TAG,"Failed to start TCP server");
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
        ArrayList handlers = new ArrayList();

        private Socket socket;
        private BufferedReader read;

        public ChatHandler(Socket socket) {
            try {
                this.socket = socket;
                this.read = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                Log.i(LOG_TAG,"Client count = "+ mOuts.size());
                // Check if there are too many clients.
                if (mOuts.size() >= MAX_CLIENT) {
                    sendMessage(socket.getPort(),"TCP client Limit Exceed!!!!!!");
                } else {
                    mOuts.add(new PrintWriter(
                            new BufferedWriter(new OutputStreamWriter(
                                    socket.getOutputStream())), true));
                    mClients.add(socket.getPort());
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
                        try {
                            clientString = read.readLine();

                            if (clientString != null && messageListener != null) {
                                for (int index = 0; index < handlers.size(); index++) {

                                    ChatHandler handler = (ChatHandler) handlers.get(index);
                                    clientId = handler.socket.getPort();

                                    messageListener.messageReceived(clientId, clientString);
                                }
                            }
                            else if (clientString == null){
                                Log.e(LOG_TAG,"SOCKET#"+socket.getPort()+" DISCONNECT!!!");
                                break;
                            }
                        }
                        catch  (IOException ie) {
                            Log.e(LOG_TAG,"SOCKET#"+socket.getPort()+" read error!!!");
                            break;
                        }
                    }
                }
            } finally {
                try {
                    int index = mClients.indexOf(socket.getPort());
                    mOuts.remove(index);
                    mClients.remove(index);
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
