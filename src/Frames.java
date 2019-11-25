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
    private BitVector RCR = new BitVector();
    // private byte[] generator = [0b10001000000100001];
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
    public Frames(Character type){
        // if connection demand , num=0 to ask for Go-Back-N
        if (type=='C'){
            this.Num = 0b00000000; // Num= 0 means that we ask for Go-Back-N
        }
    }

    //private <type> Data;
    //private <type> CRC;




    // TODO trouver une facon d'assembler correctement la tram pour l'envoi selon la structure plus haut
    public ArrayList<Byte> toByteArray(String lineOfFrame)
    {
        byte[] arrayOfByte = lineOfFrame.getBytes();

        ArrayList<Byte> byteArrayList = new ArrayList<>();

        //here we add the flag, type and num for each frame
        byteArrayList.add(this.flag);
        byteArrayList.add((byte)this.type);
        byteArrayList.add(this.Num);

        for (int i = 0; i < arrayOfByte.length; i++ )
        {
            byteArrayList.add(arrayOfByte[i]);
        }

        //appel de la fonction computeCRC()

        byteArrayList.add();
        return arrayOfByte;
    }

    public int[] divideByCRC(int messageToEncode[], int CRC[])
    {
        int remainder[];
        int data[] = new int[messageToEncode.length + CRC.length];
        System.arraycopy(messageToEncode, 0, data, 0, messageToEncode.length);

        remainder = new int[CRC.length];
        System.arraycopy(data, 0, remainder, 0, CRC.length);

        for (int i = 0; i < messageToEncode.length; )

    }
    /*public byte[] computeCRC(ArrayList<Byte> arrayListOfByte)
    {
        byte[] byteArray = new byte[arrayListOfByte.size()];
        ArrayList<Boolean> encodedMessage = new ArrayList<>();
        for (int i = 0; i < byteArray.length; i++)
        {
            //byteArray[i] = (byte) arrayListOfByte.get(i);
            if (byteArray[i] != generator[i]) //XOR result is 1, add 1
                encodedMessage.add(true);
            else                        //XOR result is 0, add 0
                encodedMessage.add(false);
        }


    }*/

}
