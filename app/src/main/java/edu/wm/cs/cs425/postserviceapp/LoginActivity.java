package edu.wm.cs.cs425.postserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

import edu.wm.cs.cs425.postserviceapp.Retrofit.MyService;
import edu.wm.cs.cs425.postserviceapp.Retrofit.RetrofitClient;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {


    TextView test;
    String studentEmail;

    TextView txt_create_account;
    TextView txt_staff;

    MaterialEditText edt_login_email, edt_login_password;
    Button btn_login;
    Intent intent;
    String emailAddress;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    MyService myService;



    @Override
    protected void onStop(){
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        Retrofit retrofitClient = RetrofitClient.getInstance();
        myService = retrofitClient.create(MyService.class);

        edt_login_email = findViewById(R.id.edt_email);
        edt_login_password = findViewById(R.id.edt_password);

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(edt_login_email.getText().toString(),edt_login_password.getText().toString());
            }
        });

        txt_create_account = findViewById(R.id.txt_create_account);
        txt_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View register_layout = LayoutInflater.from(LoginActivity.this)
                        .inflate(R.layout.register_layout,null);

                new MaterialStyledDialog.Builder(LoginActivity.this)
                        .setIcon(R.drawable.ic_user)
                        .setTitle("REGISTRATION")
                        .setCustomView(register_layout)
                        .setNegativeText("CANCEL")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("REGISTER")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                MaterialEditText edt_register_email = (MaterialEditText) register_layout.findViewById(R.id.edt_email);
                                MaterialEditText edt_register_name = (MaterialEditText) register_layout.findViewById(R.id.edt_name);
                                MaterialEditText edt_register_csu = (MaterialEditText) register_layout.findViewById(R.id.edt_csu);
                                MaterialEditText edt_register_password = (MaterialEditText) register_layout.findViewById(R.id.edt_password);


                                if(TextUtils.isEmpty(edt_register_email.getText().toString())){
                                    Toast.makeText(LoginActivity.this,"Email cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if(TextUtils.isEmpty(edt_register_name.getText().toString())){
                                    Toast.makeText(LoginActivity.this,"Name cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if(TextUtils.isEmpty(edt_register_csu.getText().toString())){
                                    Toast.makeText(LoginActivity.this,"CSU cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if(TextUtils.isEmpty(edt_register_password.getText().toString())){
                                    Toast.makeText(LoginActivity.this,"Password cannot be null or empty", Toast.LENGTH_SHORT).show();
                                    return;
                                }


                                registerUser(edt_register_email.getText().toString(),edt_register_name.getText().toString(),edt_register_password.getText().toString(),edt_register_csu.getText().toString());



                            }
                        }).show();
            }
        });

        txt_staff = findViewById(R.id.txt_staffportal);
        txt_staff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View staff_layout = LayoutInflater.from(LoginActivity.this)
                        .inflate(R.layout.fragment_staff_home, null);

                intent = new Intent(LoginActivity.this, StaffActivity.class);

                intent.putExtra("emailAddress",emailAddress);
                startActivity(intent);
                finish();

            }});



            //test mongodb data

//        test = findViewById(R.id.test);
//        studentEmail = "test1@email.wm.edu";
//        Call<List<Package>> call = myService.getPackage();
//        call.enqueue(new Callback<List<Package>>() {
//            @Override
//            public void onResponse(Call<List<Package>> call, Response<List<Package>> response) {
//                if(!response.isSuccessful()){
//                    test.setText("404");
//                    return;
//                }
//
//                List<Package> packages = response.body();
//                for (Package pkg : packages){
//                    if(pkg.getStudentEmail().equalsIgnoreCase(studentEmail)){
//                        String content = "";
//                        content += "Student Email: " + pkg.getStudentEmail();
//                        content += "Package location: " + pkg.getPackageLocation() + "\n";
//                        test.append(content);
//                    }
//
//                }
//            }
//
//
//            @Override
//            public void onFailure(Call<List<Package>> call, Throwable t) {
//                test.setText(t.getMessage());
//            }
//        });



    }

    private void registerUser(String email, String name, String password, String csu) {

        compositeDisposable.add(myService.registerUser(email,name,password,csu)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Toast.makeText(LoginActivity.this, s, Toast.LENGTH_SHORT).show();
                    }

                }));

    }

    private void loginUser(String email, String password) {


        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Email cannot be null or empty", Toast.LENGTH_SHORT).show();
            return;
        }else{
            emailAddress = email;
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Password cannot be null or empty", Toast.LENGTH_SHORT).show();
            return;
        }

        compositeDisposable.add(myService.loginUser(email,password)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Toast.makeText(LoginActivity.this, s, Toast.LENGTH_SHORT).show();
                // check if anything went wrong
                //TODO: This is a temporary fix, please change this!
                if (!s.equals("\"Login Success!\""))
                    return;
                intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("emailAddress",emailAddress);
                startActivity(intent);
            }


        }));


    }



}
