import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

/* the transmitter has to :
   1 - read file's data
   2 - product and send trams
   3 - handle receipts
   4 - re-send the data in case of error
*/

public class Transmitter {

    private Socket clientSocket;
    //private DataOutputStream out;
    //private DataInputStream in;
    private PrintWriter out;
    private BufferedReader in;

    Transmitter() {
    }

    ArrayList<Frames> readFile(String filePath) throws FileNotFoundException {

        ArrayList<Frames> frameList = new ArrayList<>();
        Scanner scanner = new Scanner(new File(filePath));

        Character frameType = 'I';
        while(scanner.hasNextLine()){
            Frames frame = new Frames(frameType, scanner.nextLine());
            frameList.add(frame);
        }

        return frameList;
    }

    // reads a file and send it
    // TODO implement the HDLC PROTOCOL
    void sendFile(String filePath) throws IOException {
        ArrayList<Frames> fileFrames = readFile(filePath);

        for (Frames fileFrame : fileFrames) {
            sendFrame(fileFrame);
        }
    }

    void startConnection() throws IOException {
        clientSocket = new Socket("127.0.0.1", 6666);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    Frames sendFrame(Frames frames) throws IOException {
        String stringFrame = frames.formatFrameToSend();

        // send the frame as a string
        out.println(stringFrame);

        //printing request to console
        System.out.println("Sent to server : " + stringFrame);
        System.out.println("containing : " + frames.getData());

        String result = in.readLine();

        // printing reply to console
        System.out.println("Recieved from server : " + result);
        Frames frames1 = new Frames(result);
        System.out.println("of Type : " + frames1.getType());

        return frames1;
    }


    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }


}
