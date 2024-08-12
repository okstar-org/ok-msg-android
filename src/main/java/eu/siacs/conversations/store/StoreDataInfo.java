package eu.siacs.conversations.store;

public class StoreDataInfo {
    private static final StoreManager storeManager = StoreManager.getInstance();

     public static void putData(String value) {
         storeManager.putData(StoreDataKey.DATA_KEY,value);
     }

     public static String getData() {
         return storeManager.getData(StoreDataKey.DATA_KEY,"");
     }


}
