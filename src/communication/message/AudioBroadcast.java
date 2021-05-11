package communication.message;

import communication.base.Message;
import communication.base.MessageType;

public class AudioBroadcast extends Message {
    private String audioPath;
    public AudioBroadcast(String path){
        super(MessageType.AUDIO_BROADCAST);
        this.audioPath = path;
    }

    public String getAudioPath() {
        return audioPath;
    }
}
