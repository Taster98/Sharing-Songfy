package ml.luiggi.geosongfy;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import ml.luiggi.geosongfy.scaffoldings.Playable;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.services.OnClearFromRecentService;
import ml.luiggi.geosongfy.utils.CreateNotification;

public class SongActivity extends AppCompatActivity implements Playable {
    public static MediaPlayer mPlayer;
    private Song mSong;
    private ArrayList<Song> songList;
    private int actualPos;
    ImageButton mPlay, mBack, mNext;
    SeekBar mSeek;
    TextView mSeekTitle;
    NotificationManager notificationManager;
    ImageView mImageView;
    TextView mText2;
    TextView mTextView;
    LinearLayout mLinearLayout;
    LinearLayout container;
    MenuInflater menuInflater;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_play_layout);
        //prelevo i dati della canzone
        getSong();
        //inizializzo il MediaPlayer
        initializeMediaPlayer();
        //inizializzo la UI con essi
        initializeUI();
        //gestisco la musica
        handleMusic();
        //creo canale per la notifica
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
            //registro il receiver per la notifica
            registerReceiver(broadcastReceiver, new IntentFilter("ALL_SONGS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }
        //creo la notifica
        CreateNotification.createNotification(SongActivity.this, mSong, R.drawable.ic_pause);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.show_hide_buttons, menu);
        if(mPlay.getVisibility() == View.VISIBLE){
            menu.findItem(R.id.show_hide_button).setTitle(R.string.nascondi_pulsanti);

        }else{
            menu.findItem(R.id.show_hide_button).setTitle(R.string.mostra_pulsanti);
        }
        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals("Nascondi pulsanti")) {
            mPlay.setVisibility(View.INVISIBLE);
            mBack.setVisibility(View.INVISIBLE);
            mNext.setVisibility(View.INVISIBLE);
            item.setTitle(R.string.mostra_pulsanti);
        }else {
            mPlay.setVisibility(View.VISIBLE);
            item.setTitle(R.string.nascondi_pulsanti);
            mBack.setVisibility(View.VISIBLE);
            mNext.setVisibility(View.VISIBLE);
        }

        return true;
    }

    //funzione che mi crea un canale univoco per la notifica
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID, "Luiggi", NotificationManager.IMPORTANCE_HIGH);
            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    //funzione per inizializzare il mediaPlayer
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initializeMediaPlayer() {
        String url = mSong.getUrl();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        mPlayer = new MediaPlayer();
        mPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mPlayer.reset();
            mPlayer.setDataSource(url);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //funzione che converte i millisecondi in minuti e secondi, riformattando il tutto:
    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
        buf.append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));
        return buf.toString();
    }

    //avvia la musica
    @SuppressLint("ClickableViewAccessibility")
    private void handleMusic() {
        //Ripesco la lista di tutti i brani per i pulsanti Back e Next
        songList = (ArrayList<Song>) getIntent().getSerializableExtra("allSongs");
        //prelevo la posizione attuale nell'arraylist
        actualPos = songList.indexOf(mSong);
        //Riferimenti alle varie viste
        mPlay = (ImageButton) findViewById(R.id.song_item_play);
        mSeek = (SeekBar) findViewById(R.id.song_item_seekbar);
        mSeekTitle = (TextView) findViewById(R.id.song_item_seekTitle);
        mBack = (ImageButton) findViewById(R.id.song_item_previous);
        mNext = (ImageButton) findViewById(R.id.song_item_next);
        //Imposto la durata (da ms a s)
        mSeek.setMax(mPlayer.getDuration() / 1000);
        //Inizializzo il titolo della SeekBar
        String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
        mSeekTitle.setText(seekTitle);
        mPlay.setImageResource(R.drawable.ic_pause);
        final Handler mHandler = new Handler();

        //CONTROLLO VOLUME
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        //Eseguo il tutto nel thread dell'UI
        SongActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null) {
                    int mCurrentPosition = mPlayer.getCurrentPosition() / 1000;
                    mSeek.setProgress(mCurrentPosition);
                    String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
                    mSeekTitle.setText(seekTitle);
                }
                mHandler.postDelayed(this, 1000);
            }
        });
        //Gestisco lo spostamento manuale della SeekBar:
        mSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            //L'unico metodo che mi interessa è quello che attende che il seekbar cambi posizione
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mPlayer != null && fromUser) {
                    mPlayer.seekTo(progress * 1000);
                    String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
                    mSeekTitle.setText(seekTitle);
                }
            }
        });
        //INIZIO GESTIONE CONTROLLI
        onTrackPlay();

        mImageView.setLongClickable(true);
        mImageView.setClickable(true);

        //Imposto un listener sul bottone Play
        mPlay.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                //GESTIONE PLAY E PAUSE
                //Pause:
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    mPlay.setImageResource(R.drawable.ic_play);
                    onTrackPause();
                } else {
                    //Play:
                    mPlayer.start();
                    onTrackPlay();
                    //Creo un handler per gestire la SeekBar
                    final Handler mHandler = new Handler();
                    //Eseguo il tutto nel thread dell'UI
                    SongActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mPlayer != null) {
                                int mCurrentPosition = mPlayer.getCurrentPosition() / 1000;
                                mSeek.setProgress(mCurrentPosition);
                                String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
                                mSeekTitle.setText(seekTitle);
                            }
                            mHandler.postDelayed(this, 1000);
                        }
                    });

                    //Gestisco lo spostamento manuale della SeekBar:
                    mSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (mPlayer != null && fromUser) {
                                mPlayer.seekTo(progress * 1000);
                                String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
                                mSeekTitle.setText(seekTitle);
                            }
                        }
                    });
                    //Cambio l'icona da Play a Pause:
                    mPlay.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        //GESTURE CONTROLS
        mLinearLayout.setOnTouchListener(new OnSwipeTouchListener(this){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSwipeRight() {
                Toast.makeText(getApplicationContext(),"Precedente",Toast.LENGTH_SHORT).show();
                super.onSwipeRight();
                actualPos--;
                if (actualPos == -1) {
                    actualPos = songList.size() - 1;
                }
                mSong = songList.get(actualPos);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackPreviousNoPlay();
                initializeMusicUI();
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSwipeLeft() {
                Toast.makeText(getApplicationContext(),"Successiva",Toast.LENGTH_SHORT).show();
                super.onSwipeLeft();
                actualPos++;
                if (actualPos >= songList.size()) {
                    actualPos = 0;
                }
                mSong = songList.get(actualPos);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackNextNoPlay();
                initializeMusicUI();
            }
            @Override
            public void onClick() {
                super.onClick();
                //GESTIONE PLAY E PAUSE
                //Pause:
                if (mPlayer.isPlaying()) {
                    Toast.makeText(getApplicationContext(),"Pausa",Toast.LENGTH_SHORT).show();
                    mPlayer.pause();
                    mPlay.setImageResource(R.drawable.ic_play);
                    onTrackPause();
                } else {
                    Toast.makeText(getApplicationContext(),"Play",Toast.LENGTH_SHORT).show();
                    //Play:
                    mPlayer.start();
                    onTrackPlay();
                    //Creo un handler per gestire la SeekBar
                    final Handler mHandler = new Handler();
                    //Eseguo il tutto nel thread dell'UI
                    SongActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mPlayer != null) {
                                int mCurrentPosition = mPlayer.getCurrentPosition() / 1000;
                                mSeek.setProgress(mCurrentPosition);
                                String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
                                mSeekTitle.setText(seekTitle);
                            }
                            mHandler.postDelayed(this, 1000);
                        }
                    });
                    //Cambio l'icona da Play a Pause:
                    mPlay.setImageResource(R.drawable.ic_pause);
                }
            }
            @Override
            public void onLongClick() {
                Toast.makeText(getApplicationContext(),"Dall'inizio",Toast.LENGTH_SHORT).show();
                super.onLongClick();
                mPlayer.seekTo(0);
            }
            //CONTROLLO VOLUME
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
            }
        });
        mImageView.setOnTouchListener(new OnSwipeTouchListener(this){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSwipeRight() {
                Toast.makeText(getApplicationContext(),"Precedente",Toast.LENGTH_SHORT).show();
                super.onSwipeRight();
                actualPos--;
                if (actualPos == -1) {
                    actualPos = songList.size() - 1;
                }
                mSong = songList.get(actualPos);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackPreviousNoPlay();
                initializeMusicUI();
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSwipeLeft() {
                Toast.makeText(getApplicationContext(),"Successiva",Toast.LENGTH_SHORT).show();
                super.onSwipeLeft();
                actualPos++;
                if (actualPos >= songList.size()) {
                    actualPos = 0;
                }
                mSong = songList.get(actualPos);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackNextNoPlay();
                initializeMusicUI();
            }
            @Override
            public void onClick() {
                super.onClick();
                //GESTIONE PLAY E PAUSE
                //Pause:
                if (mPlayer.isPlaying()) {
                    Toast.makeText(getApplicationContext(),"Pausa",Toast.LENGTH_SHORT).show();
                    mPlayer.pause();
                    mPlay.setImageResource(R.drawable.ic_play);
                    onTrackPause();
                } else {
                    Toast.makeText(getApplicationContext(),"Play",Toast.LENGTH_SHORT).show();
                    //Play:
                    mPlayer.start();
                    onTrackPlay();
                    //Creo un handler per gestire la SeekBar
                    final Handler mHandler = new Handler();
                    //Eseguo il tutto nel thread dell'UI
                    SongActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mPlayer != null) {
                                int mCurrentPosition = mPlayer.getCurrentPosition() / 1000;
                                mSeek.setProgress(mCurrentPosition);
                                String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
                                mSeekTitle.setText(seekTitle);
                            }
                            mHandler.postDelayed(this, 1000);
                        }
                    });
                    //Cambio l'icona da Play a Pause:
                    mPlay.setImageResource(R.drawable.ic_pause);
                }
            }
            @Override
            public void onLongClick() {
                Toast.makeText(getApplicationContext(),"Dall'inizio",Toast.LENGTH_SHORT).show();
                super.onLongClick();
                mPlayer.seekTo(0);
            }
            //CONTROLLO VOLUME
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
            }
        });
        container.setOnTouchListener(new OnSwipeTouchListener(this){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSwipeRight() {
                Toast.makeText(getApplicationContext(),"Precedente",Toast.LENGTH_SHORT).show();
                super.onSwipeRight();
                actualPos--;
                if (actualPos == -1) {
                    actualPos = songList.size() - 1;
                }
                mSong = songList.get(actualPos);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackPreviousNoPlay();
                initializeMusicUI();
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSwipeLeft() {
                Toast.makeText(getApplicationContext(),"Successiva",Toast.LENGTH_SHORT).show();
                super.onSwipeLeft();
                actualPos++;
                if (actualPos >= songList.size()) {
                    actualPos = 0;
                }
                mSong = songList.get(actualPos);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackNextNoPlay();
                initializeMusicUI();
            }
            @Override
            public void onClick() {
                super.onClick();
                //GESTIONE PLAY E PAUSE
                //Pause:
                if (mPlayer.isPlaying()) {
                    Toast.makeText(getApplicationContext(),"Pausa",Toast.LENGTH_SHORT).show();
                    mPlayer.pause();
                    mPlay.setImageResource(R.drawable.ic_play);
                    onTrackPause();
                } else {
                    Toast.makeText(getApplicationContext(),"Play",Toast.LENGTH_SHORT).show();
                    //Play:
                    mPlayer.start();
                    onTrackPlay();
                    //Creo un handler per gestire la SeekBar
                    final Handler mHandler = new Handler();
                    //Eseguo il tutto nel thread dell'UI
                    SongActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mPlayer != null) {
                                int mCurrentPosition = mPlayer.getCurrentPosition() / 1000;
                                mSeek.setProgress(mCurrentPosition);
                                String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
                                mSeekTitle.setText(seekTitle);
                            }
                            mHandler.postDelayed(this, 1000);
                        }
                    });
                    //Cambio l'icona da Play a Pause:
                    mPlay.setImageResource(R.drawable.ic_pause);
                }
            }
            @Override
            public void onLongClick() {
                Toast.makeText(getApplicationContext(),"Dall'inizio",Toast.LENGTH_SHORT).show();
                super.onLongClick();
                mPlayer.seekTo(0);
            }
            //CONTROLLO VOLUME
            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
            }
        });

        //Imposto un listener sul bottone Back
        mBack.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if ((mPlayer.getCurrentPosition() / 1000) < 4) {
                    actualPos--;
                    if (actualPos == -1) {
                        actualPos = songList.size() - 1;
                    }
                    mSong = songList.get(actualPos);
                    mPlayer.stop();
                    initializeMediaPlayer();
                    mPlayer.start();
                    mPlay.setImageResource(R.drawable.ic_pause);
                    onTrackPreviousNoPlay();
                    initializeMusicUI();
                } else {
                    mPlayer.seekTo(0);
                }
            }
        });

        //Imposto un listener sul bottone Next
        mNext.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                actualPos++;
                if (actualPos >= songList.size()) {
                    actualPos = 0;
                }
                mSong = songList.get(actualPos);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackNextNoPlay();
                initializeMusicUI();
            }
        });

        //Imposto lo scorrimento automatico quando la canzone finisce
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                actualPos++;
                if (actualPos >= songList.size()) {
                    actualPos = 0;
                }
                mSong = songList.get(actualPos);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackNext();
                initializeMusicUI();
            }
        });
    }

    //riempio la canzone selezionata
    private void getSong() {
        mSong = (Song) getIntent().getSerializableExtra("songSelected");
    }

    @SuppressLint("RestrictedApi")
    private void initializeUI() {
        //Verifico che non vi siano Null values
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        initializeMusicUI();
    }

    private void initializeMusicUI() {
        mImageView = (ImageView) findViewById(R.id.song_item_image);
        mText2 = (TextView) findViewById(R.id.song_item_title);
        mTextView = (TextView) findViewById(R.id.song_item_author);
        mLinearLayout = (LinearLayout)findViewById(R.id.lineare_immagine_song);
        Picasso.get().load(mSong.getCover()).into(mImageView);
        String aut_feat = mSong.getAuthors();
        if (!mSong.getFeats().equals(""))
            aut_feat += " ft. " + mSong.getFeats();
        mTextView.setText(aut_feat);
        mText2.setText(mSong.getTitle());
        container = findViewById(R.id.song_layout);
    }

    //Bottone indietro
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    boolean isPlaying = false;
    //Creo un nuovo broadcast receiver per gestire le azioni ricevute dalla notifica
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionName");
            //controllo l'azione ricevuta
            switch (action) {
                case CreateNotification.ACTION_PREV:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (isPlaying) {
                        onTrackPause();
                    } else {
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
            }
        }
    };

    //Implemento i metodi dell'interfaccia Playable, facendo attenzione a modificare anche la UI qualora l'app fosse aperta
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onTrackPrevious() {
        actualPos--;
        if (actualPos < 0)
            actualPos = songList.size() - 1;
        mSong = songList.get(actualPos);
        CreateNotification.createNotification(SongActivity.this, mSong, R.drawable.ic_pause);
        mText2.setText(mSong.getTitle());
        String aut_feat = mSong.getAuthors();
        if (!mSong.getFeats().equals(""))
            aut_feat += " ft. " + mSong.getFeats();
        mTextView.setText(aut_feat);
        Picasso.get().load(mSong.getCover()).into(mImageView);
        //CODICE PER ANDARE INDIETRO
        mPlayer.stop();
        initializeMediaPlayer();
        handleMusic();
        mPlayer.start();
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onTrackPreviousNoPlay(){
        mSong = songList.get(actualPos);
        CreateNotification.createNotification(SongActivity.this, mSong, R.drawable.ic_pause);
        mText2.setText(mSong.getTitle());
        String aut_feat = mSong.getAuthors();
        if (!mSong.getFeats().equals(""))
            aut_feat += " ft. " + mSong.getFeats();
        mTextView.setText(aut_feat);
        Picasso.get().load(mSong.getCover()).into(mImageView);
        //CODICE PER ANDARE INDIETRO
        mPlayer.stop();
        initializeMediaPlayer();
        handleMusic();
        mPlayer.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onTrackNextNoPlay(){
        mSong = songList.get(actualPos);
        CreateNotification.createNotification(SongActivity.this, mSong, R.drawable.ic_pause);
        mText2.setText(mSong.getTitle());
        String aut_feat = mSong.getAuthors();
        if (!mSong.getFeats().equals(""))
            aut_feat += " ft. " + mSong.getFeats();
        mTextView.setText(aut_feat);
        Picasso.get().load(mSong.getCover()).into(mImageView);
        //CODICE PER ANDARE AVANTI
        mPlayer.stop();
        initializeMediaPlayer();
        handleMusic();
        mPlayer.start();
    }
    @Override
    public void onTrackPlay() {
        CreateNotification.createNotification(SongActivity.this, mSong, R.drawable.ic_pause);
        mText2.setText(mSong.getTitle());
        String aut_feat = mSong.getAuthors();
        if (!mSong.getFeats().equals(""))
            aut_feat += " ft. " + mSong.getFeats();
        mTextView.setText(aut_feat);
        //CODICE PER PLAY
        mPlayer.start();
        mPlay.setImageResource(R.drawable.ic_pause);
        isPlaying = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onTrackNext() {
        actualPos++;
        if (actualPos >= songList.size())
            actualPos = 0;
        mSong = songList.get(actualPos);
        CreateNotification.createNotification(SongActivity.this, mSong, R.drawable.ic_pause);
        mText2.setText(mSong.getTitle());
        String aut_feat = mSong.getAuthors();
        if (!mSong.getFeats().equals(""))
            aut_feat += " ft. " + mSong.getFeats();
        mTextView.setText(aut_feat);
        Picasso.get().load(mSong.getCover()).into(mImageView);
        //CODICE PER ANDARE AVANTI
        mPlayer.stop();
        initializeMediaPlayer();
        handleMusic();
        mPlayer.start();
    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(SongActivity.this, mSong, R.drawable.ic_play);
        mText2.setText(mSong.getTitle());
        String aut_feat = mSong.getAuthors();
        if (!mSong.getFeats().equals(""))
            aut_feat += " ft. " + mSong.getFeats();
        mTextView.setText(aut_feat);
        //CODICE PER PLAY
        mPlayer.pause();
        mPlay.setImageResource(R.drawable.ic_play);
        isPlaying = false;
    }
}
