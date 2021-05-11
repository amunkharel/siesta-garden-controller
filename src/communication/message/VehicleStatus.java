package communication.message;

import common.Coordinate;
import common.device.state.VehicleCondition;
import communication.base.Message;
import communication.base.MessageType;

public class VehicleStatus extends Message {
    private Coordinate coordinate;
    private VehicleCondition vehicleCondition;
    private String vehicleName;
    public VehicleStatus( String vehicleName,
                          VehicleCondition vehicleCondition,
                          Coordinate coordinate) {
        super(MessageType.VEHICLE_STATUS);
        this.coordinate = coordinate;
        this.vehicleCondition = vehicleCondition;
        this.vehicleName = vehicleName;
    }

    public String getVehicleName() {
        return vehicleName;
    }
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public VehicleCondition getVehicleCondition() {
        return vehicleCondition;
    }
}
