package common;

import java.util.*;

public class RandomTokenGenerator {
    private static RandomTokenGenerator randomTokenGenerator;
    private  final Random RANDOM = new Random();
    private  final int tokenNumber = 100;



    private HashMap<Integer,Integer> tokenIDMap = new HashMap<>();
    private ArrayList<Integer> tokenIds;
    private  ArrayList<Integer>  receiptIds;

    private RandomTokenGenerator(){
        tokenIds = getRandom();
        receiptIds = getRandom();

        LinkedList<Integer> alloc = new LinkedList<>(tokenIds);
        receiptIds.forEach((r)->tokenIDMap.put(r,alloc.pop()));

    }

    public static RandomTokenGenerator getInstance(){
        if(randomTokenGenerator == null) randomTokenGenerator = new RandomTokenGenerator();
        return randomTokenGenerator;
    }

    private ArrayList<Integer> getRandom(){
        int k = 999;
        final Set<Integer> picked = new HashSet<>();
        while (picked.size() < tokenNumber) {
            picked.add(RANDOM.nextInt(k + 1));
        }
        return new ArrayList<>(picked);
    }

    public HashMap<Integer, Integer> getTokenIDMap() {
        return tokenIDMap;
    }

    public ArrayList<Integer> getTokenIds() {
        return tokenIds;
    }

    public ArrayList<Integer> getReceiptIds() {
        return receiptIds;
    }

}
