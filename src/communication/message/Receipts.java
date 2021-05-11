package communication.message;

import communication.base.Message;
import communication.base.MessageType;

import java.util.HashMap;

public class Receipts extends Message {
    private HashMap<Integer,Integer> receipts;
    public Receipts(HashMap<Integer,Integer> receipts) {
        super(MessageType.RECEIPTS);
        this.receipts = receipts;
    }

    public HashMap<Integer,Integer> getReceipts() {
        return receipts;
    }
}
