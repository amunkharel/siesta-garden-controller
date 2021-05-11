package communication.command;

import communication.base.Message;
import communication.base.MessageType;

public class RequestOperatorEmergency extends Message {

    public RequestOperatorEmergency() {
        super(MessageType.OPERATOR_EMERGENCY_REQUEST);
    }
}
