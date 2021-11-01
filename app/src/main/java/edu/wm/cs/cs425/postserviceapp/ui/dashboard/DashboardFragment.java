package edu.wm.cs.cs425.postserviceapp.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

import edu.wm.cs.cs425.postserviceapp.DAO.User;
import edu.wm.cs.cs425.postserviceapp.LoginActivity;
import edu.wm.cs.cs425.postserviceapp.MainActivity;
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

public class DashboardFragment extends Fragment {

    Button scan_in_package;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    MyService myService;

    private DashboardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //back to home page if onBackPressed()
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(DashboardFragment.this)
                        .navigate(R.id.action_dashboardFragment_to_staffHomeFragment);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);

        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);


        Retrofit retrofitClient = RetrofitClient.getInstance();
        myService = retrofitClient.create(MyService.class);

        scan_in_package = (Button) root.findViewById(R.id.scan);
        scan_in_package.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MaterialEditText edt_csu = (MaterialEditText) root.findViewById(R.id.edt_csu);
                MaterialEditText edt_package_id = (MaterialEditText) root.findViewById(R.id.edt_package_id);
                MaterialEditText edt_package_location = (MaterialEditText) root.findViewById(R.id.edt_package_location);
                MaterialEditText edt_package_size = (MaterialEditText) root.findViewById(R.id.edt_package_size);

                String CSU=edt_csu.getText().toString();
                if(TextUtils.isEmpty(CSU)){
                    Toast.makeText(getActivity(),"CSU cannot be null or empty", Toast.LENGTH_SHORT).show();
                    return;
                }else if(CSU.length()!=4){
                    Toast.makeText(getActivity(),"CSU should be 4 digits", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edt_package_id.getText().toString())){
                    Toast.makeText(getActivity(),"Package ID cannot be null or empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edt_package_location.getText().toString())){
                    Toast.makeText(getActivity(),"Package location cannot be null or empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edt_package_size.getText().toString())){
                    Toast.makeText(getActivity(),"Package size cannot be null or empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                //get name and email of the user by CSU and create package record
                MyService myService= RetrofitClient.getInstance().create(MyService.class);
                Call<List<User>> call = myService.getUserByCSU(CSU);
                call.enqueue(new Callback<List<User>>() {
                    @Override
                    public void onResponse(Call<List<User>> info, Response<List<User>> response) {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        List<User> users=response.body();
                        if(users.size()>0){
                            scan_inPackage(users.get(0).getEmail(),users.get(0).getName(),edt_package_id.getText().toString(),
                                    edt_package_location.getText().toString(),edt_package_size.getText().toString(),CSU);
                            edt_csu.setText("");
                            edt_package_id.setText("");
                            edt_package_location.setText("");
                            edt_package_size.setText("");
                        }else{
                            Log.e("invalid input", "invalid CSU");
                        }
                    }
                    @Override
                    public void onFailure(Call<List<User>> call, Throwable t) {
                        System.out.println("Error");
                    }
                });



            }
        });


        return root;
    }

    private void scan_inPackage(String email, String name, String package_id, String package_location, String package_size, String csu) {

        compositeDisposable.add(myService.scan_inPackage(email,name,package_id,package_location,package_size,csu)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(getActivity(), ""+s, Toast.LENGTH_SHORT).show();
                    }

                }));
    }
}