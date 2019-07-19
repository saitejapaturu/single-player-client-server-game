import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Gets client's socket from server class.
 * Runs the game.
 */
public class ServerThread implements Runnable
{

    private Socket clientSocket;    // Client socket
    private String clientName;      // Client's username
    private int guessCounter = 0;        // Counts the numbers guessed

    private final int ANSWER;       // The answer to win the game, generated using Random

    private Random random = new Random(); //  To generate a random number for the ANSWER

    private boolean clientWon = false;  //To check the state if client won.

    //Final variables
    private final int MAX_GUESSES = 4;      // Maximum number of guesses
    private final int MIN_GUESS_RANGE = 0;  // The lowest integer allowed to guess
    private final int MAX_GUESS_RANGE = 9;  // The highest integer allowed to guess

    //Stable Messages to Client

    // Message to register Client
    private final String REGISTER_MESSAGE = "Register your username: (Maximum 25 characters)";
    // Message to start Game.
    private final String GAME_START_MESSAGE = "Random number has been generated. " +
                                                "\nGame has begun. \nTry to guess the answer in 4 tries";
    // Message for Invalid Guesses.
    private final String INVALID_GUESS_MESSAGE = "Invalid Number! Please enter an integer between 0 - 9";
    // Message if the Client Won the game.
    private final String WIN_MESSAGE = "Congratulations! You guessed the right number.";
    // Message if the Client Lost.
    private final String LOSS_MESSAGE = "Your Guesses have ended! Betterluck next time.";
    // Message if the Guess was lower than the answer
    private final String GUESS_LOWER_THAN_ANSWER_MESSAGE = "The number Guessed is smaller than the Answer.";
    // Message if the guess was higher than the answer
    private final String GUESS_HIGHER_THAN_ANSWER_MESSAGE = "The number Guessed is bigger than the Answer.";

    // Blank line between turns
    private final String BLANK_LINE = "\n";

    //Game states
    //There are 3 game states,
    // R - Register where client user name
    // G - Guess where client guesses the number
    // GO - Game Over, either if client used 4 tries or Won the game.
    private final String STATE[] = {"R","G", "GO"};


    //Gets client's socket from server class and generates random answer.
    public ServerThread(Socket ClientSocket)
    {
        this.clientSocket = ClientSocket;
        this.ANSWER = random.nextInt(MAX_GUESS_RANGE - MIN_GUESS_RANGE + 1) + MIN_GUESS_RANGE;
    }

    //Runs the server thread concurrently
    @Override
    public void run()
    {
        System.out.println("ServerThread running");
        System.out.println(Thread.activeCount() + " Threads are running on the server.");   //testing number of threads

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {

            inputStream = clientSocket.getInputStream();    //Gets input Stream from Client to read from client.

            outputStream = clientSocket.getOutputStream();  //Gets outputStream from Client to write to client.

            //DEBUG
            System.out.println("Accepted Client");

            System.out.println("Randomly Generated number is: " + ANSWER);  //Server to check answer

            String clientInput, outputMessage;


            //Step 1, register the client.
            // Sends message to client to register and get's username from client using STATE "R" and assigns it.
            sendOutput(outputStream, inputStream, REGISTER_MESSAGE);
            this.clientName = getInput(outputStream, inputStream, STATE[0]);

            //Step 2, start the game
            //Starts the game.
            sendOutput(outputStream, inputStream, ("Registration successful. Username: " + this.clientName
                                                    + "\n" + GAME_START_MESSAGE));

            // Allows the client to guess until the tries are over or Client won.
            while (guessCounter < MAX_GUESSES && !clientWon)
            {
                //Sends message to tell the client to proceed guessing and indicates the no of guesses left.
                // Message to tell the Client to proceed guessing.
                String prcoeedToGuessMessage = clientName + ": You have " + (MAX_GUESSES - guessCounter)
                        + " guesses left." +("\n" + clientName
                        + ": Proceed with Guess no " + (guessCounter+1));

                sendOutput(outputStream, inputStream, (BLANK_LINE+prcoeedToGuessMessage));

                clientInput = getInput(outputStream, inputStream, STATE[1]);

                //DEBUG info to server
                System.out.println("Guess received from user:" + this.clientName + " is :" + clientInput);

                // Cheks the validity
                outputMessage = guessValidity(clientInput);

                //If it is a valid guess but not answer, then increments the guess counter.
                if (outputMessage.equals(GUESS_LOWER_THAN_ANSWER_MESSAGE) ||
                        outputMessage.equals(GUESS_HIGHER_THAN_ANSWER_MESSAGE))
                {
                    guessCounter++;
                }

                //If the guess was correct then ends the loop.
                else if(outputMessage.equals(WIN_MESSAGE))
                {
                    clientWon = true;
                }

                // Sends appropriate message to Client.
                sendOutput(outputStream, inputStream, outputMessage);
            }

            //If the guesses were over but Client Lost
            if (!clientWon)
            {
                sendOutput(outputStream, inputStream, LOSS_MESSAGE + ("\nCorrect Answer was: " + this.ANSWER));
            }

            //Final Stage, Game Over - GO
            // Sends message to client that the game finished.
            outputStream.write(STATE[2].getBytes());    //Game over.

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //Closing streams and connections
        finally
        {
            try
            {
                //Closing output-stream
                outputStream.close();

                //Closing input-stream
                inputStream.close();

                //Close connection
                clientSocket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /*
    *   Takes guess provided by the client.
    *   If the guess is not a integer or out of range returns Invalid message.
    *   If the guess is in range but less than or grater than answer then returns appropriate message.
    *   If the guess is correct, returns Win message.
    */
    private String guessValidity(String guess)
    {
        int guessNumber;

        // Covert Guess to string, if not a string return invalid message.
        try
        {
            guessNumber = Integer.parseInt(guess);
        }
        catch (NumberFormatException e)
        {
            return INVALID_GUESS_MESSAGE;
        }

        // Check if out of range.
        if (guessNumber < MIN_GUESS_RANGE || guessNumber > MAX_GUESS_RANGE)
        {
            return INVALID_GUESS_MESSAGE;
        }

        // Check if answer
        else if (guessNumber == ANSWER)
        {
            return WIN_MESSAGE;
        }

        // Check if less than answer
        else if (guessNumber < ANSWER)
        {
            return GUESS_LOWER_THAN_ANSWER_MESSAGE;
        }

        // Check if greater than answer
        else
        {
            return GUESS_HIGHER_THAN_ANSWER_MESSAGE;
        }
    }

    // Sends output message to client and waits for the continue message.
    private void sendOutput(OutputStream outputStream, InputStream inputStream, String output)
    {
        byte [] buffer = new byte[1024];
        try
        {
            outputStream.write(output.getBytes());
            inputStream.read(buffer);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    // Sends State command to Client, indicating current state. Get's input from client and returns input.
    private String getInput(OutputStream outputStream, InputStream inputStream, String state)
    {
        byte [] buffer = new byte[1024];
        String input_from_client = null;

        try
        {
            outputStream.write(state.getBytes());
            inputStream.read(buffer);

            //Convert to string
            input_from_client = new String(buffer).replace("\0","");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return input_from_client;
    }
}

