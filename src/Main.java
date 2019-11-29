import java.io.*;
import java.util.Scanner;


// for now I just set a sender/receiver but as basic as possible using this guide
// https://www.baeldung.com/a-guide-to-java-sockets

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to pseudoHDLC program what would you like to do?:\n" +
                "[1] : start a Receiver\n" +
                "[2] : start a Transmitter");

        String choice = scanner.nextLine();

        if(choice.equals("1")) receiverProtocol();
        else if (choice.equals("2")) transmitterProtocol();
        else System.out.println("wrong option choice");
    }

    private static void receiverProtocol() throws IOException {
        System.out.println("you chose \" start a receiver \"");
        Receiver server = new Receiver();
        server.start();
        System.out.println("receiver started");
        server.listen();
    }

    private static void transmitterProtocol() throws IOException {
        System.out.println("you chose \"start a transmitter\" ");
        Transmitter client = new Transmitter();
        client.startConnection();

        // wait that user click 1 to sent a tram asking for connection
        queryCommand("send a tram asking for connection and use of Go-Back-N (REJ)", "1");
        Frames frames = new Frames('C');

        // creates frame containing Num => 0 (for Go-Back-N) and Type => C sends it and wait for answer
        Frames response = client.sendFrame(frames);//new Frames('C'));
        // the received frame should be of type ?? containing ???
        // TODO
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
