package SGC;
import common.RandomTokenGenerator;

import java.util.ArrayList;
import java.util.HashMap;

public class TokenStorageManager{
    private static HashMap<String, ArrayList<Integer>> map = new HashMap<>();
    private final ArrayList<Integer> tokenRFIDS = new ArrayList<>();


    public void storeId(int tokenId) {
        this.tokenRFIDS.add(tokenId);
    }

    public  void registerTokenIdToVehicle(String vehicleID,int tokenId){
        ArrayList<Integer> vId = map.getOrDefault(vehicleID,null);
        if(vId==null){
            ArrayList<Integer> newVehicleList = new ArrayList<>();
            newVehicleList.add(tokenId);
            map.put(vehicleID,newVehicleList);
        }else {
            vId.add(tokenId);
        }
    }

    public void deRegisterPassenger(String vehicleID, int tokenId) {
        ArrayList<Integer> vId = map.getOrDefault(vehicleID,null);
        if(vId !=null){
            vId.remove((Integer)tokenId);
        }
    }

    public void removeId(int tokenId) {
        this.tokenRFIDS.remove(tokenId);
    }

    public ArrayList<Integer> getTokenIds() {
        return tokenRFIDS;
    }

    public static HashMap<String, ArrayList<Integer>> getMap() {
        return map;
    }
}
