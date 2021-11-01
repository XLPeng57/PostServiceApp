package edu.wm.cs.cs425.postserviceapp.ui.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.wm.cs.cs425.postserviceapp.ConfirmationActivity;
import edu.wm.cs.cs425.postserviceapp.DAO.Package;
import edu.wm.cs.cs425.postserviceapp.R;
import edu.wm.cs.cs425.postserviceapp.Retrofit.MyService;
import edu.wm.cs.cs425.postserviceapp.Retrofit.RetrofitClient;
import edu.wm.cs.cs425.postserviceapp.DAO.User;
import edu.wm.cs.cs425.postserviceapp.ScheduleActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static java.util.Calendar.*;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private String studentEmail;
    private Button b1;
    private TableLayout table;
    TableRow row;
    HashMap<String, Integer> checkList;
    MyService myService;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        Retrofit retrofitClient = RetrofitClient.getInstance();
        myService = retrofitClient.create(MyService.class);


        studentEmail = getActivity().getIntent().getStringExtra("emailAddress");
        //a listener that navigates to schedule activity for walk-in appointments
        View.OnClickListener walkInListener=new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String packageIDs=getCheckedPackageIds();
                //don't create a schedule if no packages are selected
                if (packageIDs.length() == 0){
                    Toast.makeText(getContext(), "Must select at least one package", Toast.LENGTH_SHORT).show();
                    return;
                }
                //do not pass name and csu unless required by view objects
                Intent intent = new Intent(getActivity(), ScheduleActivity.class);
                intent.putExtra("email",studentEmail);
                intent.putExtra("packageIDs",packageIDs);
                startActivity(intent);
            }
        };

        b1 = (Button) root.findViewById(R.id.walkinbutton);
        b1.setOnClickListener(walkInListener);
        // set up a locker pick-up schedule request
        Button lockerButton=root.findViewById(R.id.lockerbutton);
        lockerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pop up locker pickup dialog
                Bundle args = new Bundle();
                String packageIDs=getCheckedPackageIds();
                if(packageIDs.length()==0){
                    Toast.makeText(getContext(), "Must select at least one package", Toast.LENGTH_SHORT).show();
                    return;
                }
                args.putString("packageIDs", packageIDs);
                args.putString("email",studentEmail);
                DialogFragment lockerDialogFragment = new LockerDialogFragment();
                lockerDialogFragment.setArguments(args);
                lockerDialogFragment.show(getFragmentManager(),"locker dialog");
            }
        });


        table = (TableLayout) root.findViewById(R.id.table);

        checkList = new HashMap<String, Integer>();

        TextView menuText1 = root.findViewById(R.id.text1);
        menuText1.setText(R.string.menu_text_negative_1);
        TextView menuText2 = root.findViewById(R.id.text2);
        menuText2.setText(R.string.menu_text_negative_2);

        Call<List<Package>> call = myService.getPackage(studentEmail);
        call.enqueue(new Callback<List<Package>>() {
            @Override
            public void onResponse(Call<List<Package>> call, Response<List<Package>> response) {
                if(!response.isSuccessful()){
                    return;
                }

                List<Package> packages = response.body();
                for (Package pkg : packages){

                    String packageId = pkg.getPackageId();
                    String size = pkg.getPackageSize();
                    row = (TableRow) inflater.inflate(R.layout.rows,null);
                    TextView pkgID = (TextView) row.findViewById(R.id.pkg);
                    pkgID.setText("Package " + packageId);
                    TextView pkgSize = (TextView) row.findViewById(R.id.size);
                    pkgSize.setText(size);
                    table.addView(row);

                    //change the menu text to reflect that packages have arrived
                    menuText1.setText(R.string.menu_text_positive_1);
                    menuText2.setText(R.string.menu_text_positive_2);

                    CheckBox checkBox = row.findViewById(R.id.check);
                    //student cannot select scheduled packages
                    if(pkg.getSchedule().equals("yes")){
                        checkBox.setChecked(true);
                        checkBox.setEnabled(false);
                    }else{//if the package hasn't been scheduleds
                        checkBox.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view) {
                                if (checkBox.isChecked()){
                                    checkList.put(packageId,1);
                                }else{
                                    checkList.put(packageId,0);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Package>> call, Throwable t) {
                System.out.println("Error");
            }
        });


        return root;
    }
    // get packageIds of checked packages
    private String getCheckedPackageIds(){
        //store the package IDs of a new schedule
        StringBuilder sb=new StringBuilder();

        for (Map.Entry mapElement : checkList.entrySet()) {
            if ((Integer)mapElement.getValue()==1){
                //package IDs are separated by -
                sb.append(mapElement.getKey()+"-");
            }
        }
        return sb.toString();
    }




}