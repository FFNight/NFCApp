package testapp.ttyi.certisme;

/**
 * Created by MikePeng on 11/1/17.
 */

public class GPSLocation {
    private double latitude;
    private double longitude;
    private String userType;

    public GPSLocation(){

    }

    public GPSLocation(double latitude,double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
        //this.userType=userType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
