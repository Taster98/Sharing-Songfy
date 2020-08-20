package ml.luiggi.geosongfy;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateNotification {
    //id
    public static final String CHANNEL_ID = "geosongfy_player";

    //azioni
    public static final String ACTION_PREV = "ml.luiggi.action.PREV";
    public static final String ACTION_PLAY = "ml.luiggi.action.PLAY";
    public static final String ACTION_NEXT = "ml.luiggi.action.NEXT";

    public static Notification notification;

    public static void createNotification(final Context context, final Song song, final int playButton, int pos, int size){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            final MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context,"tag");
            String aut_feat = song.getAuthors();
            if(!song.getFeats().equals("")){
                aut_feat += " ft. "+song.getFeats();
            }
            //PRECEDENTE
            Intent previousIntent = new Intent(context,NotificationActionService.class)
                    .setAction(ACTION_PREV);
            final PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(context,0,previousIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            //PLAY
            Intent playIntent = new Intent(context,NotificationActionService.class)
                    .setAction(ACTION_PLAY);
            final PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            //NEXT
            Intent nextIntent = new Intent(context,NotificationActionService.class)
                    .setAction(ACTION_NEXT);
            final PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            final String picture = song.getCover();

            final String finalAut_feat = aut_feat;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        URL url = new URL(picture);
                        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream in = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(in);
                        notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_music)
                                .setContentTitle(song.getTitle())
                                .setContentText(finalAut_feat)
                                .setOnlyAlertOnce(true)
                                .setShowWhen(false)
                                .addAction(R.drawable.ic_prev,"Previous",pendingIntentPrevious)
                                .addAction(playButton,"Play",pendingIntentPlay)
                                .addAction(R.drawable.ic_next,"Next",pendingIntentNext)
                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                        .setShowActionsInCompactView(0,1,2)
                                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setLargeIcon(myBitmap)
                                .build();
                        notificationManagerCompat.notify(1,notification);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
