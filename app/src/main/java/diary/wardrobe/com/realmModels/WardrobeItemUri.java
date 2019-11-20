package diary.wardrobe.com.realmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user on 12/3/2016.
 */

public class WardrobeItemUri extends RealmObject {
    @PrimaryKey
    int id = 0;

    String itemsUri;
    int timesUsed=0;
    boolean putToWashAndIron=false;

    public int getTimesUsed() {
        return timesUsed;
    }

    public void setTimesUsed(int timesUsed) {
        this.timesUsed = timesUsed;
    }



    public boolean isPutToWashAndIron() {
        return putToWashAndIron;
    }

    public void setPutToWashAndIron(boolean putToWashAndIron) {
        this.putToWashAndIron = putToWashAndIron;
    }





    public String getItemsUri() {
        return itemsUri;
    }

    public void setItemsUri(String itemsUri) {
        this.itemsUri = itemsUri;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
