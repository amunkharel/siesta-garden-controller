package communication.command;

import common.device.state.InitializationState;
import communication.base.Message;
import communication.base.MessageType;

public class VehicleInitialization extends Message {
    private InitializationState initializationState;
    
    public VehicleInitialization(InitializationState initializationState) {
        super(MessageType.VEHICLE_INITIALIZATION);
        this.initializationState = initializationState;
    }

    public InitializationState getInitializationState() {
        return initializationState;
    }

}
