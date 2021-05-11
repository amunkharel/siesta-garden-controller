package communication.command;

import communication.base.Message;
import communication.base.MessageType;

public class RequestReceipts extends Message {
    private int identity;
    public RequestReceipts(int identity) {
        super(MessageType.REQUEST_RECEIPTS);
        this.identity = identity;
    }

    public int getIdentity() {
        return identity;
    }
}

