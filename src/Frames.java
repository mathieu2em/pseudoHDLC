
import java.util.ArrayList;
import java.util.Arrays;
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

    private char type;
    private String data;
    private byte Num;

    /*
    there is different types of frames :
    I : trame d’information
    C : demande de connexion (prend Num = 0 pour Go-Back-N)
    A : accusé de réception (RR), le champ Num est utilisé dans ce cas pour
        le numéro à acquitter.
    R : rejet de la trame Num et de toutes celles envoyées après (REJ)
    F : fin de la communication.
    P : trame avec P bit, équivalente à P bit.
    */

    //constructor for the frame re-creation
    public Frames(){}

    Frames(Character type, String nextLine) {

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

    // constructeur pour les frames RR et REJ
    Frames(Character type, int num){
        this.type = type;
        this.Num = (byte)num;
    }

    // recreates a frame from a byteArray
    Frames(String frameString){
        byte[] frameBytes = stringToByte(frameString);

        System.out.println(Arrays.toString(frameBytes));
        // set type
        this.type = (char)frameBytes[1];
        this.Num = frameBytes[2];
        if(this.type == 'I'){ // RR
            this.data = new String(Arrays.copyOfRange(frameBytes, 3, frameBytes.length - 3));
        }
    }

    //private <type> Data;
    //private <type> CRC;

    // format the frame to convert it to byte array to send it properly through the socket
    String formatFrameToSend() {
        byte[] arrayOfByte = data.getBytes();

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        //here we add the flag, type and num for each frame
        byteArrayList.add(this.flag);
        byteArrayList.add((byte) this.type);
        byteArrayList.add(this.Num);

        for (byte b : arrayOfByte) {
            byteArrayList.add(b);
        }

        // generate the byte array to modify for the crc
        byte[] arrayCRC = new byte[arrayOfByte.length + 2];
        arrayCRC[0] = (byte)this.type;
        arrayCRC[1] = this.Num;
        // add to this byte array the bytes of the data
        System.arraycopy(arrayOfByte, 0, arrayCRC, 2, arrayCRC.length - 2);
        //appel de la fonction computeCRC()
        int[] data = byteArrToArr10(arrayCRC);

        int[] CRCresult = divideByCRC(data);
        // the CRC result is added to the byteArrayList
        ArrayList<Byte> convertedCRCResult = convertToByteArrayList(CRCresult);
        System.out.println(convertedCRCResult.toString());// TODO test
        byteArrayList.addAll(convertedCRCResult);
        // the last byte flag is added
        byteArrayList.add(flag);

        // we convert the arraylist into an array
        byte[] result = new byte[byteArrayList.size()];
        for(int i=0; i<result.length; i++){
            result[i] = byteArrayList.get(i);
        }
        System.out.println("formatted" + Arrays.toString(result));
        return arr10ToString(byteArrToArr10(result));
    }

    private byte[] getFrameToByteArray(){
        return stringToByte(formatFrameToSend());
    }

    private static int[] byteArrToArr10(byte[] bytes){

        ArrayList<Integer> arrayOfBits = new ArrayList<>();

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

    private String arr10ToString(int[] fram10){
        StringBuilder result = new StringBuilder();
        for (int value : fram10) {
            result.append(value);
        }
        return result.toString();
    }

    private byte[] stringToByte(String intArr){
        byte[] result = new byte[intArr.length()/8];
        for(int i = 0; i<intArr.length(); i+=8){
            long la = Long.parseLong(intArr.substring(i, i+8), 2);
            result[i/8] = (byte)la;
        }
        return result;
    }

    //TODO verifier si les bits restent dans le bon ordre
    private ArrayList<Byte> convertToByteArrayList(int[] intArr){

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        double val = 0;
        for(int i=intArr.length-1; i>=0; i--){
            int j = i%8;
            if(j == 0 && i!=intArr.length-1){
                byteArrayList.add((byte)val);
                val = 0;
            }
            if(intArr[i] == 1){
                val = val + Math.pow(2, j);
            }
        }

        Collections.reverse(byteArrayList);

        return byteArrayList;
    }

    private int[] divideByCRC(int[] messageToEncode) {


        int r = CRC.length-1 + messageToEncode.length-CRC.length;
        // va etre le resultat mais dici la contient data
        int[] tempMessageToEncode = new int[messageToEncode.length + r - 1];
        System.arraycopy(messageToEncode,0,tempMessageToEncode,0, messageToEncode.length);
        while(r >= 0){
            if(tempMessageToEncode[0]==1) {
                for (int j = 0; j < CRC.length; j++) {
                    tempMessageToEncode[j] = xor(tempMessageToEncode[j], CRC[j]);
                }
            }
            tempMessageToEncode = bitshift(tempMessageToEncode);
            r--;
        }
        return Arrays.copyOfRange(tempMessageToEncode, 0, CRC.length-1);
    }

    private int[] bitshift(int[] ints){
        if(ints.length==1){
            return new int[]{0};
        }
        if (ints.length - 1 >= 0) System.arraycopy(ints, 1, ints, 0, ints.length - 1);
        ints[ints.length-1]=0;
        return ints;
    }

    //XOR operation with 2 bits given in entry
    private int xor(int a, int b) {
        return a^b;
    }

    /*
    private int[] appendRemainderToData(int[] remainder, int[] data) {
        int[] encodedMessage = new int[data.length + remainder.length];
        System.arraycopy(data, 0, encodedMessage, 0, data.length);
        System.arraycopy(remainder, 0, encodedMessage, data.length, remainder.length);

        return encodedMessage;
    }
    */

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte getNum() {
        return Num;
    }

    public void setNum(byte num) {
        Num = num;
    }
}