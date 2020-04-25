package irfan.apps.alourt.Handlers;

public class User {
    String userName;
    long mobile;
    boolean isAdmin;


    public User(String name, long mobile, boolean isAdmin) {
        this.userName = name;
        this.mobile = mobile;
        this.isAdmin = isAdmin;
    }

    public User() {
    }

    public String getUserName() {
        return userName;
    }

    public long getMobile() {
        return mobile;
    }

    public boolean getAdminStatus() {
        return isAdmin;
    }

}
