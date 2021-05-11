package SGC;

import communication.base.Message;
import communication.command.EmergencyHandled;
import communication.command.EmergencyOverride;
import communication.message.AudioBroadcast;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoudSpeakerController implements Runnable{

    private BlockingQueue<Message> messageQueue;
    private boolean shutDown;

    public LoudSpeakerController() {
        messageQueue = new LinkedBlockingQueue<>();
        shutDown = false;
    }

    @Override
    public void run(){
        Message message;
        while (!shutDown) {
            try {
                message = messageQueue.take();
                switch(message.getMessageType()){
                    case EMERGENCY_OVERRIDE:
                        EmergencyOverride emergencyOverride = (EmergencyOverride) message;
                        //handle
                        break;
                    case EMERGENCY_HANDLED:
                        EmergencyHandled emergencyHandled = (EmergencyHandled) message;
                        //handle
                        break;
                    case AUDIO_BROADCAST:
                        AudioBroadcast audioBroadcast = (AudioBroadcast) message;
                        playAudio(audioBroadcast.getAudioPath());
                        break;
                    default:
                        System.out.println("NEVER GONNA GIVE YOU UP");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void playAudio(String path) {
        System.out.println("AUDIO PLAYING: " + path);
        Media media = new Media(new File(path).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.play();
    }

    public void shutDown() {
        shutDown = true;
    }

    public BlockingQueue<Message> getMessageQueue() {
        return messageQueue;
    }

}
