package diary.wardrobe.com.wardrobediary;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

import io.realm.Realm;

/**
 * Created by user on 12/27/2016.
 */

public class WardRobeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

// Initialize Realm. Should only be done once when the application starts.
        Realm.init(this);
        Fresco.initialize(this);
    }
}
