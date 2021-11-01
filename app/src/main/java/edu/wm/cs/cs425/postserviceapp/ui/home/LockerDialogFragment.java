package edu.wm.cs.cs425.postserviceapp.ui.home;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import edu.wm.cs.cs425.postserviceapp.ConfirmationActivity;
import edu.wm.cs.cs425.postserviceapp.MyServiceImp;
import edu.wm.cs.cs425.postserviceapp.R;
import edu.wm.cs.cs425.postserviceapp.Retrofit.MyService;
import edu.wm.cs.cs425.postserviceapp.Retrofit.RetrofitClient;
import edu.wm.cs.cs425.postserviceapp.ScheduleActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

/**
 * Fragment class of locker dialog
 * it allows users to select date of a locker pickup
 * alert user if locker is full
 * navigate to confirmation page if locker is not full
 * @author Jenny Sun
 */
public class LockerDialogFragment extends DialogFragment {
    //attributes to make a locker pickup request
    private MyServiceImp myServiceImp=new MyServiceImp();
    private String email;
    private String packageIDs;

    //max capacity of the locker
    final static int MAX_CAPACITY=5;

    //view objects
    private TextView calendar;
    private TextView alert;
    private Button yes;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "email";
    private static final String ARG_PARAM2 = "packageIDs";

    public static LockerDialogFragment newInstance(String param1, String param2) {
        LockerDialogFragment fragment = new LockerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public LockerDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {//get data from HomeFragment
            email = getArguments().getString(ARG_PARAM1);
            packageIDs = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_locker_dialog, container, false);
        calendar=root.findViewById(R.id.datePicker2);
        yes=root.findViewById(R.id.yes);
        alert=root.findViewById(R.id.textView2);
        alert.setVisibility(View.INVISIBLE);

        //DUPLICATE CODE as in ScheduleActivity, may refactor later
        //1. user can select a date
        Calendar calInstance=Calendar.getInstance();
        final int year = calInstance.get(YEAR);
        final int month = calInstance.get(MONTH);
        final int day = calInstance.get(DAY_OF_MONTH);
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpk = new DatePickerDialog(getActivity(),new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
                        String date = month + "/" + day + "/" + year;
                        calendar.setText(date);
                        //2. check if the locker is full at a given date
                        countLockerScheduleByDate(calendar.getText().toString());
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

        return root;
    }

    private View.OnClickListener lockerListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //container for parameters
            String[] newSchedule=new String[5];
            //1. schedule id
            newSchedule[0]= UUID.randomUUID().toString();
            //2. date
            newSchedule[1]=calendar.getText().toString();
            if(newSchedule[1].equals("Select Date")){
                Log.e("Input Err in LockerDialogFragment","date not selected");
                return;
            }
            //3. time
            newSchedule[2]="7am - 8pm";
            //4. email
            newSchedule[3]=email;
            //5. packageIDs
            newSchedule[4]=packageIDs;
            //update package schedule_status
            for(String pkgId: packageIDs.split("-")){
                myServiceImp.package_schedule(email,pkgId);
            }
            //create locker schedule
            myServiceImp.create_schedule(newSchedule,false);
            Intent intent=new Intent(getContext(), ConfirmationActivity.class);
            intent.putExtra("email",email);
            intent.putExtra("packageIDs",packageIDs);
            startActivity(intent);
        }
    };

    private View.OnClickListener walkInListener= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent(getContext(), ScheduleActivity.class);
            intent.putExtra("packageIDs",packageIDs);
            intent.putExtra("email",email);
            startActivity(intent);
        }
    };

    // if locker is full at a given date, alert the user to pick another date or choose walk-in appointments
    private void countLockerScheduleByDate(String date){
        MyService myService= RetrofitClient.getInstance().create(MyService.class);
        Call<Integer> call = myService.countLockerByDate(date);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> info, Response<Integer> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                Integer count=response.body();
                if(count>=MAX_CAPACITY){//3. if full
                    //set button text yes -> walk-in, set walkin listener
                    yes.setText("WALK-IN");
                    yes.setOnClickListener(walkInListener);
                    //show alert message
                    alert.setVisibility(View.VISIBLE);
                }else{//4.if not full
                    //keep the alert invisible
                    alert.setVisibility(View.INVISIBLE);
                    //set text 'yes' to 'walk-in', set locker listener
                    yes.setText("YES");
                    yes.setOnClickListener(lockerListener);
                }
            }
            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                System.out.println("Error");
            }
        });
    }
}