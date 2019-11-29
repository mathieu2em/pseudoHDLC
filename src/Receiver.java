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
            System.out.print("received from client" + inputLine);

            if(frame.getType() == 'I') System.out.println(" " + frame.getData());
            else System.out.println(" connection demand");

            processFrame(frame);
        }
    }

    private void processFrame(Frames frames) throws IOException {
        char type = frames.getType();
        if (type == 'C'){
            System.out.println("received a connection tram asking for Go Back End, will now send answer");
            out.println(genRR(0).formatFrameToSend());
        } else if (type == 'I'){
            verifyDataFrame(frames);
        } else if (type == 'P'){
            out.println(genRR(renduOu).formatFrameToSend());
        } else if (type == 'F'){
            stop();
        }
    }

    public void verifyDataFrame(Frames trame)
    {
        byte[] trameEnByteArray = trame.getFrameToByteArray();

        int nombreOctetsSansLesFlags = trameEnByteArray.length - 2;

        byte[] trameSansFlags = new byte[nombreOctetsSansLesFlags];
        System.arraycopy(trameEnByteArray, 1, trameSansFlags, 0, nombreOctetsSansLesFlags);

        int[] arrayIntAValider = trame.byteArrToArr10(trameSansFlags);

        if (trame.divideByCRC(arrayIntAValider) != null) {
            out.println(genREJ(renduOu).formatFrameToSend());
            System.out.println("Sending frame REJ " + renduOu % 8 + " to client" );
        }

        else if (!verifierNumCorrespondAuCompteur(trameEnByteArray[2], (byte)renduOu))
        {
            out.println(genREJ(renduOu % 8).formatFrameToSend());
            System.out.println("Sending frame REJ " + renduOu % 8 + " to client" );

        }
        else if ( (trameEnByteArray[2] & 0b10000000) == 0b10000000)
        {
            out.println(genRR((renduOu % 8) + 1).formatFrameToSend()); //  RR(renduOu % 8 + 1) car pour RRx, x = valeur de la trame à recevoir (donc + 1 pour prochaine trame)
            System.out.println("Sending frame RR " + ((renduOu % 8) + 1) + " to client" );
        }
        else
        {
            renduOu++;
            System.out.println("Received from client frame " + (renduOu % 8) + "containing : " +  new Frames(trame.formatFrameToSend()).getData() );
        }
       /* if (trame.divideByCRC(arrayIntAValider) != null) {
            out.println(genREJ(renduOu).formatFrameToSend());
            System.out.println("Sending frame REJ " + renduOu % 8 + " to client" );
        }

        else if (!verifierNumCorrespondAuCompteur(trameEnByteArray[2], (byte)renduOu))
        {
            out.println(genREJ(renduOu).formatFrameToSend());
            System.out.println("Sending frame REJ " + renduOu % 8 + " to client" );

        }
        else if ( (trameEnByteArray[2] & 0b10000000) == 0b10000000)
        {
            out.println(genRR(renduOu).formatFrameToSend()); // réponse RR(renduOu % 8 + 1)
            System.out.println("Sending frame RR " + renduOu % 8 + " to client" );
        }
        else
            {
                renduOu++;
                System.out.println("Received from client frame number " + trameEnByteArray[2] + "containing : " +  new Frames(trame.formatFrameToSend()).getData() );
            }*/
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

    public boolean verifierNumCorrespondAuCompteur(byte num, byte compteur)
    {
        if (num == compteur)
            return true;
        else
            return false;
    }
}