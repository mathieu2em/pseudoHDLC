import java.net.*;
import java.io.*;

/**
 * the receiver has to be able to:
 *    1 - receive trams
 *    2 - verify presence of errors
 *    3 - produce and send receipts
 *    4 - send REJ in case of errors
 */
class Receveur {

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
        Trame frame;
        while ((inputLine = in.readLine()) != null) {
            if(inputLine.equals("next")){
                System.out.println("\n\n");
                renduOu = 0;
                continue;
            }
            inputLine = bitUnstuff(inputLine);
            frame = new Trame(inputLine);
            //System.out.println(frame.getData());
            //System.out.print("received from client " + inputLine);

            //if(frame.getType() == 'I') System.out.println(" " + frame.getData());
            //else System.out.println(" connection demand");

            processFrame(frame, inputLine);
        }
    }

    private void processFrame(Trame trame, String frameStr) throws IOException {
        char type = trame.getType();
        if (type == 'C'){
            System.out.println("Recu une trame de connection demandant go back N, va maintenant envoyer une reponse");
            out.println(genRR(0).formatFrameToSend());
        } else if (type == 'I'){
            verifyDataFrame(trame, frameStr);
        } else if (type == 'P'){
            System.out.println("recu demande P de la part du client.");
            Trame rr = genRR(renduOu);
            out.println(rr.formatFrameToSend());
            System.out.println("Envoie RR contenant " + rr.getNum()%8);

        } else if (type == 'F'){
            stop();
        }
    }

    private void verifyDataFrame(Trame trame, String frameStr) throws IOException {
        System.out.println("Recu du client: Tram avec num " + trame.getNum()%8 + " contenant : " +  trame.getData() );

        byte[] trameEnByteArray = Trame.getFrameToByteArray(frameStr);

        int nombreOctetsSansLesFlags = trameEnByteArray.length - 2;

        byte[] trameSansFlags = new byte[nombreOctetsSansLesFlags];
        System.arraycopy(trameEnByteArray, 1, trameSansFlags, 0, nombreOctetsSansLesFlags);

        int[] arrayIntAValider = Trame.byteArrToArr10(trameSansFlags);
        arrayIntAValider = Trame.divideByCRC(arrayIntAValider);

        boolean CRCwrong = false;
        for (int value : arrayIntAValider) {
            if (value == 1) {
                CRCwrong = true;
                break;
            }
        }

        if (CRCwrong){
            System.out.println("Une erreur a ete detectee dans le CRC");
            out.println(genREJ(renduOu).formatFrameToSend());
            System.out.println("Envoie Trame REJ " + renduOu % 8 + " au client " );
            String inputLine = in.readLine();
            inputLine = bitUnstuff(inputLine);
            Trame trameToErase = new Trame(inputLine);
            System.out.println("Recu du client: Trame avec num " + trameToErase.getNum()%8 + " contenant : " +  trameToErase.getData() );
            //System.out.println(" verifying if " + framesToErase.getNum()%8 + " == " + renduOu);
            while (trameToErase.getNum() != renduOu){
                inputLine = in.readLine();
                inputLine = bitUnstuff(inputLine);
                trameToErase = new Trame(inputLine);
                System.out.println("Recu du client: Tram avec num " + trameToErase.getNum()%8 + " contenant : " +  trameToErase.getData() );

            }
            renduOu++;
        }

        else if (!verifierNumCorrespondAuCompteur(trameEnByteArray[2], (byte)renduOu)){
            out.println(genREJ(renduOu).formatFrameToSend());
            System.out.println("Le compteur a détecté une trame manquante");
            System.out.println("Envoie trame REJ " + renduOu % 8 + " au client " );

            String inputLine = in.readLine();
            inputLine = bitUnstuff(inputLine);
            Trame trameToErase = new Trame(inputLine);
            System.out.println("Recu du client: Tram avec num " + trameToErase.getNum()%8 + " contenant : " +  trameToErase.getData() );
            //System.out.println(" verifying if " + framesToErase.getNum()%8 + " == " + renduOu);
            while (trameToErase.getNum() != renduOu){
                inputLine = in.readLine();
                inputLine = bitUnstuff(inputLine);
                trameToErase = new Trame(inputLine);
                System.out.println("Recu du client: Tram avec num " + trameToErase.getNum()%8 + " contenant : " +  trameToErase.getData() );
            }
            renduOu++;
        }
        else{
            if((renduOu)%8 == 6) {
                System.out.println(" moment d'envoyer un RR ");
                //  RR((renduOu+1) % 8) car pour RRx, x = valeur de la trame à recevoir
                //  (donc + 1 pour prochaine trame)
                out.println(genRR((renduOu+1) % 8).formatFrameToSend());
                System.out.println("Envoi Trame RR " + ((renduOu+1) % 8) + " au client ");
            }
            renduOu++;
        }
    }

    private void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    // returns a frame of type R => REJ and the num of the tram rejected
    private Trame genREJ(int num){
        return new Trame('R', num);
    }

    // returns a frame of type A => RR that confirms that Receiver received the tram number "num"
    private Trame genRR(int num){
        return new Trame('A', num);
    }

    private boolean verifierNumCorrespondAuCompteur(byte num, byte compteur){
        return num == compteur;
    }
    private String bitUnstuff(String frameString){
        int counter = 0;
        for(int i = 0; i<frameString.length(); i++){
            if(frameString.charAt(i)=='1') counter++;
            else if(frameString.charAt(i)=='0'){
                if(counter >= 5) frameString = charRm0At(frameString, i);
                counter=0;
            }
        }
        return frameString;
    }

    private static String charRm0At(String str, int p) {
        return str.substring(0, p) + str.substring(p + 1);
    }
}