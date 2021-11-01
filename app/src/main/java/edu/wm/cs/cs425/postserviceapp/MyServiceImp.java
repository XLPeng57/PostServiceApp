package edu.wm.cs.cs425.postserviceapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.wm.cs.cs425.postserviceapp.DAO.User;
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

/**
 * a public class that provides functions used in more than one places
 *
 */
public class MyServiceImp {
    private MyService myService;
    private CompositeDisposable compositeDisposable;

    public MyServiceImp(){
        Retrofit retrofitClient = RetrofitClient.getInstance();
        myService = retrofitClient.create(MyService.class);
        compositeDisposable = new CompositeDisposable();
    }

    //create a new schedule given user inputs
    public void create_schedule(String[] newSchedule, Boolean walkIn){
        if(newSchedule.length!=5){
            return;
        }
        compositeDisposable.add(myService.create_schedule(newSchedule[0],newSchedule[1],newSchedule[2],newSchedule[3],newSchedule[4],walkIn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        System.out.println(s);
                    }
                }));
    }

    public void package_schedule(String studentEmail, String package_id) {

        compositeDisposable.add(myService.package_schedule(studentEmail,package_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.i("schedule", "scheduled a package");
                    }
                }));
    }

}
