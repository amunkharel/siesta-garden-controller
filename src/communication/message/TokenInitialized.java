package communication.message;

import SGC.TokenController;
import communication.base.Message;
import communication.base.MessageType;

public class TokenInitialized extends Message {


    private TokenController tokenController;

    public TokenInitialized(TokenController tokenController) {
        super(MessageType.TOKEN_INITIALIZED);
        this.tokenController = tokenController;
    }

    public TokenController getTokenController() {
        return tokenController;
    }
}
