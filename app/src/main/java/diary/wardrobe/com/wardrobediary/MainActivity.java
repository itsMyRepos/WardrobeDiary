package diary.wardrobe.com.wardrobediary;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import diary.wardrobe.com.Utility.Config;
import diary.wardrobe.com.Utility.NotificationUtils;
import diary.wardrobe.com.activities.BrowserActivity;
import diary.wardrobe.com.activities.DryCleanActivity;
import diary.wardrobe.com.activities.WardrobeActivity;
import diary.wardrobe.com.activities.constant.AppConstants;
import diary.wardrobe.com.adapters.RealMDateWiseDataAdapter;
import diary.wardrobe.com.customviews.CalendarView;
import diary.wardrobe.com.customviews.Utility;
import diary.wardrobe.com.realmModels.Event;
import diary.wardrobe.com.realmModels.Item;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private LinearLayoutManager lLayout;
    private ImageView btnplan;
    private CardView cardView;
    private FirebaseAnalytics firebaseAnalytics;
    private TextView calenderEvent, userEvent;
    RecyclerView rView;
    ArrayList<String> event;
    public Handler handler;
    ArrayList<String> desc;
    String formattedDate;
    //admob
    private AdView mAdView;
    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private static final int PERMISSION_REQUEST_CODE = 1;
    SharedPreferences sharedpreferences;
    TourGuide mTourGuideHandler;
    boolean  firstTimeMainActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //admob setup
        MobileAds.initialize(this, getString(R.string.appid_admob));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        cardView = (CardView) findViewById(R.id.emptycard);
        setSupportActionBar(toolbar);
        sharedpreferences = getSharedPreferences(AppConstants.MyPREFERENCES, Context.MODE_PRIVATE);

        calenderEvent = (TextView) findViewById(R.id.calenderEvent);
        userEvent = (TextView) findViewById(R.id.userEvent);
        desc = new ArrayList<String>();
        handler = new Handler();
        /*Anlytics data code*/
        // Obtain the Firebase Analytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        firebaseAnalytics.setAnalyticsCollectionEnabled(true);
        //for horizontal view like Myntra
        lLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);

        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, 0);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "WebActivity");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
