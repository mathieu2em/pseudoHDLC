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
    //private DataOutputStream out;
    //private DataInputStream in;
    private PrintWriter out;
    private BufferedReader in;

    Transmitter() {
    }

    ArrayList<Frames> readFile(String filePath) throws FileNotFoundException {

        ArrayList<Frames> frameList = new ArrayList<>();
        Scanner scanner = new Scanner(new File(filePath));
        int lineNBR = 0;

        Character frameType = 'I';
        while(scanner.hasNextLine()){
            Frames frame = new Frames(frameType, scanner.nextLine(), lineNBR++);
            frameList.add(frame);
        }

        return frameList;
    }

    // reads a file and send it
    // TODO implement the HDLC PROTOCOL
    void sendFile(String filePath) throws IOException, InterruptedException {
        ArrayList<Frames> fileFrames = readFile(filePath); // the frames to send
        int framesSent = 0; // how many frames have been sent
        int framesReceived = 0; // how many frames have been received from server and confirmed
        int canSend = Math.min(8,fileFrames.size()); // how many frames we can send until next receipt confirmation
        //TODO buffer

        while (framesReceived<fileFrames.size()){
            // TODO modify for buffer
            for (int i=0; i<canSend; i++) {
                Frames frameToSend = fileFrames.get(framesSent + i);
                // hack for RR send a right moment TODO buffer quand le buffer est vide on fait ca
                if( i == canSend-1) frameToSend.setNum((byte)(frameToSend.getNum() & 0b10000000));

                sendFrame(frameToSend);
                framesSent++;
            }
            // now wait 3 seconds and check for the RR to be received
            TimeUnit.SECONDS.sleep(3);
            // if nothing received send a request for RR
            if(!in.ready()) {
                sendFrame(new Frames('P', 0));
            }
            // receive the answer
            Frames answer = new Frames(in.readLine());
            // check if its a RR or a REJ
            char type = answer.getType();
            if(type=='A'){ // RR
                // modify the buffer to send what we can send until next answer
                // if we were able to send 7 trams and we receive rr 7 then nothing changes
                // if we receive RR 4 then we can now only send 4 trams before waiting
                framesReceived = answer.getNum() + 1;
                if( framesReceived == framesSent){
                    canSend = 8;
                } else {
                    canSend = canSend - (framesSent - framesReceived);
                }
                // TODO on ajuste le buffer avec les nouveaux elements
            } else if(type=='B'){
                Frames frameToSend;
                for(int i=framesSent-answer.getNum();i<canSend; i++) {
                    frameToSend = fileFrames.get(framesSent + i);
                    sendFrame(frameToSend);
                }
                // hack for RR send a right moment
                // if( i == canSend-1) frameToSend.setNum((byte)(frameToSend.getNum() & 0b10000000));

                sendFrame(frameToSend);
                framesSent++;
            }
            // on rajoute les framesSent - num derniers elements
        }
    }

    void startConnection() throws IOException {
        clientSocket = new Socket("127.0.0.1", 6666);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    Frames sendFrame(Frames frames) throws IOException {
        String stringFrame = frames.formatFrameToSend();

        // send the frame as a string
        out.println(stringFrame);

        //printing request to console
        System.out.println("Sent to server : " + stringFrame);
        System.out.println("containing : " + frames.getData());

        String result = in.readLine();

        // printing reply to console
        System.out.println("Recieved from server : " + result);
        Frames frames1 = new Frames(result);
        System.out.println("of Type : " + frames1.getType());

        return frames1;
    }


    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }


}
