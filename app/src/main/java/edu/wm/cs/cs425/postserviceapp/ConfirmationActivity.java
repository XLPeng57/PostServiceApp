package edu.wm.cs.cs425.postserviceapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import edu.wm.cs.cs425.postserviceapp.DAO.Package;
import edu.wm.cs.cs425.postserviceapp.DAO.User;
import edu.wm.cs.cs425.postserviceapp.Retrofit.MyService;
import edu.wm.cs.cs425.postserviceapp.Retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class ConfirmationActivity extends AppCompatActivity {

    private TextView textView;
    private Button button;
    private ImageView imageView;
    private String email;
    private String source;
    private String des;
    private FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        //initialize view objects
        textView = findViewById(R.id.edittext);
        imageView = findViewById(R.id.imageview);
        button = findViewById(R.id.trackbutton);

        //get confirmation information from intent
        email=getIntent().getStringExtra("email");
        String[] packageIDs=getIntent().getStringExtra("packageIDs").split("-");
        //content: confirmation message consists of name, csu, scheduleID, packageIDs
        StringBuilder content = new StringBuilder();
        for (String id: packageIDs) {
            content.append("Package " + id + ", ");
        }
        //get name and csu
        MyService myService=RetrofitClient.getInstance().create(MyService.class);
        Call<List<User>> info = myService.getUser(email);
        info.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> info, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    List<User> users = response.body();
                    for (User user : users){
                        content.insert(0,user.getName()+", CSU #"+user.getCsu()+": ");
                        textView.setText(content.toString().substring(0,content.length()-2));
                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        BitMatrix bitMatrix = null;
                        try {
                            bitMatrix = multiFormatWriter.encode(textView.toString(), BarcodeFormat.QR_CODE,2500,2500);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        imageView.setImageBitmap(bitmap);
                    }
                }

            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t){
                System.out.println("Callback Error");
            }
        });


        des = "CPU William and Mary";
        source = "Earth Fare Monticello Avenue";

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        button = findViewById(R.id.trackbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if(location!=null){
                                        Double lat = location.getLatitude();
                                        Double longt = location.getLongitude();

                                        source = lat.toString()+", "+longt.toString();
                                        System.out.println(source);

                                    }
                                }
                            });
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
                DisplayTrack(source, des);


            }
        });

    }

    private void DisplayTrack(String source, String des) {

        Uri uri = Uri.parse("https://www.google.co.in/maps/dir/"+source+"/"+des);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        intent.setPackage("com.google.android.apps.maps");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }
    //always back to main activity
    @Override
    public void onBackPressed(){
        Intent intent=new Intent(this, MainActivity.class);
        intent.putExtra("emailAddress",email);
        startActivity(intent);
    }



}
