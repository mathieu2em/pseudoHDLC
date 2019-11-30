
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
class Trame {

    private byte flag = 0b01111110;
    private static int[] CRC = {1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,1};

    private char type;
    private String data;
    private byte Num;

    //constructor for the frame re-creation
    Trame(Character type, String nextLine, int nbr) {

        this.type = type;
        this.data = nextLine;
        this.Num = (byte)nbr;
    }

    // constructeur pour les frames qui n'ont pas besoin de data
    Trame(char type) {
        this.type = type;
        // if connection demand , num=0 to ask for Go-Back-N
        if (type == 'C')
        {
            this.Num = 0b00000000; // Num= 0 means that we ask for Go-Back-N
        }
    }

    // constructeur pour les frames RR et REJ
    Trame(char type, int num){
        this.type = type;
        this.Num = (byte)num;
    }

    // recreates a frame from a byteArray
    Trame(String frameString){
        byte[] frameBytes = stringToByte(frameString);

        // set type
        this.type = (char)frameBytes[1];
        this.Num = frameBytes[2];
        if(this.type == 'I'){
            this.data = new String(Arrays.copyOfRange(frameBytes, 3, frameBytes.length - 3));
        }
    }

    // format the frame to convert it to byte array to send it properly through the socket
    String formatFrameToSend() {
        byte[] arrayOfByte;
        byte[] arrayCRC;

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        //here we add the flag, type and num for each frame
        byteArrayList.add(this.flag);
        byteArrayList.add((byte) this.type);
        byteArrayList.add(this.Num);

        if(this.type == 'I') {
            arrayOfByte = data.getBytes();
            arrayCRC = new byte[arrayOfByte.length + 2];
            // generate the byte array to modify for the crc
            arrayCRC[0] = (byte)this.type;
            arrayCRC[1] = this.Num;

            // add to this byte array the bytes of the data
            for(int i = 0; i<arrayOfByte.length; i++){
                arrayCRC[i+2] = arrayOfByte[i];
            }

            for (byte b : arrayOfByte) {
                byteArrayList.add(b);
            }

        } else {
            arrayCRC = new byte[2];
            // generate the byte array to modify for the crc
            arrayCRC[0] = (byte)this.type;
            arrayCRC[1] = this.Num;
        }

        //appel de la fonction computeCRC()
        int[] dataCRC = byteArrToArr10(arrayCRC);

        int[] CRCresult = divideByCRC(dataCRC);
        // the CRC result is added to the byteArrayList
        ArrayList<Byte> convertedCRCResult = convertToByteArrayList(CRCresult);

        byteArrayList.addAll(convertedCRCResult);
        // the last byte flag is added
        byteArrayList.add(flag);

        // we convert the arraylist into an array
        byte[] result = new byte[byteArrayList.size()];
        for(int i=0; i<result.length; i++){
            result[i] = byteArrayList.get(i);
        }

        return arr10ToString(byteArrToArr10(result));
    }

    static byte[] getFrameToByteArray(String frameStr){
        return stringToByte(frameStr);
    }

    static int[] byteArrToArr10(byte[] bytes){

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

    static String arr10ToString(int[] fram10){
        StringBuilder result = new StringBuilder();
        for (int value : fram10) {
            result.append(value);
        }
        return result.toString();
    }

    static byte[] stringToByte(String intArr){
        byte[] result = new byte[intArr.length()/8];
        for(int i = 0; i<intArr.length(); i+=8){
            long la = Long.parseLong(intArr.substring(i, i+8), 2);
            result[i/8] = (byte)la;
        }
        return result;
    }

    private ArrayList<Byte> convertToByteArrayList(int[] intArr){

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        byte[] temp = stringToByte(arr10ToString(intArr));
        for(int i=0; i<temp.length; i++){
            byteArrayList.add(temp[i]);
        }
        return byteArrayList;
    }

    static int[] divideByCRC(int[] messageToEncode) {
        int r = CRC.length-1 + messageToEncode.length-CRC.length;

        // va etre le resultat mais dici la contient data
        int[] tempMessageToEncode = new int[messageToEncode.length + r - 1];
        System.arraycopy(messageToEncode,0,tempMessageToEncode,0, messageToEncode.length);

        while(r >= 0)
        {
            if(tempMessageToEncode[0]==1)
            {
                for (int j = 0; j < CRC.length; j++)
                {
                    tempMessageToEncode[j] = xor(tempMessageToEncode[j], CRC[j]);
                }
            }

            tempMessageToEncode = bitshift(tempMessageToEncode);
            r--;
        }

        return Arrays.copyOfRange(tempMessageToEncode, 0, CRC.length-1);
    }

    private static int[] bitshift(int[] ints){
        if(ints.length==1)
        {
            return new int[]{0};
        }
        if (ints.length - 1 >= 0) System.arraycopy(ints, 1, ints, 0, ints.length - 1);
        ints[ints.length-1]=0;
        return ints;
    }

    //XOR operation with 2 bits given in entry
    private static int xor(int a, int b) {
        return a^b;
    }

    char getType() {
        return type;
    }

    String getData() {
        return data;
    }

    byte getNum() {
        return Num;
    }
}