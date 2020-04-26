package irfan.apps.alourt.Handlers;

public class Activator {
    long mobile;
    String latitude;
    String longitude;
    String name;

    public Activator() {
    }

    public Activator(long mobile, String latitude, String longitude, String name) {
        this.mobile = mobile;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public long getMobile() {
        return mobile;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }
}
