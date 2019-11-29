import java.net.*;
import java.io.*;

/**
 * the receiver has to be able to:
 *    1 - receive trams
 *    2 - verify presence of errors
 *    3 - produce and send receipts
 *    4 - send REJ in case of errors
 */
public class Receiver {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int renduOu = 0;

    Receiver(){}

    void start() throws IOException {
        serverSocket = new ServerSocket(6666);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    void listen() throws IOException {
        String inputLine;
        Frames frame;
        while ((inputLine = in.readLine()) != null) {
            frame = new Frames(inputLine);
            System.out.println(frame.getData());
            System.out.println("received from client" + inputLine + " " + frame.getData());
            processFrame(frame);
            //out.println(inputLine);
        }
    }

    public void processFrame(Frames frames) throws IOException {
        char type = frames.getType();
        if (type == 'C'){
            System.out.println("received a connection tram asking for Go Back End, will now send answer");
            out.println(genRR(0).formatFrameToSend());
        } else if (type == 'I'){
            // TODO verifyDataFrame(frames); verifie le num, verifie le crc, store result dans un String global
        } else if (type == 'P'){
            out.println(genRR(renduOu).formatFrameToSend());
        } else if (type == 'F'){
            stop();
        }
    }

    private void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    // returns a frame of type R => REJ and the num of the tram rejected
    private Frames genREJ(int num){
        return new Frames('R', num);
    }

    // returns a frame of type A => RR that confirms that Receiver received the tram number "num"
    private Frames genRR(int num){
        return new Frames('A', num);
    }
}