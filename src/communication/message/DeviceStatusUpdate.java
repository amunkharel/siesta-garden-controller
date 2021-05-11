package communication.message;

import common.Coordinate;
import communication.base.Message;
import communication.base.MessageType;

import java.util.Date;

public class DeviceStatusUpdate extends Message {
    private String location;
    private String deviceName;
    private String deviceStatus;
    private Coordinate coordinate;
    private Date timestamp;



    public DeviceStatusUpdate(String deviceName, String deviceStatus, Coordinate coordinate, Date timestamp){
        super(MessageType.DEVICE_STATUS_UPDATE);
        this.deviceName = deviceName;
        this.deviceStatus = deviceStatus;
        this.timestamp = timestamp;
        this.coordinate = coordinate;
    }

public DeviceStatusUpdate(String deviceName, String deviceStatus, String location, Date timestamp){
        super(MessageType.DEVICE_STATUS_UPDATE);
        this.deviceName = deviceName;
        this.deviceStatus = deviceStatus;
        this.timestamp = timestamp;
        this.location = location;
    }

    public DeviceStatusUpdate(String deviceName, String deviceStatus, Date timestamp) {
        super(MessageType.DEVICE_STATUS_UPDATE);
        this.deviceName = deviceName;
        this.deviceStatus = deviceStatus;
        this.timestamp = timestamp;
    }

    public String getDeviceName(){
        return deviceName;
    }

    public String getDeviceStatus(){
        return deviceStatus;
    }

    @Override
    public Date getTimestamp(){
        return timestamp;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "DeviceStatusUpdate{" +
                "deviceName='" + deviceName + '\'' +
                ", deviceStatus='" + deviceStatus + '\'' +
                ", coordinate='" + coordinate + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
