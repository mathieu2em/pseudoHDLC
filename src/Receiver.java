import java.net.*;
import java.io.*;
import java.util.Arrays;

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

        //String greeting = in.readLine();
        // Receiving data from client
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[1024];

        
        baos.write(buffer, 0 , in.read(buffer));

        byte result[] = baos.toByteArray();

        String res = Arrays.toString(result);
        System.out.println("Recieved from client : "+res);

        //echoing back to client
        out.write(result);

        System.out.println("Closing connection");

        // close connection
        clientSocket.close();
        in.close();

        /*
        if ("hello server".equals(greeting))
        {
            out.println("hello client");
        }
        else {
            out.println("unrecognised greeting");
        }

         */
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public static void Main(String[] args) throws IOException {
        //TODO again , trying stuff
        Receiver server=new Receiver();
        server.start(6666);
    }
}