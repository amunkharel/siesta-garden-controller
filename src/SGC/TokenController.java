package SGC;

import common.device.proxy.TokenBraceletProxy;
import common.device.state.TokenState;
import communication.base.Message;
import communication.message.TokenDisplayMessage;
import communication.message.TokenStatus;
import simulation.Simulation;

import java.util.concurrent.*;

public class TokenController implements Runnable{
    private final BlockingQueue<Message> centralControlMessageQueue;
    private final BlockingQueue<Message> messageQueue;
    private ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    private int tokenID;
    private boolean shutDown;
    private TokenBraceletProxy tokenBraceletProxy;


    public TokenController(BlockingQueue<Message> messageQueue, int tokenID){
        this.centralControlMessageQueue = messageQueue;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.shutDown = false;
        this.tokenID = tokenID;
        this.tokenBraceletProxy = Simulation.getInstance().getTokeBraceletProxy(tokenID);
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartBeatStatus,0,1, TimeUnit.SECONDS);

    }

    /**
     * This is used by central control to send messages to this component
     * @param message The message being sent from central control
     */
    public void sendMessage(Message message){
        try{
            messageQueue.put(message);
        } catch(InterruptedException e){
            // This should not happen, but try again if it did
            sendMessage(message);
        }
    }

    private void sendHeartBeatStatus(){
        TokenStatus tokenStatus = new TokenStatus(tokenID, TokenState.OKAY);
        try {
            centralControlMessageQueue.put(tokenStatus);
        } catch (InterruptedException e) {}
    }

    @Override
    public void run(){
        // Do some message handling stuff
        Message message;
        while(!shutDown){
            try{
                message = messageQueue.take();
                processMessage(message);
            } catch(InterruptedException e){}
        }
    }

    private void processMessage(Message message) {
        switch(message.getMessageType()){
            case TOKEN_DISPLAY_MESSAGE:
                TokenDisplayMessage tokenDisplayMessage = (TokenDisplayMessage) message;
                switch (tokenDisplayMessage.getBraceletNotification()){
                    case VIEWING_PERIOD_START:
                        tokenBraceletProxy.notifyViewingPeriodStart();
                        break;
                    case VIEWING_PERIOD_END:
                        tokenBraceletProxy.notifyViewingPeriodEnd();
                        break;
                }
                break;
            case EMERGENCY_OVERRIDE:
                tokenBraceletProxy.notifyEmergency();
                break;
            default:
                System.out.println("TOKEN CONTROLLER RECEIVED INVALID MESSAGE: " + message.getMessageType().name()); }

    }

    private void displayMessage(Message message){
        // do nothing
    }

    public int getTokenID() {
        return tokenID;
    }
}
