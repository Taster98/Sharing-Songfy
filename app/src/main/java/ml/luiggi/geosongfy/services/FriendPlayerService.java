package ml.luiggi.geosongfy.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class FriendPlayerService extends Service {
    public static MediaPlayer mediaPlayer;
    static String urlMusic;
    int position;
    String uid;
    public FriendPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    DatabaseReference userdb = FirebaseDatabase.getInstance().getReference().child("user");
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bndl = intent.getExtras();
        if(bndl != null){
            uid = bndl.getString("uid");
        }
        Log.d("TAG","CANE"+uid);

        userdb.child(uid).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG","HELP");
                if(snapshot.exists()){
                    urlMusic = snapshot.child("songUrl").getValue(String.class);
                    position = Math.toIntExact(snapshot.child("position").getValue(Long.class));
                    Log.d("TAG","CANCARO "+urlMusic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        try {
            urlMusic = intent.getExtras().getString("songUrl");
            position = intent.getExtras().getInt("position");
            Log.d("TAG","CANCARO "+urlMusic);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(urlMusic);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(position);
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}