/* structure is : | Flag | Type | Num | Data | CRC | Flag |
   flag : 1 octet (01111110)
   Type : can be I , C , A, R, F, P
   Num  : 1 octet specify the number of the tram sent (I) or the receipt nbr (RR,REJ)
   Data : variable size , used to carry data, if receipt tram : size = null , size calculated by detecting flags
   CRC  : contains the checksum computed using CRC , 2 octets, checksum is calculated on Type, Num and Data
 */
public class Tram {

    private byte FlagI;
    private byte FlagC;
    private byte FlagA;
    private byte FlagR;
    private byte FlagF;
    private byte FlagP;

    private byte Type;
    private byte Num;
    //private <type> Data;
    //private <type> CRC;
}
