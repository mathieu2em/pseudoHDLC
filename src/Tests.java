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

    private int[] bitFlipper(int[] message) {
        if (message[0] == 1)
            message[0] = 0;
        else
            message[0] = 1;
        return message;
    }


    // Methode Test

    public void Test(String filePath) throws IOException {
        scanner = new Scanner(new File(filePath));
        System.out.println("======== Bienvenue dans la section de tests. Quel cas voulez-vous tester? ==========\n" +
                "[1] : transmission sans erreur" +
                "[2] : transmission avec perte" +
                "[3] : transmission avec erreurs");
        String choix = scanner.nextLine();

        // Connexions
        System.out.println("\n ======== Envoi des trames ========");
        Receiver server = new Receiver();
        server.start(6666);
        System.out.println("Receveur prêt");

        Transmitter client = new Transmitter();
        client.startConnection("127.0.0.1", 6666);
        System.out.println("Transmitteur prêt");

        // Envoi des trames
        ArrayList<Frames> frameListForTests = client.readFile(filePath);

        for (int i = 0; i< frameListForTests.size(); i++)
        {
            client.sendFrame(frameListForTests.get(i));
        }

        // Reception de trames
        //TODO cet arraylist doit etre retourne par une methode du receveur
        ArrayList<Frames> framesReceived = new ArrayList<>();

        for (int i = 0; i< framesReceived.size(); i++) {
            /*byte[] frameByteArray = frame.convertFrameToByteArray(framesReceived.get(i));

            int numberOfBytesWithoutFlags = frameByteArray.length - 2;

            byte[] frameWithoutFlags = new byte[numberOfBytesWithoutFlags];
            System.arraycopy(frameByteArray, 1, frameWithoutFlags, 0, numberOfBytesWithoutFlags);

            int[] intArrayFrameToCheck = frame.byteArrToArr10(frameWithoutFlags);

            //TEST CAS TRAME ERRONEE
            if (choix.equals("3")) {
                if (i == 0) {
                    bitFlipper(intArrayFrameToCheck);
                }
            }
*/
            //TEST CAS TRAME PERDUE
            byte compteurDeTrames = 0;


/*
            // Diviser par le CRC
            if (frame.divideByCRC(intArrayFrameToCheck) != null) {
                System.out.println("La trame" + framesReceived.get(i).getNum() + "est erronee.");
            } else {
                System.out.println("La trame" + framesReceived.get(i).getNum() + "ne comporte pas d'erreur.");
            }
*/
        }
    }
}
