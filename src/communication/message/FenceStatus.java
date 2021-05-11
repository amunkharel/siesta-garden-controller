package communication.message;


import common.device.state.FenceState;
import communication.base.Message;
import communication.base.MessageType;

public class FenceStatus extends Message {

    private FenceState fenceState;

    public FenceStatus(FenceState status){
        super(MessageType.FENCE_STATUS);
        this.fenceState = status;
    }

    public FenceState getStatusType(){
        return this.fenceState;
    }
}
