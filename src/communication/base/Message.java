package communication.base;

import java.util.Date;

public abstract class Message{
    private MessageType messageType;
    private Date timestamp;

    public Message(MessageType messageType){
        this.messageType = messageType;
        // Timestamp
        this.timestamp = new Date();
    }

    public MessageType getMessageType(){
        return this.messageType;
    }

    public Date getTimestamp(){
        return timestamp;
    }
}
