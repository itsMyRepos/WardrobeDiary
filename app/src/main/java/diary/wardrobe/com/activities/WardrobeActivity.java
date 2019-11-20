package diary.wardrobe.com.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import diary.wardrobe.com.activities.constant.AppConstants;
import diary.wardrobe.com.adapters.WardrobeDataAdapter;
import diary.wardrobe.com.customviews.Utility;
import diary.wardrobe.com.realmModels.WardrobeItemUri;
import diary.wardrobe.com.wardrobediary.R;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

/**
 * Created by pawan_pakalwar on 18-07-2017.
 */
public class WardrobeActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Realm realm;
    Toolbar toolbar;
    //camera and gallery related.
    private String userChoosenTask;
    FloatingActionButton selected;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    WardrobeDataAdapter realMDateWiseDataAdapter;
    OrderedRealmCollection<WardrobeItemUri> itemsList;
    Menu menu;
    String fromAddItm;
    TourGuide mTourGuideHandler;
    MenuItem addItem;
    LinearLayout emptyLay;
    Button addWItem;
    //admob
    private AdView mAdView;
    SharedPreferences sharedpreferences;
    boolean  firstTimeMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wardrobe_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_wardrobe);
        selected = (FloatingActionButton) findViewById(R.id.selected);
        realm = Realm.getDefaultInstance();
        //  toolbar=(Toolbar)findViewById(R.id.toolbar);
