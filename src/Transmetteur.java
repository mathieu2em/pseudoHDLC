import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class Transmetteur {

    PrintWriter out;
    BufferedReader in;

    Transmetteur() {}

    ArrayList<Trame> readFile(String fileName) throws FileNotFoundException {

        ArrayList<Trame> frameList = new ArrayList<>();
        Scanner scanner = new Scanner(new File(fileName)); // hardcoded pour les tests
        int lineNBR = 0;

        Character frameType = 'I';

        while(scanner.hasNextLine())
        {
            Trame frame = new Trame(frameType, scanner.nextLine(), lineNBR++);
            frameList.add(frame);
        }

        return frameList;
    }

    void sendFile(ArrayList<Trame> trames, int choix) throws IOException, InterruptedException {
        int peutEnvoyer = 7; // nbr de trames qu'on peut envoyer avant d'attendre retour
        int nombreTramesEnvoyees = 0; // nbr de trames dont on a recu la confirmation de reception
        int i = 0;

        boolean pasEncoreEteSabotte = true;

        System.out.println("se prepare a l'envoi du fichier contenant " + trames.size() + " trames");

        while (nombreTramesEnvoyees < trames.size()-1)
        {
            while(peutEnvoyer>0 && i<trames.size() )
            {
                if(choix==2 && i==trames.size()-2 && pasEncoreEteSabotte)
                {
                    System.out.println(" ( saute la trame " + i%8 + " contenant " + trames.get(i).getData() + " pour le test )");
                    // do not send frame
                    pasEncoreEteSabotte = false;
                }
                else if(choix==3 && i==trames.size()-2 && pasEncoreEteSabotte)
                {
                    System.out.println(" ( bousille le crc du frame " + i%8 + " contenant " + trames.get(i).getData() + " pour le test )");
                    // do not send frame
                    sendFrameBadCRC(trames.get(i));
                    pasEncoreEteSabotte = false;
                }
                else {
                    sendFrame(trames.get(i));
                }

                System.out.println(
                        "Envoi de la trame " + ((int) trames.get(i).getNum() % 8) +
                                " comportant le contenu : " +
                                trames.get(i).getData());
                peutEnvoyer--;
                i++;
            }

            // attend 3 sec et envoit pbit si rien recu
            TimeUnit.SECONDS.sleep(3);
            if (choix == 4 && nombreTramesEnvoyees == 6) in.readLine(); // perds la reponse RR 7 si on est en mode test option 4

            // il n'y a pas de réponse du receveur
            if (!in.ready())
            {
                System.out.println("Rien recu pour 3 secondes, envoie une trame de type P");
                sendFrame(new Trame('P', 0));
            }
            Trame reponseDuReceveur = new Trame(in.readLine());
            char typeDeReponse = reponseDuReceveur.getType();

            //réponse RR qui est du # de la dernière trame reçue + 1
            if (typeDeReponse == 'A') {
                peutEnvoyer = Math.min(7, trames.size() - i);
                nombreTramesEnvoyees = reponseDuReceveur.getNum();

                System.out.println("serveur a repondu avec RR contenant num : " + reponseDuReceveur.getNum()%8);

            }

            // reponse REJ
            else {
                System.out.println("serveur a repondu avec REJ contenant num : " + reponseDuReceveur.getNum()%8);
                //ramener nombre de trames envoyées à la dernière trame non reçue
                int indexTrameRenvoi = reponseDuReceveur.getNum();
                System.out.println(" serveur doit renvoyer a partir de trame " + indexTrameRenvoi%8);
                nombreTramesEnvoyees = indexTrameRenvoi;
                i = indexTrameRenvoi;
                peutEnvoyer = 7- i%8;
            }
        }
    }

    void startConnection() throws IOException {
        Socket clientSocket = new Socket("127.0.0.1", 6666);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    void sendFrame(Trame trame) {
        String stringFrame = trame.formatFrameToSend();

        stringFrame = bitStuff(stringFrame);

        // send the frame as a string
        out.println(stringFrame);
    }

    private String bitStuff(String frameString){
        int counter = 0;

        for(int i = 0; i<frameString.length(); i++)
        {
            if(frameString.charAt(i)=='1')
            {
                counter++;
                if(counter==5)
                {
                    frameString = charAdd0At(frameString, i + 1);
                    counter = 0;
                }
            }
            else if(frameString.charAt(i)=='0')
            {
                counter=0;
            }
        }
        return frameString;
    }

    private static String charAdd0At(String str, int p) {
        return str.substring(0, p) + '0' + str.substring(p);
    }

    private void sendFrameBadCRC(Trame trame) {
        String stringFrame = trame.formatFrameToSend();

        byte[] frameBytes = Trame.stringToByte(stringFrame);

        frameBytes[frameBytes.length-2] = (byte)(~frameBytes[frameBytes.length-2]);

        stringFrame = Trame.arr10ToString(Trame.byteArrToArr10(frameBytes));

        stringFrame = bitStuff(stringFrame);

        // send the frame as a string
        out.println(stringFrame);
    }
}
