package communication.message;

import common.device.state.TokenState;
import communication.base.Message;
import communication.base.MessageType;

public class TokenStatus extends Message {
    private int identity;
    private TokenState tokenState;

    public TokenState getTokenState() {
        return tokenState;
    }

    public int getIdentity() {
        return identity;
    }

    public TokenStatus(int identity, TokenState tokenState) {
        super(MessageType.TOKEN_STATUS);
        this.identity = identity;
        this.tokenState = tokenState;
    }
}
