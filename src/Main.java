import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


// for now I just set a sender/receiver but as basic as possible using this guide
// https://www.baeldung.com/a-guide-to-java-sockets

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to pseudoHDLC program what would you like to do?:\n" +
                           "[1] : start a Receiver\n" +
                           "[2] : start a Transmitter");

        String choice = scanner.nextLine();

        if(choice.equals("1")) receiverProtocol();
        else if (choice.equals("2")) transmitterProtocol(scanner);
        else System.out.println("wrong option choice");
    }

    private static void receiverProtocol() throws IOException {
        System.out.println("you chose \" start a receiver \"");
        Receiver server = new Receiver();
        server.start();
        System.out.println("receiver started");
        server.listen();
    }

    private static void transmitterProtocol(Scanner scanner) throws IOException, InterruptedException {
        System.out.println("you chose \"start a transmitter\" ");
        Transmitter client = new Transmitter();
        ArrayList<Frames> framesToSend;

        client.startConnection();

        // wait that user click 1 to sent a tram asking for connection
        queryCommand("send a tram asking for connection and use of Go-Back-N (REJ)", "1");
        Frames frames = new Frames('C');

        // creates frame containing Num => 0 (for Go-Back-N) and Type => C sends it and wait for answer
        Frames result = client.sendFrame(frames);//new Frames('C'));

        if(result.getType() == 'A'){
            System.out.println("server accepted connection with Go-Back-N by sending back RR with num 0");
        }

        System.out.println("========== Bienvenue dans la section de tests. Quel cas voulez-vous tester? ==========\n" +
                "[1] : transmission sans erreur\n" +
                "[2] : transmission avec trame perdue\n" +
                "[3] : transmission avec erreur CRC\n" +
                "[4] : transmission avec Pbit");
        String choix = scanner.nextLine();
        
        //TODO C'EST CETTE PARTIE QU'IL RESTE À INCORPORER DANS LE MAIN POUR LES DIFFÉRENTS CAS DE TESTS
        //TEST CAS TRAME PERDUE
        if (choix.equals("1")) {
            System.out.println("nom du fichier?");
            String filename = scanner.nextLine();
            ArrayList<Frames> trames = client.readFile(filename);

            client.sendFile(trames, 0);
        }
        else if (choix.equals("2")) {
            System.out.println("nom du fichier?");
            String filename = scanner.nextLine();

            framesToSend = enleverTrameDeListePourTestTramePerdue(client.readFile(filename));

            client.sendFile(framesToSend, 0);
        }
        //TEST CAS TRAME ERRONEE
        else if (choix.equals("3")) {
            System.out.println("Nom du fichier?");
            String filename = scanner.nextLine();

            framesToSend = client.readFile((filename));

            client.sendFileWithBadCRC(framesToSend, 6);
        }
        else if (choix.equals("4")){
            System.out.println("Nom du fichier?");
            String filename = scanner.nextLine();

            framesToSend = client.readFile((filename));

            client.sendFile(framesToSend, 4);
        }
    }

    private static void queryCommand(String query, String approvementString){
        Scanner scanner = new Scanner(System.in);
        String choice;
        do {

            System.out.println("press " + approvementString + " and press ENTER to " + query);
            choice = scanner.nextLine();

        } while(!choice.equals(approvementString));
    }

    private static ArrayList<Frames> enleverTrameDeListePourTestTramePerdue(ArrayList<Frames> listeTrames){
        listeTrames.remove(6);
        return listeTrames;
    }

    private static String bitFlipper(String frameString, int pos) {
        return frameString.substring(0, pos) + ((frameString.charAt(pos)=='1')?'0':'1') + frameString.substring(pos);
    }

    public static boolean verifierNumCorrespondAuCompteur(byte num, byte compteur)
    {
        if (num == compteur)
            return true;
        else
            return false;
    }
}
