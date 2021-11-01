package edu.wm.cs.cs425.postserviceapp.staff;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.fragment.NavHostFragment;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

import edu.wm.cs.cs425.postserviceapp.DAO.Package;
import edu.wm.cs.cs425.postserviceapp.R;
import edu.wm.cs.cs425.postserviceapp.Retrofit.MyService;
import edu.wm.cs.cs425.postserviceapp.Retrofit.RetrofitClient;
import edu.wm.cs.cs425.postserviceapp.StaffActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ShowPackageDetailFragment extends Fragment {


    //objects for backend service
    private MyService myService= RetrofitClient.getInstance().create(MyService.class);
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private String ID;//schedule ID
    private String[] idList;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ShowPackageDetailFragment() {
        // Required empty public constructor
    }

    public static ShowScheduleFragment newInstance(String param1, String param2) {
        ShowScheduleFragment fragment = new ShowScheduleFragment();
        Bundle args = new Bundle();
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root=inflater.inflate(R.layout.fragment_package_detail, container, false);
        TableLayout table=root.findViewById(R.id.schedule_detail_table);
        TextView title=root.findViewById(R.id.textView4);
        Button completeButton=root.findViewById(R.id.complete);
        //back to schedule page if onBackPressed()
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(ShowPackageDetailFragment.this)
                        .navigate(R.id.action_showPackageDetailFragment_back_to_showScheduleFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
        //get data from bundle
        ID=getArguments().getString("ID");
        String Name=getArguments().getString("Name");
        String CSU=getArguments().getString("CSU");
        String packageIDs = getArguments().getString("PackageID");
        //inflate title
        title.setText(Name+", CSU #"+CSU);
        //inflate table
        idList = packageIDs.split("-");
        for (String id: idList){
            Call<List<Package>> call = myService.getPackageWithId(id);
            call.enqueue(new Callback<List<Package>>() {
                @Override
                public void onResponse(Call<List<Package>> info, Response<List<Package>> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }

                    for (Package pkg: response.body()){
                        String pkgId = pkg.getPackageId();
                        String location = pkg.getPackageLocation();
                        String size = pkg.getPackageSize();

                        TableRow row = (TableRow) inflater.inflate(R.layout.schedule_detail_row,null);
                        TextView IdView = (TextView) row.findViewById(R.id.packageId_text);
                        TextView locationView= (TextView) row.findViewById(R.id.location_text);
                        TextView sizeView=(TextView) row.findViewById(R.id.size_text);

                        IdView.setText(pkgId);
                        locationView.setText(location);
                        sizeView.setText(size);

                        table.addView(row);
                    }
                }
                @Override
                public void onFailure(Call<List<Package>> call, Throwable t) {
                    System.out.println("Error");
                }
            });
        }
        completeButton.setOnClickListener(completeListener);

        return root;
    }

    private View.OnClickListener completeListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //1. fulfill the schedule request by ID
            if(ID.length()==0){
                Log.e("invalid input","ID cannot be empty");
                return;
            }
            fulfillRequestByID(ID);
            //2. complete packages with the scheduleID
            if(idList.length==0){
                Log.e("invalid input","package ID cannot be empty");
                return;
            }
            for(String id: idList){
                completePackageByID(id);
            }
            //3. go back to showScheduleFragment
            NavHostFragment.findNavController(ShowPackageDetailFragment.this)
                    .navigate(R.id.action_showPackageDetailFragment_back_to_showScheduleFragment);
        }
    };
    //retrofit fulfill schedule request by id
    private void fulfillRequestByID(String ID){
        compositeDisposable.add(myService.fulfillRequest(ID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<String>() {
                @Override
                public void accept(String s) throws Exception {
                    Log.i("success", "request is fulfilled");
                }
            }));
    }
    //retrofit update package status 'complete' to yes
    private void completePackageByID(String id){
        compositeDisposable.add(myService.completePackageByID(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.i("success", "package is prepared");
                    }
                }));
    }


}
