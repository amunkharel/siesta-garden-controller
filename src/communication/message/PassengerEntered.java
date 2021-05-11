package communication.message;

import communication.base.Message;
import communication.base.MessageType;

public class PassengerEntered extends Message {
    private final int passengerToken;
    private final String vehicleName;

    public PassengerEntered(int passengerToken, String vehicleName){
        super(MessageType.PASSENGER_ENTERED_VEHICLE);
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
