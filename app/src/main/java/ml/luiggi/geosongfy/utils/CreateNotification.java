package ml.luiggi.geosongfy.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.SongActivity;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.services.NotificationActionService;

/*
 * Questa classe rappresenta la creazione della notifica e le interpretazioni dei comandi ricevuti su di essa.
 */
public class CreateNotification {
    //id del channel per la notifica
    public static final String CHANNEL_ID = "geosongfy_player";
    //azioni interpretabili nella notifica
    public static final String ACTION_PREV = "ml.luiggi.action.PREV";
    public static final String ACTION_PLAY = "ml.luiggi.action.PLAY";
    public static final String ACTION_NEXT = "ml.luiggi.action.NEXT";

    public static Notification notification;

    //funzione che crea una notifica
    public static void createNotification(final Context context, final Song song, final int playButton) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            final MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");
            String aut_feat = song.getAuthors();
            if (!song.getFeats().equals("")) {
                aut_feat += " ft. " + song.getFeats();
            }
            //PRECEDENTE
            Intent previousIntent = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PREV);
            final PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //PLAY
            Intent playIntent = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PLAY);
            final PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //SUCCESSIVO
            Intent nextIntent = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_NEXT);

            //Open SongActivity
            Intent openIntent = new Intent(context, SongActivity.class);
            openIntent.setAction(Long.toString(System.currentTimeMillis()));
            openIntent.putExtra("notify",1);
            openIntent.putExtra("songSelected",SongActivity.mSong);
            openIntent.putExtra("allSongs",SongActivity.songList);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //openIntent.putExtra("notify", 1);
            final PendingIntent openingIntent = PendingIntent.getActivity(context,0,openIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            //passo anche l'arraylist totale perchè potrebbe servirmi per implementare poi il pulsante avanti/indietro
            final PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            final String picture = song.getCover();

            final String finalAut_feat = aut_feat;
            //Creo un thread per il download dell'immagine per non intasare il thread dell'UI;dunque creo la notifica nel thread.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(picture);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream in = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(in);
                        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_music)
                                .setContentTitle(song.getTitle())
                                .setContentText(finalAut_feat)
                                .setOnlyAlertOnce(true)
                                .setShowWhen(false)
                                .addAction(R.drawable.ic_prev, "Previous", pendingIntentPrevious)
                                .addAction(playButton, "Play", pendingIntentPlay)
                                .addAction(R.drawable.ic_next, "Next", pendingIntentNext)
                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                        .setShowActionsInCompactView(0, 1, 2)
                                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(openingIntent)
                                .setLargeIcon(myBitmap)
                                .build();
                        notificationManagerCompat.notify(1, notification);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
