package communication.message;

import communication.base.Message;
import communication.base.MessageType;

public class PassengerExited extends Message {
    private final int passengerToken;
    private final String vehicleName;

    public PassengerExited(int passengerToken, String vehicleName){
        super(MessageType.PASSENGER_EXITED_VEHICLE);
        this.passengerToken = passengerToken;
        this.vehicleName = vehicleName;
    }

    public int getPassengerToken(){
        return passengerToken;
    }

    public String getVehicleName(){
        return vehicleName;
    }
}
