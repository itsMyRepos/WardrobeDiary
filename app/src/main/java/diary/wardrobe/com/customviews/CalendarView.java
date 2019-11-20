/*
 * Copyright (C) 2014 Mukesh Y authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package diary.wardrobe.com.customviews;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stacktips.view.CalendarListener;
import com.stacktips.view.CustomCalendarView;
import com.stacktips.view.DayDecorator;
import com.stacktips.view.DayView;
import com.stacktips.view.utils.CalendarUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import diary.wardrobe.com.Utility.Utils;
import diary.wardrobe.com.activities.AddItemActivity;
import diary.wardrobe.com.activities.constant.AppConstants;
import diary.wardrobe.com.adapters.RealMDateWiseDataAdapter;
import diary.wardrobe.com.realmModels.Item;
import diary.wardrobe.com.wardrobediary.R;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

/**
 * @author Mukesh Y
 */
public class CalendarView extends AppCompatActivity {

    public GregorianCalendar month, itemmonth;// calendar instances.

    public CalendarAdapter adapter;// adapter instance
    public Handler handler;// for grabbing some event values for showing the dot
    // marker.
    public ArrayList<String> items; // container to store calendar items which
    // needs showing the event marker
    ArrayList<String> event;
    LinearLayout rLayout;
    ArrayList<String> date;
    ArrayList<String> desc;
    //btn date plan
    ImageView btnDatePlan;
    //for camera and gallery related
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Button btnSelect;
    private ImageView ivImage;
    private String userChoosenTask;
    private String selectedGridDate;
    private RecyclerView recyclerViewInside;
    private LinearLayoutManager lLayout;
    private Realm realm;
    private RealMDateWiseDataAdapter realMDateWiseDataAdapter;
    CollapsingToolbarLayout collapsingToolbarLayout;
    //calender items height var
    int itemCount;
    RelativeLayout header;
    int rowheight;
    int w, h;
    GridView gridview;
    int widht;
    int height;
    //Cal lib changes
    CustomCalendarView calendarView;
    public String selectedDate;
    SimpleDateFormat df;
boolean isPastDate;
    //
    TourGuide mTourGuideHandler;
    SharedPreferences sharedpreferences;
    boolean  firstTimeMainActivity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calender_app_scroll);
        Locale.setDefault(Locale.US);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rLayout = (LinearLayout) findViewById(R.id.text);
        header = (RelativeLayout) findViewById(R.id.header);
        month = (GregorianCalendar) GregorianCalendar.getInstance();
        itemmonth = (GregorianCalendar) month.clone();
        realm = Realm.getDefaultInstance();
        items = new ArrayList<String>();
        sharedpreferences = getSharedPreferences(AppConstants.MyPREFERENCES, Context.MODE_PRIVATE);

        adapter = new CalendarAdapter(this, month);
        recyclerViewInside = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(CalendarView.this, 2);
        recyclerViewInside.setLayoutManager(mLayoutManager);
        recyclerViewInside.addItemDecoration(new GridSpacingItemDecoration(2, Utility.dpToPx(CalendarView.this, 10), true));
        recyclerViewInside.setItemAnimator(new DefaultItemAnimator());
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        realm = Realm.getDefaultInstance();
        df = new SimpleDateFormat("dd-MM-yyyy");

        //btnDatePlan
        btnDatePlan = (ImageView) findViewById(R.id.datebtnplan);
        btnDatePlan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firstTimeMainActivity) {
                    mTourGuideHandler.cleanUp();
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(AppConstants.CalenderView,false);
                    editor.commit();
                }
                if(!isPastDate){
                startActivity(new Intent(CalendarView.this, AddItemActivity.class).putExtra("DATE", selectedDate));
                }else{
                    Utils.ShowOKButtonDismissDialog(CalendarView.this,"Alert"," Can't plan for past date.");

                }
//                selectImage();
            }
        });

///calender lib changes
        //Initialize CustomCalendarView from layout
        calendarView = (CustomCalendarView) findViewById(R.id.calendar_view);

//Initialize calendar with date
        Calendar currentCalendar = Calendar.getInstance(Locale.getDefault());

//Show Monday as first date of week
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);

//Show/hide overflow days of a month
        calendarView.setShowOverflowDate(false);

//call refreshCalendar to update calendar the view
        calendarView.refreshCalendar(currentCalendar);
