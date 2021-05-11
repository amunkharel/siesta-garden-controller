package communication.message;

import communication.base.Message;
import communication.base.MessageType;

public class PassengerDeRegistration extends Message {
    private final int passengerToken;
    private final String vehicleName;

    public PassengerDeRegistration(int passengerToken, String vehicleName){
        super(MessageType.PASSENGER_DEREGISTRATION);
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