//User Engaement session
        firebaseAnalytics.setMinimumSessionDuration(5000);
        //session timeout
        firebaseAnalytics.setSessionTimeoutDuration(1000000);
        //User Properties
        firebaseAnalytics.setUserId(String.valueOf(0));
        firebaseAnalytics.setUserProperty("Food", "Kishor");
        /*Anylatics data*/
        //plan button
        btnplan = (ImageView) findViewById(R.id.datebtnplan);
        btnplan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tkn = FirebaseInstanceId.getInstance().getToken();
                Log.d("Not", "Token [" + tkn + "]");
                startActivity(new Intent(MainActivity.this, CalendarView.class));
                if(firstTimeMainActivity) {
                    mTourGuideHandler.cleanUp();
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(AppConstants.MainActivity,false);
                    editor.commit();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        rView = (RecyclerView) findViewById(R.id.recycler_view);
//prmission relaed

        if (checkPermission()) {

            afterPermissionGranted();
        } else {
            requestPermission();
        }
        //prmission related
        firstTimeMainActivity=sharedpreferences.getBoolean(AppConstants.MainActivity, true);
        if (firstTimeMainActivity) {
            //firsttime use
            showFirstimeTutorial();
        }

    }

    private boolean checkPermission() {
        int rwExternalresult = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int calender = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR);

        if (rwExternalresult == PackageManager.PERMISSION_GRANTED && calender == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }
    }

    // Fetches reg id from shared preferences
    // and displays on the screen
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);

        if (!TextUtils.isEmpty(regId)) {
            // txtRegId.setText("Firebase Reg Id: " + regId);
        } else {
            // txtRegId.setText("Firebase Reg Id is not received yet!");
        }
    }


    public void getCalenderEventsInfo() {
        handler.post(calendarUpdater); // generate some calendar items
    }

    public Runnable calendarUpdater = new Runnable() {

        @Override
        public void run() {
            // Print dates of the current week
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String itemvalue;
            event = Utility.readCalendarEvent(MainActivity.this);
            Log.d("=====Event====", event.toString());
            Log.d("=====Date ARRAY====", Utility.startDates.toString());
            for (int i = 0; i < Utility.startDates.size(); i++) {
                if (Utility.startDates.get(i).equals(df.format(new Date())) && !desc.contains(Utility.nameOfEvent.get(i))) {
                    desc.add(Utility.nameOfEvent.get(i));
                }
            }

            if (desc.size() > 0) {
                String event = "";
                for (int i = 0; i < desc.size(); i++) {

                    if (i > 0) {
                        event = event + ",";
                    }
                    event = event + desc.get(i);

                }
                calenderEvent.setText(event);
            } else {
                calenderEvent.setText("No Events");
            }

        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.wardrobe_add) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_wardrobe) {
            // Handle the camera action
            startActivity(new Intent(this, WardrobeActivity.class));
        } else if (id == R.id.nav_dryclen_list) {
            startActivity(new Intent(this, DryCleanActivity.class));
        } else if (id == R.id.nav_slideshow) {
//            startActivity(new Intent(this, WebActivity.class));
            openInAppBrowser("http://www.mensxp.com/fashion/shoes/40682-shahid-kapoor-just-wore-3-shoes-that-are-driving-us-crazy-like-crazy.html");
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void openInAppBrowser(String url) {
        Intent intent = new Intent(this, BrowserActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
         //   Toast.makeText(this, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CALENDAR}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    //  Snackbar.make(view,"Permission Granted, Now you can access location data.",Snackbar.LENGTH_LONG).show();
                  //  Toast.makeText(this, "Permission granted.", Toast.LENGTH_LONG).show();
                    afterPermissionGranted();
                } else {
                    requestPermission();
                    //  Snackbar.make(view,"Permission Denied, You cannot access location data.",Snackbar.LENGTH_LONG).show();
                  Toast.makeText(this, "Permission not  granted.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    private void afterPermissionGranted() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        formattedDate = df.format(new Date());

        Realm.init(MainActivity.this);
        Realm realm = Realm.getDefaultInstance();

        OrderedRealmCollection<Item> itemsList = realm.where(Item.class).equalTo("date", formattedDate).findAll();
        if (itemsList.size() > 0) {
            cardView.setVisibility(View.GONE);
            Event event = realm.where(Event.class).equalTo("date", formattedDate).findFirst();
            if (event != null && event.getEvent() != null && event.getEvent().length() > 0) {
                userEvent.setText(event.getEvent());
            } else {
                userEvent.setText("No Event.");
            }
            RealMDateWiseDataAdapter realMDateWiseDataAdapter = new RealMDateWiseDataAdapter(MainActivity.this, itemsList);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(MainActivity.this, 2);
            rView.setLayoutManager(lLayout);
//            rView.addItemDecoration(new GridSpacingItemDecoration(2, Utility.dpToPx(WebActivity.this, 10), true));
//            rView.setItemAnimator(new DefaultItemAnimator());
            rView.setAdapter(realMDateWiseDataAdapter);
        } else {
            cardView.setVisibility(View.VISIBLE);
        }
        getCalenderEventsInfo();
        //load ads
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
//for notification
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    //txtMessage.setText(message);
                }
            }
        };

        displayFirebaseRegId();

    }


    public void showFirstimeTutorial() {
        Animation animation = new TranslateAnimation(0f, 0f, 200f, 0f);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        animation.setInterpolator(new BounceInterpolator());

        ToolTip toolTip = new ToolTip()
                .setTitle("Calender")
                .setDescription("Welcome to Wardrobe Diary!.Let start using this app by using calender")
                .setTextColor(Color.parseColor("#bdc3c7"))
                .setBackgroundColor(Color.parseColor("#e74c3c"))
                .setShadow(true)
                .setGravity(Gravity.TOP | Gravity.LEFT)
                .setEnterAnimation(animation);


        mTourGuideHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                .setPointer(new Pointer())
                .setToolTip(toolTip)
                .setOverlay(new Overlay())
                .playOn(btnplan);
    }


}
