import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/* the transmitter has to :
   1 - read file's data
   2 - product and send trams
   3 - handle receipts
   4 - re-send the data in case of error
*/
public class Transmitter {

    private Socket clientSocket;
    PrintWriter out;
    BufferedReader in;

    Transmitter() {}

    ArrayList<Frames> readFile(String filePath) throws FileNotFoundException {

        ArrayList<Frames> frameList = new ArrayList<>();
        Scanner scanner = new Scanner(new File("test.txt")); // TODO hardcoded pour les tests
        int lineNBR = 0;

        Character frameType = 'I';
        while(scanner.hasNextLine()){
            Frames frame = new Frames(frameType, scanner.nextLine(), lineNBR++);
            frameList.add(frame);
        }

        return frameList;
    }

    public void sendFile(ArrayList<Frames> trames, int choix) throws IOException, InterruptedException {
        int peutEnvoyer = 7; // nbr de trames qu'on peut envoyer avant d'attendre retour
        int nombreTramesEnvoyees = 0; // nbr de trames dont on a recu la confirmation de reception

        while (nombreTramesEnvoyees <= trames.size()) // **
        {
            for (int i = nombreTramesEnvoyees; i < trames.size(); i++)
            {
                if (peutEnvoyer >= 0)
                {
                    sendFrame(trames.get(i));
                    System.out.println(
                            "Envoi de la trame " + ((int)trames.get(i).getNum()% 8) + " comportant le contenu : " + trames.get(i).getData() );
                    peutEnvoyer--;
                    nombreTramesEnvoyees++;
                }
            }

            // attend 3 sec et envoit pbit
            TimeUnit.SECONDS.sleep(3);

            if(choix==4 && nombreTramesEnvoyees==6) in.readLine(); // perds la reponse RR 7 si on est en mode test option 4
            // il n'y a pas de réponse du receveur
            if (!in.ready())
            {
                System.out.println("nothing received for 3 seconds : send P request");
                sendFrame(new Frames('P', 0));
            }
            Frames reponseDuReceveur = new Frames(in.readLine());
            char typeDeReponse = reponseDuReceveur.getType();

            if (typeDeReponse == 'A') //réponse RR qui est du # de la dernière trame reçue + 1
            {
                peutEnvoyer = 7;
            }

            else // reponse REJ
            {
                //ramener nombre de trames envoyées à la dernière trame non reçue
                int numDeLaTrameARenvoyer = reponseDuReceveur.getNum();
                nombreTramesEnvoyees = numDeLaTrameARenvoyer;
            }
        }
    }

    void startConnection() throws IOException {
        clientSocket = new Socket("127.0.0.1", 6666);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    Frames sendFrame(Frames frames) throws IOException {
        String stringFrame = frames.formatFrameToSend();

        stringFrame = bitStuff(stringFrame);

        // send the frame as a string
        out.println(stringFrame);

        //printing request to console
        //System.out.println("Sent to server : " + stringFrame + " of Type : " + frames.getType() + " with data " + frames.getData());

        String result = in.readLine();

        // printing reply to console
        System.out.print("Recu du server : " + result);
        Frames frames1 = new Frames(result);
        System.out.println(" du Type : " + frames1.getType() + " contenant numero " + frames1.getNum());

        return frames1;
    }

    private String bitStuff(String frameString){
        int counter = 0;
        for(int i = 0; i<frameString.length(); i++){
            if(frameString.charAt(i)=='1'){
                counter++;
                if(counter==5) {
                    frameString = charAdd0At(frameString, i + 1);
                    counter = 0;
                }
            }
            else if(frameString.charAt(i)=='0'){
                counter=0;
            }
        }
        return frameString;
    }

    public static String charAdd0At(String str, int p) {
        return str.substring(0, p) + '0' + str.substring(p);
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public void sendFileWithBadCRC(ArrayList<Frames> trames, int badFrameIndex) throws IOException, InterruptedException {
        int peutEnvoyer = 7; // nbr de trames qu'on peut envoyer avant d'attendre retour
        int nombreTramesEnvoyees = 0; // nbr de trames dont on a recu la confirmation de reception

        while (nombreTramesEnvoyees <= trames.size()) // **
        {
            for (int i = nombreTramesEnvoyees; i < trames.size(); i++)
            {
                if (peutEnvoyer >= 0)
                {
                    if(i == badFrameIndex) sendFrameBadCRC(trames.get(i));
                    else sendFrame(trames.get(i));
                    System.out.println(
                            "Envoi de la trame " + ((int)trames.get(i).getNum()% 8) + " comportant le contenu : " + trames.get(i).getData() );
                    peutEnvoyer--;
                    nombreTramesEnvoyees++;
                }
            }

            // il n'y a pas de réponse du receveur
            if (!in.ready())
            {
                // attend 3 sec et envoit pbit
                TimeUnit.SECONDS.sleep(3);
                sendFrame(new Frames('P', 0));
            }

            // s'il y a une reponse du receveur
            if (in.ready())
            {
                Frames reponseDuReceveur = new Frames(in.readLine());
                char typeDeReponse = reponseDuReceveur.getType();

                if (typeDeReponse == 'A') //réponse RR qui est du # de la dernière trame reçue + 1
                {
                    peutEnvoyer = 7;
                }

                else // reponse REJ
                {
                    //ramener nombre de trames envoyées à la dernière trame non reçue
                    int numDeLaTrameARenvoyer = (int)reponseDuReceveur.getNum();
                    nombreTramesEnvoyees = numDeLaTrameARenvoyer;
                }
            }
        }
    }

    private void sendFrameBadCRC(Frames frames) throws IOException {
        String stringFrame = frames.formatFrameToSend();

        byte[] frameBytes = frames.stringToByte(stringFrame);

        frameBytes[frameBytes.length-2] = (byte)(~frameBytes[frameBytes.length-2]);

        stringFrame = frames.arr10ToString(Frames.byteArrToArr10(frameBytes));

        stringFrame = bitStuff(stringFrame);

        // send the frame as a string
        out.println(stringFrame);

        //printing request to console
        System.out.println("Sent to server : " + stringFrame + " of Type : " + frames.getType() + " with data " + frames.getData());

        String result = in.readLine();

        // printing reply to console
        System.out.println("Recieved from server : " + result);
        Frames frames1 = new Frames(result);
        System.out.println("of Type : " + frames1.getType());
    }
}
