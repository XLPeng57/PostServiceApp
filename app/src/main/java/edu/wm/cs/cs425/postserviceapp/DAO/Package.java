package edu.wm.cs.cs425.postserviceapp.DAO;

public class Package {

    private String email;
    private String name;
    private String csu;
    private String package_id;
    private String package_location;
    private String package_size;
    private String schedule;
    private String complete;

    public String getStudentEmail() {
        return email;
    }

    public String getStudentName() {
        return name;
    }

    public String getStudentCsu() {
        return csu;
    }


    public String getPackageId() {
        return package_id;
    }


    public String getPackageLocation() {
        return package_location;
    }

    public String getPackageSize() {
        return package_size;
    }


    public String getSchedule() {
        return schedule;
    }


    public String getComplete() {
        return complete;
    }

}
