import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class Tests {

    private Scanner scanner;
    private Frames frame;

    // Constructeur
    public Tests(Scanner scanner, Frames frames)
    {
        this.scanner = scanner;
        this.frame = frame;
    }

    // Methodes utilitaires

    public int[] bitFlipper(int[] message) {
        if (message[0] == 1)
            message[0] = 0;
        else
            message[0] = 1;
        return message;
    }

    public boolean verifierNumCorrespondAuCompteur(byte num, byte compteur)
    {
        if (num == compteur)
            return true;
        else
            return false;
    }


    // Methode Test

    public void Test(String filePath) throws IOException
    {
        scanner = new Scanner(new File(filePath));
        System.out.println("======== Bienvenue dans la section de tests. Quel cas voulez-vous tester? ==========\n" +
                "[1] : transmission sans erreur" +
                "[2] : transmission avec perte" +
                "[3] : transmission avec erreurs"); //TODO rajouter 4e cas pbit
        String choix = scanner.nextLine();

        // Connexions
        System.out.println("\n ======== Envoi des trames ========");
        Receiver server = new Receiver();
        server.start();
        System.out.println("Receveur prêt");

        Transmitter client = new Transmitter();
        client.startConnection();
        System.out.println("Transmitteur prêt");

       // CETTE PARTIE N'EST PLUS NÉCESSAIRE COMME ON FUSIONNE LES TESTS AVEC LE MAIN
      /*  // Envoi des trames
        ArrayList<Frames> frameListForTests = client.readFile(filePath);

        for (int i = 0; i< frameListForTests.size(); i++)
        {
            //client.sendFrame(frameListForTests.get(i));
        }

        // Reception de trames
        ArrayList<Byte[]> framesReceived = new ArrayList<>();
        byte compteurDeTrames = framesReceived.get(0)[2];

        for (int i = 0; i < framesReceived.size(); i ++)
        {
            for (int j = 0; j < framesReceived.get(i).length; j++) {

                int numberOfBytesWithoutFlags = framesReceived.get(i).length - 2;

                byte[] frameWithoutFlags = new byte[numberOfBytesWithoutFlags];
                System.arraycopy(framesReceived, 1, frameWithoutFlags, 0, numberOfBytesWithoutFlags);

                int[] intArrayFrameToCheck = frame.byteArrToArr10(frameWithoutFlags);

                //TEST CAS TRAME ERRONEE
                if (choix.equals("3")) {
                    if (j == 0) {
                        bitFlipper(intArrayFrameToCheck);
                    }
                }
*/
                //TODO C'EST CETTE PARTIE QU'IL RESTE À INCORPORER DANS LE MAIN POUR LES DIFFÉRENTS CAS DE TESTS
                /*//TEST CAS TRAME PERDUE
                if (choix.equals("2"))
                {
                    framesReceived = enleverTrameDeListePourTestTramePerdue(framesReceived);
                }

                if (!verifierNumCorrespondAuCompteur(framesReceived.get(i)[2], compteurDeTrames))
                {
                    System.out.println("La trame " + compteurDeTrames + " s'est perdue.");
                }
                compteurDeTrames += (byte)1;

                // Diviser par le CRC
                if (frame.divideByCRC(intArrayFrameToCheck) != null) {
                    System.out.println("La trame " + 3 + " est erronée.");
                }*/
            }
        }




