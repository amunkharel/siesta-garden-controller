package communication.message;

import common.device.state.TokenKioskState;
import communication.base.Message;
import communication.base.MessageType;

public class TokenKioskStatus extends Message {
    private int identity;
    private TokenKioskState tokenKioskState;
    public TokenKioskStatus(int identity,TokenKioskState tokenKioskState) {
        super(MessageType.TOKEN_KIOSK_STATUS);
        this.tokenKioskState = tokenKioskState;
        this.identity = identity;
    }

    public TokenKioskState getTokenKioskStatus() {
        return tokenKioskState;
    }

    public int getIdentity() {
        return identity;
    }
}
