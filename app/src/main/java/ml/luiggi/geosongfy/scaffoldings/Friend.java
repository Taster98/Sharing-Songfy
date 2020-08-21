package ml.luiggi.geosongfy.scaffoldings;

import java.io.Serializable;

public class Friend implements Serializable {
    private String uid,
            name,
            phoneNumber,
            notificationKey;

    public Friend(String uid, String name, String phoneNumber, String notificationKey) {
        this.name = name;
        this.notificationKey = notificationKey;
        this.phoneNumber = phoneNumber;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }
}
