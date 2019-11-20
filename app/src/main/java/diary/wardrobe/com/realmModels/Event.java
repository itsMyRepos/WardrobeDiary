package diary.wardrobe.com.realmModels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by user on 12/3/2016.
 */

public class Event extends RealmObject {
     @PrimaryKey
    String date;

    String event;

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    String Description;


    //RealmList<ItemUri> uriItems=new RealmList<ItemUri>();
    int usedTimes;

//    public RealmList<ItemUri> getUriItems() {
//        return uriItems;
//    }

    //    public void setUriItems(RealmList<ItemUri> uriItems) {
//        this.uriItems = uriItems;
//    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


}
