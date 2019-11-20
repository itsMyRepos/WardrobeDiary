package diary.wardrobe.com.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import diary.wardrobe.com.wardrobediary.AlarmReceiver;
import diary.wardrobe.com.wardrobediary.R;

public class AlarmActivity extends AppCompatActivity implements DatePicker.OnDateChangedListener, OnClickListener {

    DatePicker pickerDate;
    TimePicker pickerTime;
    Button buttonSetAlarm, setDateBtn, setTimeBtn, setReminderBtn;
    TextView info, date, time;
    AppCompatCheckBox oneday, twodays;
    final static int RQS_1 = 1;
    boolean isTimeSet, isDateSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alram_activity);

        info = (TextView) findViewById(R.id.info);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        pickerDate = (DatePicker) findViewById(R.id.pickerdate);
        pickerTime = (TimePicker) findViewById(R.id.pickertime);
        setDateBtn = (Button) findViewById(R.id.setdate);
        setTimeBtn = (Button) findViewById(R.id.settime);
        setReminderBtn = (Button) findViewById(R.id.setreminder);
        setDateBtn.setOnClickListener(this);
        setTimeBtn.setOnClickListener(this);
        setReminderBtn.setOnClickListener(this);
        //
        oneday = (AppCompatCheckBox) findViewById(R.id.oneday);
        twodays = (AppCompatCheckBox) findViewById(R.id.twoday);


        Calendar now = Calendar.getInstance();

        pickerDate.init(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH),
                null);

        pickerTime.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        pickerTime.setCurrentMinute(now.get(Calendar.MINUTE));
        time.setText("" + now.get(Calendar.HOUR_OF_DAY));

        buttonSetAlarm = (Button) findViewById(R.id.setalarm);
        buttonSetAlarm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                time.setText("" + pickerTime.getCurrentHour() + " :" + pickerTime.getCurrentMinute());
                date.setText("" + pickerDate.getDayOfMonth() + "-" + pickerDate.getMonth() + "-" + pickerDate.getYear());

                Calendar current = Calendar.getInstance();

                Calendar cal = Calendar.getInstance();
                cal.set(pickerDate.getYear(),
                        pickerDate.getMonth(),
                        pickerDate.getDayOfMonth(),
                        pickerTime.getCurrentHour(),
                        pickerTime.getCurrentMinute(),
                        00);

                if (cal.compareTo(current) <= 0) {
                    //The set Date/Time already passed
                    Toast.makeText(getApplicationContext(),
                            "Invalid Date/Time",
                            Toast.LENGTH_LONG).show();
                } else {
                    setAlarm(cal);
                }
                finish();
            }
        });
    }

    private void setAlarm(Calendar targetCal) {
//		time.setText("");
//
//		info.setText("\n\n***\n"
//				+ "Alarm is set@ " + targetCal.getTime() + "\n"
//				+ "***\n");

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);


    }

    public void SetReminders1Day2Day() {

        try {
            Calendar calendar = Calendar.getInstance();
            String date = getIntent().getStringExtra("DATE");
            SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            calendar.setTime(df.parse(date));// all done
            Calendar current = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, current.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, current.get(Calendar.MINUTE));

            if (oneday.isChecked()) {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                if (calendar.compareTo(current) <= 0) {
                    Toast.makeText(getApplicationContext(),
                            "Cant set reminder to past date",
                            Toast.LENGTH_LONG).show();
                    oneday.setChecked(false);
                }else{
                    Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }

            }

            if (twodays.isChecked()) {
                calendar.add(Calendar.DAY_OF_YEAR, -2);
                if (calendar.compareTo(current) <= 0) {
                    Toast.makeText(getApplicationContext(),
                            "Cant set reminder to past date",
                            Toast.LENGTH_LONG).show();
                    twodays.setChecked(false);
                }else{
                    Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setDate(View v) {
        pickerDate.setVisibility(View.VISIBLE);
        pickerTime.setVisibility(View.GONE);
    }

    public void setTime(View v) {
        pickerDate.setVisibility(View.GONE);
        pickerTime.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
        date.setText("" + i + "-" + i1 + "-" + i2);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setdate:
                Calendar now = Calendar.getInstance();

                DatePickerDialog dp = new DatePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        Calendar current = Calendar.getInstance();

                        Calendar cal = Calendar.getInstance();
                        cal.set(view.getYear(),
                                view.getMonth(),
                                view.getDayOfMonth()
                        );

                        if (cal.compareTo(current) < 0) {
                            //The set Date/Time already passed
                            Toast.makeText(getApplicationContext(),
                                    "Invalid Date!",
                                    Toast.LENGTH_LONG).show();
                            isDateSet = false;
                            enableSetRem();
                        } else {
                            pickerDate = view;
                            isDateSet = true;
                            enableSetRem();
                            setDateBtn.setText(dayOfMonth + "-" + monthOfYear + "-" + year);
                        }
                    }
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
                dp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dp.show();

                break;
            case R.id.settime:
                Calendar timecal = Calendar.getInstance();
                TimePickerDialog Tp = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        Calendar datetime = Calendar.getInstance();
                        Calendar c = Calendar.getInstance();
                        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        datetime.set(Calendar.MINUTE, minute);
                        if (datetime.getTimeInMillis() > c.getTimeInMillis()) {
//            it's after current
                            pickerTime = view;
                            setTimeBtn.setText(hourOfDay + ":" + minute);
                            isTimeSet = true;
                            enableSetRem();
                        } else {
                            isTimeSet = false;
                            enableSetRem();
//            it's before current'
                            //The set Date/Time already passed
                            Toast.makeText(getApplicationContext(),
                                    "Invalid Time!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, timecal.get(Calendar.HOUR_OF_DAY), timecal.get(Calendar.MINUTE), false);
                Tp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Tp.show();
                break;
            case R.id.setreminder:
                Calendar current = Calendar.getInstance();

                Calendar cal = Calendar.getInstance();
                cal.set(pickerDate.getYear(),
                        pickerDate.getMonth(),
                        pickerDate.getDayOfMonth(),
                        pickerTime.getCurrentHour(),
                        pickerTime.getCurrentMinute(),
                        00);

                if (cal.compareTo(current) <= 0) {
                    //The set Date/Time already passed
                    Toast.makeText(getApplicationContext(),
                            "Invalid Date/Time",
                            Toast.LENGTH_LONG).show();
                } else {
                    setAlarm(cal);
                    SetReminders1Day2Day();
                }
                break;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);

    }

    private void enableSetRem() {
        if (isDateSet && isTimeSet) {
            setReminderBtn.setEnabled(true);
            setReminderBtn.setBackgroundColor(ContextCompat.getColor(AlarmActivity.this, R.color.colorPrimary));
        } else {
            setReminderBtn.setEnabled(false);
            setReminderBtn.setBackgroundColor(ContextCompat.getColor(AlarmActivity.this, R.color.gray));
        }
    }


    @Override
    protected void onDestroy() {
        SetReminders1Day2Day();
        super.onDestroy();
    }
}
