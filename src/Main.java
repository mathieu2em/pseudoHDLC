import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

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
        Receveur server = new Receveur();
        server.start();
        System.out.println("receiver started");
        server.listen();
    }

    private static void transmitterProtocol(Scanner scanner) throws IOException, InterruptedException {
        System.out.println("you chose \"start a transmitter\" ");
        Transmetteur client = new Transmetteur();
        ArrayList<Trame> trameToSend;

        client.startConnection();

        // wait that user click 1 to sent a tram asking for connection
        queryCommand("send a tram asking for connection and use of Go-Back-N (REJ)", "1");
        Trame frame = new Trame('C');
        client.sendFrame(frame);

        // creates frame containing Num => 0 (for Go-Back-N) and Type => C sends it and wait for answer
        Trame result = new Trame(client.in.readLine());//new Frames('C'));

        if (result.getType() == 'A') {
            System.out.println("server accepted connection with Go-Back-N by sending back RR with num 0");
        }
        String choix = "";
        while (!choix.equals("1")) {
            System.out.println("========== Bienvenue dans la section de tests. Quel cas voulez-vous tester? ==========\n" +
                    "[1] : transmission sans erreur\n" +
                    "[2] : transmission avec trame perdue\n" +
                    "[3] : transmission avec erreur CRC\n" +
                    "[4] : transmission avec Pbit");
            choix = scanner.nextLine();

            //TEST CAS TRAME PERDUE
            if (choix.equals("1"))
            {
                System.out.println("nom du fichier?");
                String filename = scanner.nextLine();
                ArrayList<Trame> trames = client.readFile(filename);

                client.sendFile(trames, 0);
            }

            else if (choix.equals("2"))
            {
                System.out.println("nom du fichier?");
                String filename = scanner.nextLine();

                trameToSend = client.readFile(filename);

                client.sendFile(trameToSend, 2);
            }

            //TEST CAS TRAME ERRONEE
            else if (choix.equals("3"))
            {
                System.out.println("Nom du fichier?");
                String filename = scanner.nextLine();

                trameToSend = client.readFile((filename));

                client.sendFile(trameToSend, 3);
            }

            else if (choix.equals("4"))
            {
                System.out.println("Nom du fichier?");
                String filename = scanner.nextLine();

                trameToSend = client.readFile((filename));

                client.sendFile(trameToSend, 4);
            }

            System.out.println(" pour quitter [1]\n" +  "pour continuer [2]");
            client.out.println("next");
            choix = scanner.nextLine();
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
}
