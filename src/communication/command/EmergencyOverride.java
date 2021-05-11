package communication.command;

import communication.base.Message;
import communication.base.MessageType;

public class EmergencyOverride extends Message {

    public EmergencyOverride() {
        super(MessageType.EMERGENCY_OVERRIDE);
    }
}
