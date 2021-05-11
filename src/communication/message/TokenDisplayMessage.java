package communication.message;

import common.device.state.BraceletNotification;
import communication.base.Message;
import communication.base.MessageType;

public class TokenDisplayMessage extends Message {

    private BraceletNotification braceletNotification;

    private int tokenID;

    public TokenDisplayMessage(int tokenID,BraceletNotification braceletNotification) {
        super(MessageType.TOKEN_DISPLAY_MESSAGE);
        this.braceletNotification = braceletNotification;
        this.tokenID = tokenID;
    }

    public int getTokenID() {
        return tokenID;
    }

    public BraceletNotification getBraceletNotification() {
        return braceletNotification;
    }
}
