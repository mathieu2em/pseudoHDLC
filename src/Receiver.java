import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

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
    private DataOutputStream out;
    private DataInputStream in;

    // TODO this is a first try in establishing a connection point WIP from
    // https://www.baeldung.com/a-guide-to-java-sockets
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        //out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new DataInputStream(clientSocket.getInputStream());
        //in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new DataOutputStream(clientSocket.getOutputStream());

        // Receiving data from client
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte buffer[] = new byte[1024];
        baos.write(buffer, 0 , in.read(buffer));

        byte result[] = baos.toByteArray();
        Frames resultFrame = new Frames(result); // TODO test

        String res = Arrays.toString(result);
        System.out.println("Recieved from client : "+res);

        //echoing back to client
        out.write(result);

        System.out.println("Press 1 to close connection if you want to");
        String choice = "";
        while (!choice.equals("1")){
            choice = new Scanner(System.in).nextLine();
        }
        System.out.println("Closing connection");

        // close connection
        clientSocket.close();
        in.close();
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    // returns a frame of type R => REJ and the num of the tram rejected
    private Frames genREJ(int num){
        Frames frames = new Frames('R', num);
        return frames;
    }

    // returns a frame of type A => RR that confirms that Receiver received the tram number "num"
    private Frames genRR(int num){
        Frames frames = new Frames('A', num);
        return frames;
    }

    public static void Main(String[] args) throws IOException {
        //TODO again , trying stuff
        Receiver server=new Receiver();
        server.start(6666);
    }
}