package edu.wm.cs.cs425.postserviceapp.DAO;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @Author: Jenny Sun
 * A Data Access Object class for schedules
 */
public class Schedule implements Comparator<Schedule> {
    private String id;
    private String date;
    private String time;
    private String email;
    private String packageIDs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPackageIDs() {
        return packageIDs;
    }

    public void setPackageIDs(String packageIDs) {
        this.packageIDs = packageIDs;
    }


    @Override
    public int compare(Schedule o1, Schedule o2) {
        String date1 = o1.date;
        String date2 = o2.date;
        String time1 = o1.time;
        String time2 = o2.time;

        String[] mdy1 = date1.split("/");  //0 index is month,1 index is day, 2 index is year
        String[] mdy2 = date2.split("/");
        String hr1 = time1.replace(" ","").split("-")[0];
        String hr2 = time2.replace(" ","").split("-")[0];

        if(Integer.parseInt(mdy1[2]) < Integer.parseInt(mdy2[2])) {
            return -1;
        } else if (Integer.parseInt(mdy1[2]) > Integer.parseInt(mdy2[2])){
            return 1;
        } else {
            if(Integer.parseInt(mdy1[0]) < Integer.parseInt(mdy2[0])) {
                return -1;
            } else if (Integer.parseInt(mdy1[0]) > Integer.parseInt(mdy2[0])) {
                return 1;
            } else {
                if(Integer.parseInt(mdy1[1]) < Integer.parseInt(mdy2[1])) {
                    return -1;
                } else if (Integer.parseInt(mdy1[1]) > Integer.parseInt(mdy2[1])) {
                    return 1;
                } else {
                    boolean hr1isAm=hr1.substring(hr1.length()-2).equals("am");
                    boolean hr2isAm=hr2.substring(hr2.length()-2).equals("am");
                    int h1=Integer.parseInt(hr1.substring(0,hr1.length()-2));
                    int h2=Integer.parseInt(hr2.substring(0,hr2.length()-2));
                    if( hr1isAm && !hr2isAm){
                        return -1;
                    }else if(hr1isAm){
                        if(h1<h2){
                            return -1;
                        }else if(h1>h2){
                            return 1;
                        }
                    }else{
                        if(hr1.equals("12pm")|| (h1<h2 && !hr2.equals("12pm") )){
                            return -1;
                        }else if(hr2.equals("12pm") || h1>h2){
                            return 1;
                        }
                    }
                }
            }
        }
        return 0;
    }
}
