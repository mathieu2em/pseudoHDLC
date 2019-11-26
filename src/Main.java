import java.net.*;
import java.io.*;
import java.util.Scanner;


// for now I just set a sender/receiver but as basic as possible using this guide
// https://www.baeldung.com/a-guide-to-java-sockets

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to pseudoHDLC program what would you like to do?:\n" +
                "[1] : start a Receiver" +
                "[2] : start a Transmitter");

        String choice = scanner.nextLine();

        if(choice.equals("1"))
        {
            System.out.println("you chose \" start a receiver \"");
            //TODO again , trying stuff
            Receiver server = new Receiver();
            server.start(6666);
            System.out.println("receiver started");
        }
        else if (choice.equals("2"))
        {
            System.out.println("you chose \"start a transmitter\" ");
            Transmitter client = new Transmitter();
            client.startConnection("127.0.0.1", 6666);
            do {
                System.out.println("press 1 to send a tram asking for connection and use of Go-Back-N (REJ)");
                choice = scanner.nextLine();
            } while(!choice.equals("1"));
            // cree une trame de demande de connection avec utilisation de Go-Back-N (REJ)
            // donc une trame contenant pour numero => 0 et pour type => C
            Frames response = client.sendFrame(new Frames('C'));
        }
        else {
            System.out.println("wrong option choice");
        }
    }
}
