package edu.wm.cs.cs425.postserviceapp.staff;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import edu.wm.cs.cs425.postserviceapp.DAO.Package;
import edu.wm.cs.cs425.postserviceapp.DAO.Schedule;
import edu.wm.cs.cs425.postserviceapp.DAO.User;
import edu.wm.cs.cs425.postserviceapp.LoginActivity;
import edu.wm.cs.cs425.postserviceapp.R;
import edu.wm.cs.cs425.postserviceapp.Retrofit.MyService;
import edu.wm.cs.cs425.postserviceapp.Retrofit.RetrofitClient;
import edu.wm.cs.cs425.postserviceapp.ui.home.LockerDialogFragment;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ShowScheduleFragment extends Fragment {

    //objects for backend service
    private MyService myService=RetrofitClient.getInstance().create(MyService.class);

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private static Bundle args;

    public ShowScheduleFragment() {
        // Required empty public constructor
    }

    public static ShowScheduleFragment newInstance(String param1, String param2) {
        ShowScheduleFragment fragment = new ShowScheduleFragment();
        args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    //reload page when onBackPressed
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root=inflater.inflate(R.layout.fragment_show_schedule, container, false);
        TableLayout table=root.findViewById(R.id.schedule_table);
        //back to home page if onBackPressed()
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(ShowScheduleFragment.this)
                        .navigate(R.id.action_showScheduleFragment_to_staffHomeFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);

        Call<List<Schedule>> call = myService.getAllSchedules();

        call.enqueue(new Callback<List<Schedule>>() {
            @Override
            public void onResponse(Call<List<Schedule>> call, Response<List<Schedule>> response) {
                if(!response.isSuccessful()){
                    return;
                }

                List<Schedule> schedules = response.body();

                //schedules are sorted here
                schedules.sort(new Schedule());

                for (Schedule schedule : schedules){
                    //info that are needed for inflating views and bundle
                    String id = schedule.getId();
                    String date = schedule.getDate();
                    String time = schedule.getTime();
                    String email = schedule.getEmail();
                    String packageIds = schedule.getPackageIDs();

                    TableRow row = (TableRow) inflater.inflate(R.layout.schedule_row,null);
                    TextView dateView = (TextView) row.findViewById(R.id.date_text);
                    TextView timeView= (TextView) row.findViewById(R.id.time_text);
                    TextView nameView=(TextView) row.findViewById(R.id.name_text);
                    TextView csuView = (TextView) row.findViewById(R.id.csu_text);

                    dateView.setText(date);
                    timeView.setText(time);
                    setUserInfoView(email, nameView, csuView);


                    //navigate to another fragment upon click on a single row
                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Bundle bundle = new Bundle();
                            bundle.putString("ID",id);
                            bundle.putString("Name",nameView.getText().toString());
                            bundle.putString("CSU",csuView.getText().toString());
                            bundle.putString("PackageID",packageIds);
                            NavHostFragment.findNavController(ShowScheduleFragment.this)
                                    .navigate(R.id.action_showScheduleFragment_to_showPackageDetailFragment,bundle);

                        }
                    });
                    table.addView(row);
                }
            }
            @Override
            public void onFailure(Call<List<Schedule>> call, Throwable t) {
                System.out.println("Error");
            }
        });


        Button home = (Button)root.findViewById(R.id.staff_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Back to staff home
                NavHostFragment.findNavController(ShowScheduleFragment.this)
                        .navigate(R.id.action_showScheduleFragment_to_staffHomeFragment);
            }
        });

        return root;
    }

    /**
     * store User.name and User.CSU returned by the callback to view objects
     * @author Jenny Sun
     * @param email
     * @param nameView
     * @param csuView
     */
    private void setUserInfoView(String email, TextView nameView, TextView csuView){
        Call<List<User>> info = myService.getUser(email);
        info.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> info, Response<List<User>> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                List<User> users = response.body();
                if(users.size()>0){
                    User temp=users.get(0);
                    nameView.setText(temp.getName());
                    csuView.setText(temp.getCsu());
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                System.out.println("Error");
            }
        });
    }



}
