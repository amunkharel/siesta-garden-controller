package SGC;

import common.device.proxy.TokenKioskProxy;
import communication.base.Message;
import communication.command.RequestReceipts;
import communication.message.Receipts;
import communication.message.TokenInitialized;
import communication.message.TokenKioskStatus;
import simulation.Simulation;

import java.util.HashMap;
import java.util.concurrent.*;

public class TokenKioskController implements Runnable{
    private int identity;
    private HashMap<Integer,Integer> tokenIDMap = new HashMap<>();
    private TokenKioskProxy tokenKioskProxy = Simulation.getInstance().getKiosk();
    private ExecutorService executorService;
    private ScheduledExecutorService heartbeatExecutor;

    private final BlockingQueue<Message> centralControlMessageQueue;
    private final BlockingQueue<Message> messageQueue;
    private boolean shutDown;
    private boolean disabled;

    public TokenKioskController(int identity, BlockingQueue<Message> messageQueue){
        this.identity = identity;
        this.centralControlMessageQueue = messageQueue;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.shutDown = false;
        this.executorService = Executors.newCachedThreadPool();
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        this.executorService.submit(this::startUp);
    }

    public void startUp(){
        tokenIDMap = requestTokenReceipts();
        if(tokenIDMap == null) startUp();
        assert tokenIDMap != null;
        executorService.submit(this);
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartBeatStatus,0,1,TimeUnit.SECONDS);
        initialize();
    }

    private void initialize(){
        disabled= false;
        while(!shutDown && !disabled){
            int receiptNumber  = scan();
            int newTokenID = authenticateReceiptScan(receiptNumber);
            if(newTokenID != -1){
                dispenseToken(newTokenID);
            }
        }
    }

    private void dispenseToken(int newTokenID) {
        tokenKioskProxy.putTokenID(newTokenID);
        TokenController tokenController  = new TokenController(centralControlMessageQueue,newTokenID);
        executorService.submit(tokenController);
        try {
            centralControlMessageQueue.put(new TokenInitialized(tokenController));
        } catch (InterruptedException e) {}
    }

    private int scan(){
        return tokenKioskProxy.getReceiptID();
    }

    private int authenticateReceiptScan(int receiptNumber){
        Integer temp = tokenIDMap.getOrDefault(receiptNumber,null);
        return (temp!= null) ? temp:-1;
    }

    private void sendHeartBeatStatus(){
        TokenKioskStatus tokenKioskStatus = new TokenKioskStatus(identity,tokenKioskProxy.getState());
        try {
            centralControlMessageQueue.put(tokenKioskStatus);
        } catch (InterruptedException e) {}
    }





    private HashMap<Integer,Integer> requestTokenReceipts(){
        try {
            centralControlMessageQueue.put(new RequestReceipts(identity));
            return  ((Receipts)messageQueue.take()).getReceipts();
        } catch (InterruptedException e) { }
        return null;
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


    @Override
    public void run(){
        // Do some message handling stuff
        Message message;
        while(!shutDown){
            try{
                message = messageQueue.take();
                processMessage(message);
            } catch(InterruptedException e){ /* Don't care, loop again */ }

            // Blah Blah handling.
        }
    }

    private void processMessage(Message message) {
        switch (message.getMessageType()){
            case EMERGENCY_OVERRIDE:
                shutDown();
                break;
            case EMERGENCY_HANDLED:
                restart();
                break;
        }
    }

    private void shutDown(){
        tokenKioskProxy.shutDown();
    }

    private void restart(){
        tokenKioskProxy.restart();
        executorService.submit(this::initialize);
    }
}
