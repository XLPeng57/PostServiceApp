package edu.wm.cs.cs425.postserviceapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.wm.cs.cs425.postserviceapp.ConfirmationActivity;
import edu.wm.cs.cs425.postserviceapp.DAO.User;
import edu.wm.cs.cs425.postserviceapp.R;
import edu.wm.cs.cs425.postserviceapp.Retrofit.MyService;
import edu.wm.cs.cs425.postserviceapp.Retrofit.RetrofitClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

public class ScheduleActivity extends AppCompatActivity {

    //view objects
    private TextView cal;
    private RadioGroup radioGroup;
    private Button button;

    //fixed attributes of time estimate color
    final static int GREEN = 0;
    final static int YELLOW = 1;
    final static int RED = 2;

    MyService myService;

    private MyServiceImp myServiceImp=new MyServiceImp();

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Retrofit retrofitClient = RetrofitClient.getInstance();
        myService = retrofitClient.create(MyService.class);

        //initialize views
        radioGroup=findViewById(R.id.radioGroup);
        button = findViewById(R.id.qrbutton);
        //get strings from intent
        Intent intent = getIntent();
        String packageIDs = intent.getStringExtra("packageIDs");
        String studentEmail = intent.getStringExtra("email");

        //initialize container for schedule params
        String[] newSchedule=new String[5];

        //date picker
        cal = findViewById(R.id.datePicker1);
        Calendar calendar = getInstance();
        final int year = calendar.get(YEAR);
        final int month = calendar.get(MONTH);
        final int day = calendar.get(DAY_OF_MONTH);
        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dpk = new DatePickerDialog(ScheduleActivity.this,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
                        String date = month + "/" + day + "/" + year;
                        cal.setText(date);
                        newSchedule[1]= date;
                        showTimeEstimateByColor(date);//show time estimate for selected date
                    }
                }, year, month, day);
                DatePicker dp=dpk.getDatePicker();
                //restrict date to Today~5 days ahead
                dp.setMinDate(new Date().getTime());
                Calendar maxdate=Calendar.getInstance();
                maxdate.setTime(new Date());
                maxdate.set(DATE,maxdate.get(Calendar.DATE) + 5);
                dp.setMaxDate(maxdate.getTime().getTime());
                dpk.show();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //information needed to create a schedule
                //1.schedule ID
                newSchedule[0]= UUID.randomUUID().toString();

                RadioButton checkedButton=findViewById(radioGroup.getCheckedRadioButtonId());
                //check if a time window has been selected
                if (checkedButton == null) {
                    Toast.makeText(getApplicationContext(), "Must select a time", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newSchedule[1]==null) {
                    Toast.makeText(getApplicationContext(), "Must select a date", Toast.LENGTH_SHORT).show();
                    return;
                }
                newSchedule[2]=checkedButton.getText().toString().trim();
                newSchedule[3]=studentEmail;
                newSchedule[4]=packageIDs;
                for(String pkgId: packageIDs.split("-")){
                    myServiceImp.package_schedule(studentEmail,pkgId);
                }
                //create a new walk-in schedule!
                myServiceImp.create_schedule(newSchedule,true);
                //go to confirmation page
                Intent toConfirmation = new Intent(ScheduleActivity.this, ConfirmationActivity.class);
                toConfirmation.putExtra("packageIDs",packageIDs);
                toConfirmation.putExtra("email",studentEmail);
                startActivity(toConfirmation);
//                finish();
            }
        });
    }

    /**
     * private method to show time estimate by color
     * green: <10min, yellow: <=20min, red: >20min
     * @author: Jenny Sun
     * @param date
     */
    private void showTimeEstimateByColor(String date){
        for(int i=0;i<radioGroup.getChildCount();++i){
            RadioButton radioButton=(RadioButton) radioGroup.getChildAt(i);
            setButtonColor(radioButton,date);
        }
    }
    //private method to set color of individual button text
    private void setButtonColor(RadioButton radioButton, String date){
        String time=radioButton.getText().toString().trim();
        Call<Integer> call = myService.countScheduleByTime(date,time);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> info, Response<Integer> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                Integer count=response.body();
                if(count>=RED){
                    radioButton.setTextColor(Color.parseColor("#ffcc0000"));
                }else if(count<=YELLOW && count>GREEN){
                    radioButton.setTextColor(Color.parseColor("#daa520"));
                }else{
                    radioButton.setTextColor(Color.parseColor("#006400"));
                }
            }
            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                System.out.println("Error");
            }
        });
    }


