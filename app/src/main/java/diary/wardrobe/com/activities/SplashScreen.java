package diary.wardrobe.com.activities;

/**
 * Created by user on 11/24/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import diary.wardrobe.com.wardrobediary.MainActivity;
import diary.wardrobe.com.wardrobediary.R;

public class SplashScreen extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView myTextView=(TextView)findViewById(R.id.botom_desc);
        TextView title=(TextView)findViewById(R.id.wardrobe_title);
        Typeface typeFace= Typeface.createFromAsset(getAssets(),"Roboto-Light.ttf");
        Typeface medium= Typeface.createFromAsset(getAssets(),"Roboto-Medium.ttf");
        myTextView.setTypeface(typeFace);
        title.setTypeface(medium);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}