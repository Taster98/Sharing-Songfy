package ml.luiggi.sharingsongfy.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import ml.luiggi.sharingsongfy.MainPageActivity;
import ml.luiggi.sharingsongfy.R;

public class FriendPlayerService extends Service {
    public static MediaPlayer mediaPlayer;
    static String urlMusic;
    int position;
    Boolean isSharing;
    String uid;
    String titolo = "";

    public FriendPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    DatabaseReference userdb = FirebaseDatabase.getInstance().getReference().child("user");

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainPageActivity.class);
        notificationIntent.setAction("STOP");
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification notification =
                    new Notification.Builder(this, "geosongfy_player")
                            .setContentTitle(getText(R.string.app_name))
                            .setContentText(getText(R.string.in_ascolto))
                            .setSmallIcon(R.drawable.ic_music)
                            .setContentIntent(pendingIntent)
                            .setTicker(getText(R.string.ticker_text))
                            .build();


            startForeground(1, notification);

        }
        //prelevo lo uid dal bundle del servizio:
        Bundle bndl = intent.getExtras();
        if (bndl != null) {
            uid = bndl.getString("uid");
        }
        //creo un riferimento al database rispettivo all'utente con uid come id
        userdb.child(uid).addValueEventListener(new ValueEventListener() {
            //con questo listener resto in ascolto sul cambio dei dati (in tempo reale nel db):
            //Se per caso qualcuno avesse messo in pausa la musica o avesse fermato la condivisione,
            //il servizio deve bloccare il player. Se invece la musica fosse cambiata, allora il player
            //deve ripartire con la nuova canzone.
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String prevSong;
                if (snapshot.exists()) {
                    prevSong = urlMusic;
                    urlMusic = snapshot.child("songUrl").getValue(String.class);
                    position = Math.toIntExact(snapshot.child("position").getValue(Long.class));
                    titolo = snapshot.child("title").getValue(String.class);
                    isSharing = snapshot.child("isSharing").getValue(Boolean.class);
                    try {
                        if (isSharing != null && isSharing) {
                            if (!prevSong.equals(urlMusic)) {
                                mediaPlayer.reset();
                                mediaPlayer.setDataSource(urlMusic);
                                mediaPlayer.prepare();
                                mediaPlayer.seekTo(position);
                                mediaPlayer.start();
                            }
                        } else {
                            mediaPlayer.stop();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //queste stesse operazioni devono essere fatte sin da subito e non solo quando i dati nel db cambiano.
        try {
            //inizialmente prelevo le informazioni dal bundle
            urlMusic = intent.getExtras().getString("songUrl");
            position = intent.getExtras().getInt("position");
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