//        setSupportActionBar(tockar);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        itemsList = realm.where(WardrobeItemUri.class).findAll();
        emptyLay = (LinearLayout) findViewById(R.id.empty_lay);
        addWItem = (Button) findViewById(R.id.addItems);
        addWItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
                if(firstTimeMainActivity) {
                    mTourGuideHandler.cleanUp();
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(AppConstants.Wardrobe,false);
                    editor.commit();
                }

            }
        });

        sharedpreferences = getSharedPreferences(AppConstants.MyPREFERENCES, Context.MODE_PRIVATE);

        if (getIntent() != null) {
            fromAddItm = getIntent().getStringExtra("SELECT_FILE");
            if (fromAddItm != null && fromAddItm.equalsIgnoreCase("SELECT_FILE")) {
                selected.setVisibility(View.VISIBLE);
                realMDateWiseDataAdapter = new WardrobeDataAdapter(WardrobeActivity.this, itemsList, false, true);
                recyclerView.setAdapter(realMDateWiseDataAdapter);
                // selected.setImageResource();
            } else {
                realMDateWiseDataAdapter = new WardrobeDataAdapter(WardrobeActivity.this, itemsList, false, false);
                recyclerView.setAdapter(realMDateWiseDataAdapter);
            }
        }

        if (itemsList.size() == 0) {
            emptyLay.setVisibility(View.VISIBLE);
        }

        selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WardrobeActivity.this, AddItemActivity.class);
                intent.putExtra("hashmap", realMDateWiseDataAdapter.hashMap);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //load ads
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        //show case totorial
        firstTimeMainActivity=sharedpreferences.getBoolean(AppConstants.Wardrobe, true);
        if (firstTimeMainActivity && addWItem.getVisibility()==View.VISIBLE) {
            //firsttime use
            showFirstimeTutorial();
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);//Menu Resource, Menu
        this.menu = menu;
        MenuItem delete = (MenuItem) menu.findItem(R.id.delete);
        MenuItem dryclean = (MenuItem) menu.findItem(R.id.dryclean);
        addItem = (MenuItem) menu.findItem(R.id.wardrobe_add);
        if (fromAddItm != null && fromAddItm.equalsIgnoreCase("SELECT_FILE")) {
            delete.setVisible(false);
            dryclean.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.wardrobe_add:
                selectImage();
                break;
            case R.id.delete:
                Toast.makeText(this, "delete event", Toast.LENGTH_SHORT).show();
                deleteImages(realMDateWiseDataAdapter.hashMap);
                break;
            case R.id.dryclean:
                Toast.makeText(this, "Your selected clothes putted to Wash and Iron", Toast.LENGTH_SHORT).show();
                washAndIron(realMDateWiseDataAdapter.hashMap);
                break;
            case R.id.select:
                realMDateWiseDataAdapter.setCheckBoxEnable(true);
                refreshMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteImages(HashMap<Integer, String> hashmap) {
        try {
            Map<Integer, String> map = (Map<Integer, String>) hashmap;
            Iterator<Map.Entry<Integer, String>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {

                Map.Entry<Integer, String> entry = entries.next();
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                Toast.makeText(this, "nextKey" + entry.getKey(), Toast.LENGTH_SHORT).show();
                realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                WardrobeItemUri itemUri = realm.where(WardrobeItemUri.class).equalTo("id", entry.getKey()).findFirst();
                itemUri.deleteFromRealm();
                realm.commitTransaction();
            }
            RealmResults<WardrobeItemUri> itemses = realm.where(WardrobeItemUri.class).findAll();
            if (itemses.size() == 0) {
                emptyLay.setVisibility(View.VISIBLE);
            }

            realMDateWiseDataAdapter.hashMap.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void washAndIron(HashMap<Integer, String> hashmap) {
        try {
            Map<Integer, String> map = (Map<Integer, String>) hashmap;
            Iterator<Map.Entry<Integer, String>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Integer, String> entry = entries.next();
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                WardrobeItemUri itemUri = realm.where(WardrobeItemUri.class).equalTo("id", entry.getKey()).findFirst();
                itemUri.setPutToWashAndIron(true);
//            realm.copyToRealmOrUpdate(itemUri);
                realm.commitTransaction();
            }
            OrderedRealmCollection<WardrobeItemUri> itemUri = realm.where(WardrobeItemUri.class).findAll();
            if (itemUri.size() == 0) {
                emptyLay.setVisibility(View.VISIBLE);

            }
            realMDateWiseDataAdapter.hashMap.clear();
            realMDateWiseDataAdapter.setCheckBoxEnable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wardrobe_add:
                selectImage();
                break;
        }
        return super.onContextItemSelected(item);

    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(WardrobeActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(WardrobeActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
                    if (result)
                        galleryIntent();

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                File destination = new File(Environment.getExternalStorageDirectory(),
                        "/.WardRobe Diary");

//        File direct = new File(Environment.getExternalStorageDirectory() + "/DirName");

                if (!destination.exists()) {
                    File itemsDirectory = new File("/sdcard/.WardRobe Diary/");
                    itemsDirectory.mkdirs();
                }

                File file = new File(new File("/sdcard/.WardRobe Diary/"), System.currentTimeMillis() + ".jpg");
                if (file.exists()) {
                    file.delete();
                }

                FileOutputStream out = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                realm = Realm.getDefaultInstance();
                int nextKey = getNextKey();
                WardrobeItemUri item = new WardrobeItemUri();
                realm.beginTransaction();
                item.setId(nextKey);
                item.setItemsUri(file.getPath());
                realm.copyToRealm(item);
                realm.commitTransaction();
                RealmResults<WardrobeItemUri> itemses = realm.where(WardrobeItemUri.class).findAll();
                if (itemses.size() > 0) {
                    emptyLay.setVisibility(View.GONE);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //  ivImage.setImageBitmap(bm);
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        File destination = new File(Environment.getExternalStorageDirectory(),
                "/.WardRobe Diary");

//        File direct = new File(Environment.getExternalStorageDirectory() + "/DirName");

        if (!destination.exists()) {
            File itemsDirectory = new File("/sdcard/.WardRobe Diary/");
            itemsDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/.WardRobe Diary/"), System.currentTimeMillis() + ".jpg");
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
            WardrobeItemUri item = new WardrobeItemUri();
            realm.beginTransaction();
            item.setId(nextKey);
            item.setItemsUri(file.getPath());
            realm.copyToRealm(item);
            realm.commitTransaction();

            RealmResults<WardrobeItemUri> itemses = realm.where(WardrobeItemUri.class).findAll();
            if (itemses.size() > 0) {
                emptyLay.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //    ivImage.setImageBitmap(thumbnail);
    }

    public void updateAdapter() {
        realMDateWiseDataAdapter = new WardrobeDataAdapter(WardrobeActivity.this, itemsList, false, true);
        recyclerView.setAdapter(realMDateWiseDataAdapter);
        realMDateWiseDataAdapter.longClick = true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem delete = (MenuItem) menu.findItem(R.id.delete);
        MenuItem dryclean = (MenuItem) menu.findItem(R.id.dryclean);
        if (realMDateWiseDataAdapter.hashMap.size() > 0) {
            delete.setVisible(true);
            dryclean.setVisible(true);
        } else {
            delete.setVisible(false);
            dryclean.setVisible(false);
        }
        return true;
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void refreshMenu() {
        onPrepareOptionsMenu(menu);
    }

    public int getNextKey() {
        try {
            int index = realm.where(WardrobeItemUri.class).max("id").intValue() + 1;
            return index;
        } catch (Exception e) {
            e.printStackTrace();
        }
        {
            return 0;
        }
    }

    public void showFirstimeTutorial() {
        Animation animation = new TranslateAnimation(0f, 0f, 200f, 0f);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        animation.setInterpolator(new BounceInterpolator());

        ToolTip toolTip = new ToolTip()
                .setTitle("Add Items")
                .setDescription("Start adding your Wardrobe Item!")
                .setTextColor(Color.parseColor("#bdc3c7"))
                .setBackgroundColor(Color.parseColor("#e74c3c"))
                .setShadow(true)
                .setGravity(Gravity.BOTTOM | Gravity.BOTTOM)
                .setEnterAnimation(animation);


        mTourGuideHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                .setPointer(new Pointer())
                .setToolTip(toolTip)
                .setOverlay(new Overlay())
                .playOn(addWItem);
    }
}
