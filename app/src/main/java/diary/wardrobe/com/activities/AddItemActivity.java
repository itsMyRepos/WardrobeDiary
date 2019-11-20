package diary.wardrobe.com.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import diary.wardrobe.com.Utility.Utils;
import diary.wardrobe.com.activities.constant.AppConstants;
import diary.wardrobe.com.adapters.RealMDateWiseDataAdapter;
import diary.wardrobe.com.customviews.GridSpacingItemDecoration;
import diary.wardrobe.com.customviews.Utility;
import diary.wardrobe.com.realmModels.Event;
import diary.wardrobe.com.realmModels.Item;
import diary.wardrobe.com.realmModels.WardrobeItemUri;
import diary.wardrobe.com.wardrobediary.R;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class AddItemActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    EditText edtEvent, edtDesc;
    private Realm realm;
    //selected date
    String dateSelected;
    //camera and gallery related.
    private String userChoosenTask;
    private final int REQUEST_CAMERA = 0, SELECT_FILE = 1, CHOOSE_FROM_WARDROBE = 3;
    //admob
    private AdView mAdView;

    String currentEvent="";
    RelativeLayout saveButton,setReminder;
    RealMDateWiseDataAdapter realMDateWiseDataAdapter;
    FloatingActionButton fab;
    TourGuide mTourGuideHandler;
    boolean  firstTimeMainActivity;
    SharedPreferences sharedpreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        edtEvent = (EditText) findViewById(R.id.edtEvent);
        edtDesc = (EditText) findViewById(R.id.edtDiscr);
        saveButton = (RelativeLayout) findViewById(R.id.save);
        setReminder=(RelativeLayout)findViewById(R.id.setReminder) ;
        saveButton.setClickable(false);
        saveButton.setBackgroundColor(getResources().getColor(R.color.gray));
        setReminder.setClickable(false);
        setReminder.setBackgroundColor(getResources().getColor(R.color.gray));

        sharedpreferences = getSharedPreferences(AppConstants.MyPREFERENCES, Context.MODE_PRIVATE);

        realm = Realm.getDefaultInstance();
        if (getIntent() != null && getIntent().getExtras() != null) {
            dateSelected = getIntent().getStringExtra("DATE");
        }
        OrderedRealmCollection<Item> itemsList = realm.where(Item.class).equalTo("date", dateSelected).findAll();
         realMDateWiseDataAdapter = new RealMDateWiseDataAdapter(AddItemActivity.this, itemsList);
        recyclerView.setAdapter(realMDateWiseDataAdapter);

        if (itemsList != null && itemsList.size() > 0) {
            // set some properties of rowTextView or something
            Event event = realm.where(Event.class).equalTo("date", dateSelected).findFirst();
            if (event != null && event.getEvent() != null && event.getEvent().length() > 0) {
                currentEvent = event.getEvent();
                edtEvent.setText(event.getEvent());
            }
            if (event != null && event.getDescription() != null && event.getDescription().length() > 0) {
                edtDesc.setText(event.getDescription());
            }


            //for reminder button
            setReminder.setClickable(true);
            setReminder.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, Utility.dpToPx(AddItemActivity.this, 10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(realMDateWiseDataAdapter);


         fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                selectImage();
                if(firstTimeMainActivity) {
                    mTourGuideHandler.cleanUp();
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(AppConstants.AddItem,false);
                    editor.commit();
                }
                Intent chooseFromWR = new Intent(AddItemActivity.this, WardrobeActivity.class);
                chooseFromWR.putExtra("SELECT_FILE", "SELECT_FILE");
                startActivityForResult(chooseFromWR, CHOOSE_FROM_WARDROBE);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //load ads
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        edtEvent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (edtEvent.getText().toString().length()>0) {
                    saveButton.setClickable(true);
                    saveButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    saveButton.setClickable(false);
                    saveButton.setBackgroundColor(getResources().getColor(R.color.gray));

                }
            }
        });
        firstTimeMainActivity=sharedpreferences.getBoolean(AppConstants.AddItem, true);
        if (firstTimeMainActivity) {
            //firsttime use
           Utils.hideKeyboard(this);
            showFirstimeTutorial();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SELECT_FILE:
                    onSelectFromGalleryResult(data);
                    break;
                case REQUEST_CAMERA:
                    onCaptureImageResult(data);
                    break;
                case CHOOSE_FROM_WARDROBE:
                    HashMap<Integer, String> hashMap = (HashMap<Integer, String>) data.getExtras().get("hashmap");
                    selectFromWardrobe(hashMap);
                    break;
            }

        }
    }

    private void selectFromWardrobe(HashMap<Integer, String> hashMap) {
        Map<Integer, String> map = (Map<Integer, String>) hashMap;
        Iterator<Map.Entry<Integer, String>> entries = map.entrySet().iterator();

        while (entries.hasNext()) {
            try {
                Map.Entry<Integer, String> entry = entries.next();
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                realm = Realm.getDefaultInstance();
                OrderedRealmCollection itemUriList = realm.where(Item.class).equalTo("id", entry.getKey()).equalTo("date",dateSelected).findAll();
                if (itemUriList.size() == 0) {
                    int nextKey = getNextKey();
                    Item item = new Item();
                    realm.beginTransaction();
                    item.setId(entry.getKey());
                    item.setDate(dateSelected);
                    item.setUri(entry.getValue());
                    item.setEvent(edtEvent.getText().toString());
                    item.setDescription(edtDesc.getText().toString());
                    realm.copyToRealm(item);
                    //for saving Wardrobe used
                    WardrobeItemUri itemUri = realm.where(WardrobeItemUri.class).equalTo("id", entry.getKey()).findFirst();
                    itemUri.setTimesUsed(itemUri.getTimesUsed() + 1);
                    //Event
                    if (edtEvent.getText().toString().trim().length() > 0) {
                        Event event = new Event();
                        event.setDate(dateSelected);
                        event.setEvent(edtEvent.getText().toString());
                        event.setDescription(edtDesc.getText().toString());
                        realm.copyToRealmOrUpdate(event);
                    }
                    realm.commitTransaction();
                }
                saveButton.setClickable(true);
                saveButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                setReminder.setClickable(true);
                setReminder.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        File destination = new File(Environment.getExternalStorageDirectory(),
                "/WardRobe Diary");

//        File direct = new File(Environment.getExternalStorageDirectory() + "/DirName");

        if (!destination.exists()) {
            File itemsDirectory = new File("/sdcard/WardRobe Diary/");
            itemsDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/WardRobe Diary/"), System.currentTimeMillis() + ".jpg");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            realm = Realm.getDefaultInstance();
            int nextKey = getNextKey();
            Item item = new Item();
            realm.beginTransaction();
            item.setId(nextKey);
            item.setDate(dateSelected);
            item.setUri(file.getPath());
            item.setEvent(edtEvent.getText().toString());
            item.setDescription(edtDesc.getText().toString());
            realm.copyToRealm(item);
            realm.commitTransaction();

            RealmResults<Item> itemses = realm.where(Item.class).findAll();
            System.out.print("");
        } catch (Exception e) {
            e.printStackTrace();
        }


        //    ivImage.setImageBitmap(thumbnail);
    }


    public int getNextKey() {
        try {
            int index = realm.where(Item.class).max("id").intValue() + 1;
            return index;
        } catch (Exception e) {
            e.printStackTrace();
        }
        {
            return 0;
        }
    }


    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                File destination = new File(Environment.getExternalStorageDirectory(),
                        "/WardRobe Diary");

//        File direct = new File(Environment.getExternalStorageDirectory() + "/DirName");

                if (!destination.exists()) {
                    File itemsDirectory = new File("/sdcard/WardRobe Diary/");
                    itemsDirectory.mkdirs();
                }

                File file = new File(new File("/sdcard/WardRobe Diary/"), System.currentTimeMillis() + ".jpg");
                if (file.exists()) {
                    file.delete();
                }

                FileOutputStream out = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                realm = Realm.getDefaultInstance();
                int nextKey = getNextKey();
                Item item = new Item();
                realm.beginTransaction();
                item.setId(nextKey);
                item.setDate(dateSelected);
                item.setUri(file.getPath());
                item.setEvent(edtEvent.getText().toString());
                item.setDescription(edtDesc.getText().toString());
                realm.copyToRealm(item);
                realm.commitTransaction();
                RealmResults<Item> itemses = realm.where(Item.class).findAll();
                System.out.print("");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //  ivImage.setImageBitmap(bm);
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Choose from Wardrobe",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddItemActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(AddItemActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("Choose from Wardrobe")) {
                    Intent chooseFromWR = new Intent(AddItemActivity.this, WardrobeActivity.class);
                    chooseFromWR.putExtra("SELECT_FILE", "SELECT_FILE");
                    startActivityForResult(chooseFromWR, CHOOSE_FROM_WARDROBE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    public void setAlarm(View v) {
        startActivity(new Intent(AddItemActivity.this, AlarmActivity.class).putExtra("DATE",dateSelected));
    }

    public void save(View v) {
        if (!currentEvent.equals(edtEvent.getText().toString())) {
            //Event
            if (edtEvent.getText().toString().trim().length() > 0) {
                realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Event event = new Event();
                event.setDate(dateSelected);
                event.setEvent(edtEvent.getText().toString());
                event.setDescription(edtDesc.getText().toString());
                realm.copyToRealmOrUpdate(event);
                realm.commitTransaction();
            }

        }
        finish();
    }
    public void showFirstimeTutorial(){
        Animation animation = new TranslateAnimation(0f, 0f, 200f, 0f);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        animation.setInterpolator(new BounceInterpolator());

        ToolTip toolTip = new ToolTip()
                .setTitle("Add Items")
                .setDescription("Select your wardrobe item from your Wardrobe!")
                .setTextColor(Color.parseColor("#bdc3c7"))
                .setBackgroundColor(Color.parseColor("#e74c3c"))
                .setShadow(true)
                .setGravity(Gravity.TOP | Gravity.LEFT)
                .setEnterAnimation(animation);


        mTourGuideHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                .setPointer(new Pointer())
                .setToolTip(toolTip)
                .setOverlay(new Overlay())
                .playOn(fab);
    }
}
