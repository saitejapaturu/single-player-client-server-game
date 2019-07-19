import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Gets socket from client class.
 * Runs the game from server.
 */
public class ClientThread extends Thread

{
    private Socket socket;

    // A list of game states, used to check if the state of the game
    // There are 3 game states,
    // R - Register where client user name
    // G - Guess where client guesses the number
    // GO - Game Over, either if client used 4 tries or Won the game.
    private final List<String> STATES = new ArrayList<String>(Arrays.asList("R","G","GO"));
    // To send confirmation to continue to server
    private final String CONTINUE_MESSAGE = "c";

    public ClientThread(Socket socket)
    {
        //setting thread as user interface thread
        this.setDaemon(true);
        this.socket = socket;
    }

    //Runs the client thread concurrently
    public void run()
    {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        Scanner scanner = null;

        try
        {
            inputStream = socket.getInputStream();      //Gets inputStream from Server to read from server.

            outputStream = socket.getOutputStream();    //Gets outputStream from Server to write to server.

            scanner = new Scanner(System.in);           // Scanner for user input

            String serverInput, clientOutput;           // Strings to store serverInput and Client output.

            byte [] buffer = new byte[1024];


            while(true)
            {
                // Get input from server
                inputStream.read(buffer);
                serverInput = new String(buffer).replace("\0","");
                buffer = new byte[1024];

                // If the input is one of the STATE Commands
                // Loops according to the state.
                if (STATES.contains(serverInput))
                {
                    // Check if the game is over
                    if (serverInput.equals("GO"))
                    {
                        break;
                    }

                    // Gets input from user either to register or to guess the number.
                    do {
                        clientOutput = scanner.nextLine();
                        int maxLength;
                        // Checks if the input is for either username or guessing number
                        // 25 is maximum as username can be a maximum 25 characters.
                        if(serverInput.equals("R"))
                        {
                            maxLength = 25;
                        }

                        // 1 is maximum as guess number can be a single digit integer.
                        else
                        {
                            maxLength = 1;
                        }

                        // Check if the input is in range.
                        if(clientOutput.length() < 1)
                        {
                            System.out.println("Message to server is too short. Try again.");
                        }
                        else if(clientOutput.length() > maxLength)
                        {
                            // If checking for Guess Number.
                            if(maxLength == 1)
                            {
                                System.out.println("Guess Number to send to server is too long.");
                            }
                            else
                            {
                                System.out.println("Username to send to server is too long.");
                            }
                        }
                        // If the input is within range, continue.
                        else
                        {
                            break;
                        }
                    }
                    while (true);
                }

                // If the input is one NOT of the STATE Commands
                // Prints the input to user and sends confirmation to continue to SERVER.
                else
                {
                    System.out.println(serverInput);
                    clientOutput = CONTINUE_MESSAGE;
                }

                // Writes either the username, guess number or continue confirmation to server.
                outputStream.write(clientOutput.getBytes());

            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        //Closing streams and connections
        finally
        {
            try
            {
                //Closing input-stream
                scanner.close();
                inputStream.close();

                //Closing output-stream
                outputStream.close();

                //Close connection
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
