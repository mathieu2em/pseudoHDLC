import jdk.nashorn.internal.runtime.BitVector;
import java.util.ArrayList;

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

        throw new java.lang.UnsupportedOperationException("NO !!!");
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


    // TODO trouver une facon d'assembler correctement la tram pour l'envoi selon la structure plus haut
    public ArrayList<Byte> toByteArray(String lineOfFrame) {
        byte[] arrayOfByte = lineOfFrame.getBytes();

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        //here we add the flag, type and num for each frame
        byteArrayList.add(this.flag);
        byteArrayList.add((byte) this.type);
        byteArrayList.add(this.Num);

        for (int i = 0; i < arrayOfByte.length; i++) {
            byteArrayList.add(arrayOfByte[i]);
        }

        //appel de la fonction computeCRC()

        byteArrayList.add();
        return arrayOfByte;
    }

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
    }

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