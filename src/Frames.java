
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

/* structure is : | Flag | Type | Num | Data | CRC | Flag |
   flag : 1 octet (01111110)
   Type : can be I , C , A, R, F, P
   Num  : 1 octet specify the number of the tram sent (I) or the receipt nbr (RR,REJ)
   Data : variable size , used to carry data, if receipt tram : size = null , size calculated by detecting flags
   CRC  : contains the checksum computed using CRC , 2 octets, checksum is calculated on Type, Num and Data
 */
public class Frames {

    private byte flag = 0b01111110;
    private int[] CRC = {1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,1};
    /*
    private byte I;
    private byte C;
    private byte A;
    private byte R;
    private byte F;
    private byte P;
    */
    private char type;
    private String data;
    private byte Num; // TODO , il faut qu'on trouve une bonne facon de numeroter nos trames

    /*
    there is different types of frames :
    I : trame d’information
    C : demande de connexion (prend Num = 0 pour Go-Back-N)
    A : accusé de réception (RR), le champ Num est utilisé dans ce cas pour
        le numéro à acquitter.
    R : rejet de la trame Num et de toutes celles envoyées après (REJ)
    F : fin de la communication.
    P : trame avec P bit, équivalente à P bit.
    TODO il faut gerer tous les types possibles dans le constructeur
    */
    public Frames(String nextLine, Character type) {

        this.type = type;
        this.data = nextLine;


    }

    // constructeur pour les frames qui n'ont pas besoin de data
    public Frames(Character type) {
        // if connection demand , num=0 to ask for Go-Back-N
        if (type == 'C') {
            this.Num = 0b00000000; // Num= 0 means that we ask for Go-Back-N
        }
    }

    //private <type> Data;
    //private <type> CRC;

    public static int[] byteArrToArr10(byte[] bytes){

        ArrayList<Integer> arrayOfBits = new ArrayList<Integer>();

        // for each array of bytes store every individual byte in the array list in the correct order
        for(int i=bytes.length-1; i>=0; i--){
            int bits = bytes[i];

            for(int j=0; j<8; j++){
                arrayOfBits.add(bits&1); // for example 1010110101 & 000000001 = 1 else 0
                bits >>= 1; // now evaluate next one
            }
        }
        // convert the arrayList of Int to int array
        int[] intArr = new int[arrayOfBits.size()];
        for(int i=0; i<arrayOfBits.size(); i++){
            intArr[i] = arrayOfBits.get(arrayOfBits.size()-i-1);
        }

        return intArr;
    }

    // format the frame to convert it to byte array to send it properly through the socket
    public byte[] formatFrameToSend() {
        byte[] arrayOfByte = data.getBytes();

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        //here we add the flag, type and num for each frame
        byteArrayList.add(this.flag);
        byteArrayList.add((byte) this.type);
        byteArrayList.add(this.Num);

        for (int i = 0; i < arrayOfByte.length; i++) {
            byteArrayList.add(arrayOfByte[i]);
        }

        byte[] arrayCRC = new byte[arrayOfByte.length + 2];
        arrayCRC[0] = (byte)this.type;
        arrayCRC[1] = this.Num;
        for(int i = 2; i<arrayCRC.length; i++){
            arrayCRC[i] = arrayOfByte[i-2];
        }
        //appel de la fonction computeCRC()
        int[] data = byteArrToArr10(arrayCRC);

        int[] CRCresult = divideByCRC(data);

        byteArrayList.addAll(convertToByteArray(CRCresult));

        byteArrayList.add(flag);

        byte[] result = new byte[byteArrayList.size()];
        for(int i=0; i<result.length; i++){
            result[i] = byteArrayList.get(i);
        }

        return result;
    }

    //TODO verifier si les bits restent dans le bon ordre
    public ArrayList<Byte> convertToByteArray(int[] intArr){

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        double val = 0;
        for(int i=intArr.length-1; i>=0; i--){
            int j = i%8;
            if(j == 0 && i!=0){
                byteArrayList.add((byte)val);
                val = 0;
            }
            if(intArr[i] == 1){
                val = val + Math.pow(2, j);
            }
        }

        Collections.reverse(byteArrayList);

        // convert arraylist to array
        byte[] bytes = new byte[byteArrayList.size()];
        for(int i=0; i<byteArrayList.size(); i++){
            bytes[i] = byteArrayList.get(i);
        }

        return byteArrayList;
    }

    /*
    public int[] getEncodedMessage(int messageToEncode[])
    {
        int remainder[] = divideByCRC(messageToEncode);
        for(int i=0 ; i < messageToEncode.length ; i++) {
            System.out.print(messageToEncode[i]);
        }
        for(int i=0 ; i < remainder.length-1 ; i++) {
            System.out.print(remainder[i]);
        }
        //return
    }*/

    public int[] divideByCRC(int messageToEncode[]) {
        int remainder[];

        int data[] = new int[messageToEncode.length + CRC.length];
        System.arraycopy(messageToEncode, 0, data, 0, messageToEncode.length);

        //array that stores the remainder. remainder's bits initially set to the data bits
        remainder = new int[CRC.length];
        System.arraycopy(data, 0, remainder, 0, CRC.length);

        //loop continuously EXOR the bits of the remainder (data) and CRC
        for (int i = 0; i < messageToEncode.length; i++) {
            if (remainder[0] == 1) {
                for (int j = 1; j < CRC.length; j++) {
                    remainder[j - 1] = exor(remainder[j], CRC[j]);
                }
            } else {
                for (int j = 1; j < CRC.length; j++) {
                    remainder[j - 1] = exor(remainder[j], 0);
                }
            }
            remainder[CRC.length - 1] = data[i + CRC.length];
        }
        return remainder;
    }

    //XOR operation with 2 bits given in entry
    public int exor(int a, int b) {
      /*  if (a == b) {
            return 0;
        }*/
        return a^b;
    }

    private int[] appendRemainderToData(int[] remainder, int[] data) {
        int[] encodedMessage = new int[data.length + remainder.length];
        System.arraycopy(data, 0, encodedMessage, 0, data.length);
        System.arraycopy(remainder, 0, encodedMessage, data.length, remainder.length);

        return encodedMessage;
    }
}