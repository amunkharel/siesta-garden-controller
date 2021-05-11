package communication.message;

import communication.base.Message;
import communication.base.MessageType;

public class PassengerRegistered extends Message {
    private final int passengerToken;
    private final String vehicleName;

    public PassengerRegistered(int passengerToken, String vehicleName){
        super(MessageType.PASSENGER_REGISTRATION);
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
