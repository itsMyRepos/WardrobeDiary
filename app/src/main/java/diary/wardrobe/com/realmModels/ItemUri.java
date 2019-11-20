package diary.wardrobe.com.realmModels;

import io.realm.RealmObject;

/**
 * Created by user on 12/3/2016.
 */

public class ItemUri extends RealmObject {
    public String getItemsUri() {
        return itemsUri;
    }

    public void setItemsUri(String itemsUri) {
        this.itemsUri = itemsUri;
    }

    String itemsUri;
}