//    //author: Kaichen Zhang
//    public static class RangeTimePickerDialog extends TimePickerDialog {
//
//        private int minHour = -1;
//        private int minMinute = -1;
//
//        private int maxHour = 25;
//        private int maxMinute = 25;
//
//        private int currentHour = 0;
//        private int currentMinute = 0;
//
//        private Calendar calendar = Calendar.getInstance();
//        private DateFormat dateFormat;
//
//
//        public RangeTimePickerDialog(Context context, int dialogTheme, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
//            super(context, callBack, hourOfDay, minute, is24HourView);
//            currentHour = hourOfDay;
//            currentMinute = minute;
//            dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
//            fixSpinner(context, hourOfDay, minute, is24HourView);
//
//            try {
//                Class<?> superclass = getClass().getSuperclass();
//                Field mTimePickerField = superclass.getDeclaredField("mTimePicker");
//                mTimePickerField.setAccessible(true);
//                TimePicker mTimePicker = (TimePicker) mTimePickerField.get(this);
//                mTimePicker.setOnTimeChangedListener(this);
//            } catch (NoSuchFieldException e) {
//            } catch (IllegalArgumentException e) {
//            } catch (IllegalAccessException e) {
//            }
//        }
//
//        public void setMin(int hour, int minute) {
//            minHour = hour;
//            minMinute = minute;
//        }
//
//        public void setMax(int hour, int minute) {
//            maxHour = hour;
//            maxMinute = minute;
//        }
//
//        @Override
//        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//
//            boolean validTime = true;
//            if (hourOfDay < minHour || (hourOfDay == minHour && minute < minMinute)){
//                validTime = false;
//            }
//
//            if (hourOfDay  > maxHour || (hourOfDay == maxHour && minute > maxMinute)){
//                validTime = false;
//            }
//
//            if (validTime) {
//                currentHour = hourOfDay;
//                currentMinute = minute;
//            }
//
//            updateTime(currentHour, currentMinute);
//            updateDialogTitle(view, currentHour, currentMinute);
//        }
//
//        private void updateDialogTitle(TimePicker timePicker, int hourOfDay, int minute) {
//            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//            calendar.set(Calendar.MINUTE, minute);
//            String title = dateFormat.format(calendar.getTime());
//            setTitle(title);
//        }
//
//
//        private void fixSpinner(Context context, int hourOfDay, int minute, boolean is24HourView) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // android:timePickerMode spinner and clock began in Lollipop
//                try {
//                    // Get the theme's android:timePickerMode
//                    //two modes are available clock mode and spinner mode ... selecting spinner mode for latest versions
//                    final int MODE_SPINNER = 2;
//                    Class<?> styleableClass = Class.forName("com.android.internal.R$styleable");
//                    Field timePickerStyleableField = styleableClass.getField("TimePicker");
//                    int[] timePickerStyleable = (int[]) timePickerStyleableField.get(null);
//                    final TypedArray a = context.obtainStyledAttributes(null, timePickerStyleable, android.R.attr.timePickerStyle, 0);
//                    Field timePickerModeStyleableField = styleableClass.getField("TimePicker_timePickerMode");
//                    int timePickerModeStyleable = timePickerModeStyleableField.getInt(null);
//                    final int mode = a.getInt(timePickerModeStyleable, MODE_SPINNER);
//                    a.recycle();
//                    if (mode == MODE_SPINNER) {
//                        TimePicker timePicker = (TimePicker) findField(TimePickerDialog.class, TimePicker.class, "mTimePicker").get(this);
//                        Class<?> delegateClass = Class.forName("android.widget.TimePicker$TimePickerDelegate");
//                        Field delegateField = findField(TimePicker.class, delegateClass, "mDelegate");
//                        Object delegate = delegateField.get(timePicker);
//                        Class<?> spinnerDelegateClass;
//                        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.LOLLIPOP) {
//                            spinnerDelegateClass = Class.forName("android.widget.TimePickerSpinnerDelegate");
//                        } else {
//
//                            spinnerDelegateClass = Class.forName("android.widget.TimePickerClockDelegate");
//                        }
//                        if (delegate.getClass() != spinnerDelegateClass) {
//                            delegateField.set(timePicker, null); // throw out the TimePickerClockDelegate!
//                            timePicker.removeAllViews(); // remove the TimePickerClockDelegate views
//                            Constructor spinnerDelegateConstructor = spinnerDelegateClass.getConstructor(TimePicker.class, Context.class, AttributeSet.class, int.class, int.class);
//                            spinnerDelegateConstructor.setAccessible(true);
//                            // Instantiate a TimePickerSpinnerDelegate
//                            delegate = spinnerDelegateConstructor.newInstance(timePicker, context, null, android.R.attr.timePickerStyle, 0);
//                            delegateField.set(timePicker, delegate); // set the TimePicker.mDelegate to the spinner delegate
//                            // Set up the TimePicker again, with the TimePickerSpinnerDelegate
//                            timePicker.setIs24HourView(is24HourView);
//                            timePicker.setCurrentHour(hourOfDay);
//                            timePicker.setCurrentMinute(minute);
//                            timePicker.setOnTimeChangedListener(this);
//                        }
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//        public static Field findField(Class objectClass, Class fieldClass, String expectedName) {
//            try {
//                Field field = objectClass.getDeclaredField(expectedName);
//                field.setAccessible(true);
//                return field;
//            } catch (NoSuchFieldException e) {} // ignore
//            // search for it if it wasn't found under the expected ivar name
//            for (Field searchField : objectClass.getDeclaredFields()) {
//                if (searchField.getType() == fieldClass) {
//                    searchField.setAccessible(true);
//                    return searchField;
//                }
//            }
//            return null;
//        }
//    }

}
