package common.device.proxy;


import common.device.state.TokenKioskState;

import java.util.concurrent.LinkedBlockingQueue;

public interface TokenKioskProxy {

    LinkedBlockingQueue<Integer>  receiptQueue = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<Integer>  tokenIDQueue = new LinkedBlockingQueue<>();


    default void putTokenID(int tokenID){
        try {
            tokenIDQueue.put(tokenID);
        } catch (InterruptedException e) { }
    }

    default Integer getTokenID(){
        try {
            return tokenIDQueue.take();
        } catch (InterruptedException e) { }
        return null;
    }

    default void putReceiptID(int receipt){
        try {
            receiptQueue.put(receipt);
        } catch (InterruptedException e) { }
    }
    default Integer getReceiptID(){
        try {
            return receiptQueue.take();
        } catch (InterruptedException e) { }
        return null;
    }

    TokenKioskState getState();

    void shutDown();

    void restart();
}
