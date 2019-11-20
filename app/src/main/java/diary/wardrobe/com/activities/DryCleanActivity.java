package diary.wardrobe.com.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import diary.wardrobe.com.adapters.DryCleanDataAdapter;
import diary.wardrobe.com.customviews.Utility;
import diary.wardrobe.com.realmModels.Item;
import diary.wardrobe.com.realmModels.WardrobeItemUri;
import diary.wardrobe.com.wardrobediary.R;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by pawan_pakalwar on 18-07-2017.
 */
public class DryCleanActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Realm realm;
    Toolbar toolbar;
    //camera and gallery related.
    private String userChoosenTask;
    FloatingActionButton selected;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    DryCleanDataAdapter realMDateWiseDataAdapter;
    OrderedRealmCollection<WardrobeItemUri> itemsList;
    Menu menu;
    String fromAddItm;
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
        itemsList = realm.where(WardrobeItemUri.class).equalTo("putToWashAndIron",true).findAll();

        if (getIntent() != null) {
             fromAddItm = getIntent().getStringExtra("SELECT_FILE");
            if (fromAddItm != null && fromAddItm.equalsIgnoreCase("SELECT_FILE")) {
                selected.setVisibility(View.VISIBLE);
                realMDateWiseDataAdapter = new DryCleanDataAdapter(DryCleanActivity.this, itemsList, false, true);
                recyclerView.setAdapter(realMDateWiseDataAdapter);
                // selected.setImageResource();
            } else {
                realMDateWiseDataAdapter = new DryCleanDataAdapter(DryCleanActivity.this, itemsList, false, false);
                recyclerView.setAdapter(realMDateWiseDataAdapter);
            }
        }
        selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DryCleanActivity.this, AddItemActivity.class);
                intent.putExtra("hashmap", realMDateWiseDataAdapter.hashMap);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        getMenuInflater().inflate(R.menu.drycleanmenu, menu);//Menu Resource, Menu
        this.menu = menu;
//        MenuItem delete = (MenuItem) menu.findItem(R.id.delete);
//        MenuItem dryclean=(MenuItem) menu.findItem(R.id.dryclean);
//        if(fromAddItm!=null && fromAddItm.equalsIgnoreCase("SELECT_FILE")){
//            delete.setVisible(false);
//            dryclean.setVisible(false);
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
       /*     case R.id.wardrobe_add:
                selectImage();
                break;
            case R.id.delete:
                Toast.makeText(this, "delete event", Toast.LENGTH_SHORT).show();
                   deleteImages(realMDateWiseDataAdapter.hashMap);
                break;
            case R.id.dryclean:
                Toast.makeText(this, "Your selected clothes putted to Wash and Iron", Toast.LENGTH_SHORT).show();
                washAndIron(realMDateWiseDataAdapter.hashMap);
                break;*/
            case R.id.select:
                realMDateWiseDataAdapter.setCheckBoxEnable(true);
               refreshMenu();
                break;
            case R.id.restore:
               restoreToWardrobe(realMDateWiseDataAdapter.hashMap);
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
                Toast.makeText(this, "nextKey"+entry.getKey(), Toast.LENGTH_SHORT).show();
                realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                WardrobeItemUri itemUri = realm.where(WardrobeItemUri.class).equalTo("id", entry.getKey()).findFirst();
                itemUri.deleteFromRealm();
                realm.commitTransaction();
            }
            realMDateWiseDataAdapter.hashMap.clear();
            realMDateWiseDataAdapter.setCheckBoxEnable(false);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void restoreToWardrobe(HashMap<Integer, String> hashmap) {
        try {
            Map<Integer, String> map = (Map<Integer, String>) hashmap;
            Iterator<Map.Entry<Integer, String>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Integer, String> entry = entries.next();
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                WardrobeItemUri itemUri = realm.where(WardrobeItemUri.class).equalTo("id", entry.getKey()).findFirst();
                itemUri.setPutToWashAndIron(false);
                itemUri.setTimesUsed(0);
//            realm.copyToRealmOrUpdate(itemUri);
                realm.commitTransaction();
            }
            resetAdapter();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
private void resetAdapter(){
    RealmResults<WardrobeItemUri> items = realm.where(WardrobeItemUri.class).equalTo("putToWashAndIron",true).findAll();
    realMDateWiseDataAdapter = new DryCleanDataAdapter(DryCleanActivity.this, items, false, true);
    recyclerView.setAdapter(realMDateWiseDataAdapter);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(DryCleanActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(DryCleanActivity.this);

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
                WardrobeItemUri item = new WardrobeItemUri();
                realm.beginTransaction();
                item.setId(nextKey);
                item.setItemsUri(file.getPath());
                realm.copyToRealm(item);
                realm.commitTransaction();
                RealmResults<WardrobeItemUri> itemses = realm.where(WardrobeItemUri.class).findAll();
                System.out.print("");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //  ivImage.setImageBitmap(bm);
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
            WardrobeItemUri item = new WardrobeItemUri();
            realm.beginTransaction();
            item.setId(nextKey);
            item.setItemsUri(file.getPath());
            realm.copyToRealm(item);
            realm.commitTransaction();

            RealmResults<Item> itemses = realm.where(Item.class).findAll();
            System.out.print("");
            Toast.makeText(this, "nextKey"+nextKey, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //    ivImage.setImageBitmap(thumbnail);
    }

    public void updateAdapter() {
        realMDateWiseDataAdapter = new DryCleanDataAdapter(DryCleanActivity.this, itemsList, false, true);
        recyclerView.setAdapter(realMDateWiseDataAdapter);
        realMDateWiseDataAdapter.longClick = true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem restore = (MenuItem) menu.findItem(R.id.restore);

        if (realMDateWiseDataAdapter.hashMap.size() > 0) {
            restore.setVisible(true);
        } else {
            restore.setVisible(false);
        }
        return true;
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

}
