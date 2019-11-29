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

        Character frameType = 'I'; // TODO pas sure que c'est I , a reverifier ...
        while(scanner.hasNextLine()){
            Frames frame = new Frames(frameType, scanner.nextLine());
            frameList.add(frame);
        }

        return frameList;
    }

    void startConnection() throws IOException {
        clientSocket = new Socket("127.0.0.1", 6666);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        /*
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        */
        // sends output to the socket
        //out = new DataOutputStream(clientSocket.getOutputStream());
        //takes input from socket
        //in = new DataInputStream(clientSocket.getInputStream());

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    /*
    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }
    */

    /* TODO s'assurer de la procedure pour l'instant on tente juste l'envoi d'une trame,
        mais on pourrais en envoyer avec une methode plus complexe qui prend un ArrayList<Frames>
        http://www.devzoneoriginal.com/2019/05/21/Java-Socket-Example-for-sending-and-recieving-byte-array/
        this may be a good way
*/

    Frames sendFrame() throws IOException {
        //TODO on devrais faire une methode qui transforme la trame en byte array

        Frames frames = new Frames('I', "test");
        String stringFrame = frames.formatFrameToSend();

        //byte[] frameToSend = frames.formatFrameToSend();
        out.println(stringFrame);
        System.out.println(stringFrame);

        //printing request to console
        System.out.println("Sent to server : " + stringFrame);
        System.out.println(frames.getData());

        String result = in.readLine();

        String res = result;
        // printing reply to console
        System.out.println("Recieved from server : " + res);

        out.println(frames.formatFrameToSend());
        //out.flush();
        // we want to see printed what we send
        //printing request to console
        System.out.println("Sent to server : " + stringFrame);

        // Receiving reply from server
        result = in.readLine();

        //TODO une methode qui convertis la reponse en frame
        //Frames response = Frames.byteToFrames(result);
        //resultat = new Frames('c'); // TODO test

        // printing reply to console
        System.out.println("Recieved from server : " + result);

        return new Frames(result);
    }


    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }


}
