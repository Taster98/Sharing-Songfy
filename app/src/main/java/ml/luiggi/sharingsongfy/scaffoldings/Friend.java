package ml.luiggi.sharingsongfy.scaffoldings;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Objects;

//implementa serializable per poter esser passata come extra tra intent
public class Friend implements Serializable {
    //Mi servono lo uid, che coincide con l'id di Firebase
    private String uid,
    //nome e numero per poterli mostrare nel recycler view
    name,
            phoneNumber;
    //canzone corrente con posizione corrente per la funzionalità "ascolto condiviso"
    private Song currentSong;
    private int songPosition;

    public Friend(String uid, String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.uid = uid;
        this.currentSong = new Song();
        songPosition = 0;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setSongPosition(int songPosition) {
        this.songPosition = songPosition;
    }

    public int getSongPosition() {
        return songPosition;
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    public Song getCurrentSong() {
        return currentSong;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }


    //Riscrivo equals e hashcode per far sì che il confronto tra due oggetti di tipo Friend avvenga mediante Friend.getPhoneNumber()
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

}
