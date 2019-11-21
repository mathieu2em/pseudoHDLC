/* structure is : | Flag | Type | Num | Data | CRC | Flag |
   flag : 1 octet (01111110)
   Type : can be I , C , A, R, F, P
   Num  : 1 octet specify the number of the tram sent (I) or the receipt nbr (RR,REJ)
   Data : variable size , used to carry data, if receipt tram : size = null , size calculated by detecting flags
   CRC  : contains the checksum computed using CRC , 2 octets, checksum is calculated on Type, Num and Data
 */
public class Frames {

    private byte flag = 0b01111110;
    /*
    private byte I;
    private byte C;
    private byte A;
    private byte R;
    private byte F;
    private byte P;
    */
    private Character type;
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
    //private <type> Data;
    //private <type> CRC;

}