//Handling custom calendar events
        calendarView.setCalendarListener(new CalendarListener() {
            @Override
            public void onDateSelected(Date date) {
                if (!CalendarUtils.isPastDay(date)) {
                    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                   // Toast.makeText(CalendarView.this, df.format(date), Toast.LENGTH_SHORT).show();
                    selectedDate = df.format(date);
                    //orignal code
                    isPastDate=false;
                    OrderedRealmCollection<Item> itemsList = realm.where(Item.class).equalTo("date", selectedDate).findAll();
                    RealMDateWiseDataAdapter realMDateWiseDataAdapter = new RealMDateWiseDataAdapter(CalendarView.this, itemsList);
                    recyclerViewInside.setAdapter(realMDateWiseDataAdapter);
                    desc = null;
                    TextView rowTextView = new TextView(CalendarView.this);
                    rowTextView.setPadding(10, 5, 10, 5);
                    rowTextView.setTextColor(Color.BLACK);
                    rLayout.removeAllViews();
                    if (itemsList.size() > 0) {
                        for (int i = 0; i < itemsList.size(); i++) {
                            // set some properties of rowTextView or something
                            rowTextView.setText("Event:" + itemsList.get(i).getEvent());
                            // add the textview to the linearlayout
                        }
                    } else {
                        rowTextView.setText("No Events!");
                    }


                    rLayout.addView(rowTextView);
                }else{
                    isPastDate=true;
                    Utils.ShowOKButtonDismissDialog(CalendarView.this,"Alert"," Can't plan for past date.");
                }

            }

            @Override
            public void onMonthChanged(Date date) {
                SimpleDateFormat df = new SimpleDateFormat("MM-yyyy");
               // Toast.makeText(CalendarView.this, df.format(date), Toast.LENGTH_SHORT).show();
            }
        });


        try {
            selectedDate = df.format(new Date());
            OrderedRealmCollection<Item> itemsList = realm.where(Item.class).equalTo("date", selectedDate).findAll();
            realMDateWiseDataAdapter = new RealMDateWiseDataAdapter(CalendarView.this, itemsList);
            // RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(CalendarView.this, 2);
//            recyclerViewInside.setLayoutManager(mLayoutManager);
//            recyclerViewInside.addItemDecoration(new GridSpacingItemDecoration(2, Utility.dpToPx(CalendarView.this, 10), true));
//            recyclerViewInside.setItemAnimator(new DefaultItemAnimator());
            recyclerViewInside.setAdapter(realMDateWiseDataAdapter);
            TextView rowTextView = new TextView(CalendarView.this);
            rowTextView.setPadding(10, 5, 10, 5);
            rowTextView.setTextColor(Color.BLACK);
            rLayout.removeAllViews();
            if (itemsList.size() > 0) {
                for (int i = 0; i < itemsList.size(); i++) {
                    // set some properties of rowTextView or something
                    if (itemsList.get(i).getEvent().trim().toString().length() > 0) {
                        rowTextView.setText("Event:" + itemsList.get(i).getEvent());
                    } else {
                        rowTextView.setText("No Events!");
                    }
                    // add the textview to the linearlayout
                }
            } else {
                rowTextView.setText("No Events!");
            }


            rLayout.addView(rowTextView);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //adding calendar day decorators
        List<DayDecorator> decorators = new ArrayList<>();
        decorators.add(new DisabledColorDecorator());
        calendarView.setDecorators(decorators);
        calendarView.refreshCalendar(currentCalendar);
        handler=new Handler();
        refreshCalendar();
          firstTimeMainActivity=sharedpreferences.getBoolean(AppConstants.CalenderView, true);
        if (firstTimeMainActivity) {
            //firsttime use
            showFirstimeTutorial();
        }
    }

//

    protected void showToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();

    }

    public void refreshCalendar() {
        TextView title = (TextView) findViewById(R.id.title);

        adapter.refreshDays();
        adapter.notifyDataSetChanged();
        handler.post(calendarUpdater); // generate some calendar items

//        title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
    }

    public Runnable calendarUpdater = new Runnable() {

        @Override
        public void run() {
            items.clear();

            // Print dates of the current week
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String itemvalue;
            event = Utility.readCalendarEvent(CalendarView.this);
            Log.d("=====Event====", event.toString());
            Log.d("=====Date ARRAY====", Utility.startDates.toString());

            for (int i = 0; i < Utility.startDates.size(); i++) {
                itemvalue = df.format(itemmonth.getTime());
                itemmonth.add(GregorianCalendar.DATE, 1);
                items.add(Utility.startDates.get(i).toString());
            }
            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }
    };


    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarView.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utility.checkPermission(CalendarView.this);

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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
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
            item.setDate(selectedGridDate);
            item.setUri(file.getPath());
            realm.copyToRealm(item);
            realm.commitTransaction();

            RealmResults<Item> itemses = realm.where(Item.class).findAll();
            System.out.print("");
        } catch (Exception e) {
            e.printStackTrace();
        }


        //    ivImage.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //  ivImage.setImageBitmap(bm);
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

    private int getGridRowsCount(int items) {
        int rows = items / 7;
        return rows;

    }
    private class DisabledColorDecorator implements DayDecorator {
        @Override
        public void decorate(DayView dayView) {
            if (CalendarUtils.isPastDay(dayView.getDate())) {
                int color = Color.parseColor("#a9afb9");
                dayView.setBackgroundColor(color);
            }
        }
    }
    public void showFirstimeTutorial(){
        Animation animation = new TranslateAnimation(0f, 0f, 200f, 0f);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        animation.setInterpolator(new BounceInterpolator());

        ToolTip toolTip = new ToolTip()
                .setTitle("Calender")
                .setDescription("Add your clothes to particular selected date in calender!")
                .setTextColor(Color.parseColor("#bdc3c7"))
                .setBackgroundColor(Color.parseColor("#e74c3c"))
                .setShadow(true)
                .setGravity(Gravity.TOP | Gravity.LEFT)
                .setEnterAnimation(animation);


        mTourGuideHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                .setPointer(new Pointer())
                .setToolTip(toolTip)
                .setOverlay(new Overlay())
                .playOn(btnDatePlan);
    }


}
