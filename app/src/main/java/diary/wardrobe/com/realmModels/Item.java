package diary.wardrobe.com.realmModels;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by user on 12/3/2016.
 */

public class Item extends RealmObject {

    @Index
    int id=0;


    String date;
    String uri;
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }


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
