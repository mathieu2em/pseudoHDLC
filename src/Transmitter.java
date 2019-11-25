import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/* the transmitter has to :
   1 - read file's data
   2 - product and send trams
   3 - handle receipts
   4 - re-send the data in case of error
*/

public class Transmitter {

    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private Scanner scanner;

    public ArrayList<Frames> readFile(String filePath) throws FileNotFoundException {

        ArrayList<Frames> frameList = new ArrayList<>();
        scanner = new Scanner(new File(filePath));

        Character frameType = 'I'; // TODO pas sure que c'est I , a reverifier ...
        while(scanner.hasNextLine()){
            Frames frame = new Frames(scanner.nextLine(), frameType);
            frameList.add(frame);
        }

        return frameList;
    }

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);

        /*
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        */
        // sends output to the socket
        out = new DataOutputStream(clientSocket.getOutputStream());
        //takes input from socket
        in = new DataInputStream(clientSocket.getInputStream());
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
    public Frames sendFrame(Frames frame) throws IOException {
        //TODO on devrais faire une methode qui transforme la trame en byte array
        byte[] frameByteArray = {(byte)0x00, (byte)0x01, (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x01,
                (byte)0x00, (byte)0x1F, (byte)0x60, (byte)0x1D, (byte)0xA1, (byte)0x09,
                (byte)0x06, (byte)0x07, (byte)0x60, (byte) 0x85, (byte)0x74, (byte)0x05,
                (byte) 0x08, (byte)0x01, (byte)0x01, (byte)0xBE, (byte)0x10, (byte)0x04,
                (byte)0x0E, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06,
                (byte)0x5F, (byte)0x1F, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x18,
                (byte)0x1D, (byte)0xFF, (byte)0xFF};//frame.toByteArray();

        out.write(frameByteArray);

        // we want to see printed what we send
        String req = Arrays.toString(frameByteArray);
        //printing request to console
        System.out.println("Sent to server : " + req);

        // Receiving reply from server
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[1024];
        baos.write(buffer, 0 , in.read(buffer));
        byte result[] = baos.toByteArray();

        //TODO une methode qui convertis la reponse en frame
        //Frames response = Frames.byteToFrames(result);
        Frames resultat = new Frames('c'); // TODO test

        String res = Arrays.toString(result);
        // printing reply to console
        System.out.println("Recieved from server : " + res);

        return resultat;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }


}
