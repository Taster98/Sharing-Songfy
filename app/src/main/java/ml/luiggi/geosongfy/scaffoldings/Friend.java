package ml.luiggi.geosongfy.scaffoldings;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Objects;

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

    public Friend() {
    }

    public Friend(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friend friend = (Friend) o;
        return Objects.equals(phoneNumber, friend.phoneNumber);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey=notificationKey;
    }
}
