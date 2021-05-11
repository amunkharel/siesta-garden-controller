package communication.message;

import communication.base.Message;
import communication.base.MessageType;

public class EmergencyDeesclation extends Message {

    public EmergencyDeesclation() {
        super(MessageType.EMERGENCY_DEESCALATION);
    }
}
