import java.net.*;
import java.io.*;

/* the receiver has to be able to:
   1 - receive trams
   2 - verify presence of errors
   3 - produce and send receipts
   4 - send REJ in case of errors
 */
public class Receiver {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    // TODO this is a first try in establishing a connection point WIP from
    // https://www.baeldung.com/a-guide-to-java-sockets
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String greeting = in.readLine();
        if ("hello server".equals(greeting)) {
            out.println("hello client");
        }
        else {
            out.println("unrecognised greeting");
        }
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