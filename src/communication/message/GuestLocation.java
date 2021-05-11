package communication.message;

import common.Coordinate;
import communication.base.Message;
import communication.base.MessageType;

public class GuestLocation extends Message {
    private int identity;
    private Coordinate coordinate;
    public GuestLocation(int identity, Coordinate coordinate){
        super(MessageType.GUEST_LOCATION);
        this.identity = identity;
        this.coordinate = coordinate;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getIdentity() {
        return identity;
    }
}
