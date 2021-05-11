package communication.message;

import common.Coordinate;
import common.device.state.TrexCondition;
import common.device.state.TrexState;
import communication.base.Message;
import communication.base.MessageType;

public class TrexStatus extends Message {
    private TrexCondition trexCondition;
    private TrexState trexState;
    private Coordinate location;

    public TrexStatus(TrexCondition condition, TrexState trexState, Coordinate location){
        super(MessageType.T_REX_STATUS);
        this.trexCondition = condition;
        this.trexState = trexState;
        this.location = location;
    }

    public TrexCondition getTrexCondition(){
        return trexCondition;
    }

    public Coordinate getLocation(){
        return location;
    }

    public TrexState getTrexState() {
        return trexState;
    }

}
