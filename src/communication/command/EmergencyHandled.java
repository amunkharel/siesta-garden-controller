package communication.command;

import communication.base.Message;
import communication.base.MessageType;

public class EmergencyHandled extends Message {
    public EmergencyHandled() {
        super(MessageType.EMERGENCY_HANDLED);
    }
}
