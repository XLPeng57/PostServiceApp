package edu.wm.cs.cs425.postserviceapp.Retrofit;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import edu.wm.cs.cs425.postserviceapp.DAO.Package;
import edu.wm.cs.cs425.postserviceapp.DAO.Schedule;
import edu.wm.cs.cs425.postserviceapp.DAO.User;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface MyService {
    @POST("register")
    @FormUrlEncoded
    Observable<String> registerUser(@Field("email") String email,
                                    @Field("name") String name,
                                    @Field("password") String password,
                                    @Field("csu") String csu);

    @POST("login")
    @FormUrlEncoded
    Observable<String> loginUser(@Field("email") String email,
                                    @Field("password") String password);

    @POST("scan_in")
    @FormUrlEncoded
    Observable<String> scan_inPackage(@Field("email") String email,
                                    @Field("name") String name,
                                    @Field("package_id") String package_id,
                                      @Field("package_location") String package_location,
                                    @Field("package_size") String package_size,
                                    @Field("csu") String csu);

    @POST("create_schedule")
    @FormUrlEncoded
    Observable<String> create_schedule(@Field("id") @NotNull String id,
                                       @Field("date") @NotNull String date,
                                       @Field("time") String time,
                                       @Field("email") @NotNull String email,
                                       @Field("packageIDs") @NotNull String packageIDs,
                                       @Field("walkIn") @NotNull Boolean walkIn);
    //get a user's arrived yet not completed packages by email
    @GET("getPackagesByEmail")
    Call<List<Package>> getPackage(@Query("email") String email);

    @GET("getPackageWithId")
    Call<List<Package>> getPackageWithId(@Query("package_id") String package_id);

    @GET("getAllSchedules")
    Call<List<Schedule>> getAllSchedules();

    @GET("countScheduleByTime")
    Call<Integer> countScheduleByTime(@Query("date") String date, @Query("time") String time);

    @GET("countLockerByDate")
    Call<Integer> countLockerByDate(@Query("date")@NotNull String date);

    @PUT("schedule")
    @FormUrlEncoded
    Observable<String> package_schedule(@Field("email") String email,
                                        @Field("package_id") String package_id);
    //fulfill a schedule request by schedule id
    @PUT("fulfillRequestByID")
    @FormUrlEncoded
    Observable<String> fulfillRequest(@Field("id") String id);

    @PUT("completePackageByID")
    @FormUrlEncoded
    Observable<String> completePackageByID(@Field("package_id") String id);


    @GET("userinfo")
    Call<List<User>> getUser(@Query("email") String email);

    @GET("getUserByCSU")
    Call<List<User>> getUserByCSU(@Query("csu") String csu);









}
