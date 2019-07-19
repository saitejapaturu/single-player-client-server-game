import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Server class creates another thread using ServerThread to handle game
 */
public class Server
{
    /**
     * Runs server.
     * Connects to port 61616 of the server, which Client class is also using.
     * Connects to it.
     * Sends the socket to ServerThread class for gameplay.
     */

    public static void main(String [] args)
    {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try
        {

            serverSocket = new ServerSocket(61616);       //Create a serversocket which binds to the server port

            // DEBUG to server screen
            System.out.println("Serversocket inet address: " + serverSocket.getInetAddress());


            // When client requests to connect, acccepts the connection and the socket returned will be stored.
            clientSocket = serverSocket.accept();               // Create a connection between server and client

            // Create a thread, which sends clientSocket to ServerThread class for writing
            Thread thread = new Thread(new ServerThread(clientSocket));
            thread.run();   // Run the thread

            try
            {
                serverSocket.close();   //Closes server socket after writing
            }

            catch(SocketException e)
            {
                e.printStackTrace();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
