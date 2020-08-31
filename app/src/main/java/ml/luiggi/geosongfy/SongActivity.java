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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import ml.luiggi.geosongfy.scaffoldings.Playable;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.services.OnClearFromRecentService;
import ml.luiggi.geosongfy.utils.CreateNotification;

public class SongActivity extends AppCompatActivity implements Playable, View.OnTouchListener {
    public static MediaPlayer mPlayer;
    public static Song mSong;
    public static ArrayList<Song> songList;
    private int actualPos;
    public static long progresso;
    ImageButton mPlay, mBack, mNext;
    SeekBar mSeek;
    TextView mSeekTitle;
    public static NotificationManager notificationManager;
    ImageView mImageView;
    TextView mText2;
    TextView mTextView;
    LinearLayout mLinearLayout;
    LinearLayout container;
    SeekBar volumeBar;
    AudioManager audioManager;
    Vibrator vib;

    @Override
    protected void onDestroy() {
        volumeBar.setVisibility(View.INVISIBLE);
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_play_layout);
        //prelevo i dati della canzone
        getSong();
        if (getIntent().getIntExtra("notify", 0) == 0) {
            //inizializzo il MediaPlayer
            initializeMediaPlayer();
        }
        //inizializzo la UI con essi
        initializeUI();
        //gestisco la musica
        handleMusic();

        //GESTIONE NOTIFICA
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
    protected void onResume() {
        super.onResume();
        volumeBar.setVisibility(View.INVISIBLE);
    }

    //funzione che mi crea un canale univoco per la notifica
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID, "Luiggi", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
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
            dbUsers.child("isSharing").setValue(true);
            dbUsers.child("songUrl").setValue(mSong.getUrl());
            dbUsers.child("author").setValue(mSong.getAuthors());
            dbUsers.child("feats").setValue(mSong.getFeats());
            dbUsers.child("title").setValue(mSong.getTitle());
            progresso = mPlayer.getCurrentPosition();
            dbUsers.child("position").setValue(progresso);
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

    //funzione che inizializza i riferimenti per gestire la musica
    private void initRefs() {
        //Ripesco la lista di tutti i brani per i pulsanti Back e Next
        songList = (ArrayList<Song>) getIntent().getSerializableExtra("allSongs");
        if (mSong != null) {
            //prelevo la posizione attuale nell'arraylist
            actualPos = songList.indexOf(mSong);
        }
        //Riferimenti alle varie viste
        mPlay = (ImageButton) findViewById(R.id.song_item_play);
        mSeek = (SeekBar) findViewById(R.id.song_item_seekbar);
        mSeekTitle = (TextView) findViewById(R.id.song_item_seekTitle);
        mBack = (ImageButton) findViewById(R.id.song_item_previous);
        mNext = (ImageButton) findViewById(R.id.song_item_next);
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        //vibration object
        vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        //Imposto la durata (da ms a s)
        mSeek.setMax(mPlayer.getDuration() / 1000);

        mPlay.setImageResource(R.drawable.ic_pause);
    }

    //funzione che gestisce i listener della SeekBar
    private void seekBarHandler() {
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
                    progresso = mPlayer.getCurrentPosition();
                    dbUsers.child("position").setValue(progresso);
                }
            }
        });
    }

    //funzione per la gestione dei pulsanti play/pause
    private void playPauseHandler() {
        onTrackPlay();

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
                    Boolean b = Boolean.FALSE;
                    dbUsers.child("isSharing").setValue(b);
                    progresso = mPlayer.getCurrentPosition();
                    dbUsers.child("position").setValue(progresso);
                } else {
                    //Play:
                    mPlayer.start();
                    onTrackPlay();
                    Boolean b = Boolean.TRUE;
                    dbUsers.child("isSharing").setValue(b);
                    dbUsers.child("songUrl").setValue(mSong.getUrl());
                    dbUsers.child("author").setValue(mSong.getAuthors());
                    dbUsers.child("feats").setValue(mSong.getFeats());
                    dbUsers.child("title").setValue(mSong.getTitle());
                    //Creo un handler per gestire la SeekBar
                    final Handler mHandler = new Handler();
                    //Eseguo il tutto nel thread dell'UI
                    SongActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mPlayer != null) {
                                int mCurrentPosition = mPlayer.getCurrentPosition() / 1000;
                                mSeek.setProgress(mCurrentPosition);
                                progresso = mPlayer.getCurrentPosition();
                                dbUsers.child("position").setValue(progresso);
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
                                progresso = mPlayer.getCurrentPosition();
                                dbUsers.child("position").setValue(progresso);
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
    }

    //funzione per la gestione del pulsante indietro
    private void backHandler() {
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
                    dbUsers.child("songUrl").setValue(mSong.getUrl());
                    dbUsers.child("author").setValue(mSong.getAuthors());
                    dbUsers.child("feats").setValue(mSong.getFeats());
                    dbUsers.child("title").setValue(mSong.getTitle());
                    progresso = mPlayer.getCurrentPosition();
                    dbUsers.child("position").setValue(progresso);
                    mPlayer.stop();
                    initializeMediaPlayer();
                    mPlayer.start();
                    mPlay.setImageResource(R.drawable.ic_pause);
                    onTrackPreviousNoPlay();
                    initializeMusicUI();
                } else {
                    mPlayer.seekTo(0);
                    progresso = mPlayer.getCurrentPosition();
                    dbUsers.child("position").setValue(progresso);

                }
            }
        });
    }

    //funzione per la gestione del pulsante avanti
    private void nextHandler() {
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
                dbUsers.child("songUrl").setValue(mSong.getUrl());
                dbUsers.child("author").setValue(mSong.getAuthors());
                dbUsers.child("feats").setValue(mSong.getFeats());
                dbUsers.child("title").setValue(mSong.getTitle());
                progresso = mPlayer.getCurrentPosition();
                dbUsers.child("position").setValue(progresso);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackNextNoPlay();
                initializeMusicUI();
            }
        });
    }

    //funzione per la gestione automatica ciclica
    private void continueMusic() {
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
                dbUsers.child("songUrl").setValue(mSong.getUrl());
                dbUsers.child("author").setValue(mSong.getAuthors());
                dbUsers.child("feats").setValue(mSong.getFeats());
                dbUsers.child("title").setValue(mSong.getTitle());
                progresso = mPlayer.getCurrentPosition();
                dbUsers.child("position").setValue(progresso);
                mPlayer.stop();
                initializeMediaPlayer();
                mPlayer.start();
                mPlay.setImageResource(R.drawable.ic_pause);
                onTrackNext();
                initializeMusicUI();
            }
        });
    }

    //avvia la musica
    //Firebase è perforza diverso da null in quanto l'utente a questo punto è loggato
    final DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid());

    @SuppressLint("ClickableViewAccessibility")
    private void handleMusic() {
        //inizializzo i riferimenti
        initRefs();
        //DatabaseReference riferimenti

        //Inizializzo il titolo della SeekBar
        String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
        mSeekTitle.setText(seekTitle);
        final Handler mHandler = new Handler();
        //Eseguo il tutto nel thread dell'UI
        SongActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mPlayer != null) {
                    int mCurrentPosition = mPlayer.getCurrentPosition() / 1000;
                    mSeek.setProgress(mCurrentPosition);
                    progresso = mPlayer.getCurrentPosition();
                    dbUsers.child("position").setValue(progresso);
                    String seekTitle = getTimeString(mPlayer.getCurrentPosition()) + "/" + getTimeString(mPlayer.getDuration());
                    mSeekTitle.setText(seekTitle);
                }
                mHandler.postDelayed(this, 1000);
            }
        });
        //Gestisco lo spostamento manuale della SeekBar:
        seekBarHandler();
        //INIZIO GESTIONE CONTROLLI
        //Controlli con i pulsanti
        //PULSANTI PLAY/PAUSE
        playPauseHandler();
        //PULSANTE INDIETRO
        backHandler();
        //PULSANTE AVANTI
        nextHandler();
        //Ascolto continuo musica
        continueMusic();

        //GESTURE CONTROLS
        mLinearLayout.setOnTouchListener(this);
        mImageView.setOnTouchListener(this);
        container.setOnTouchListener(this);
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
        mLinearLayout = (LinearLayout) findViewById(R.id.lineare_immagine_song);
        if (mSong != null) {
            Picasso.get().load(mSong.getCover()).into(mImageView);
            String aut_feat = mSong.getAuthors();
            if (!mSong.getFeats().equals(""))
                aut_feat += " ft. " + mSong.getFeats();
            mTextView.setText(aut_feat);
            mText2.setText(mSong.getTitle());
        }
        container = findViewById(R.id.song_layout);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeBar = findViewById(R.id.volumeBar);
        volumeBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, AudioManager.FLAG_SHOW_UI);
            }
        });
    }

    //Bottone indietro
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    boolean isPlaying = false;
    //Creo un nuovo broadcast receiver per gestire le azioni ricevute dalla notifica
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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
        dbUsers.child("songUrl").setValue(mSong.getUrl());
        dbUsers.child("author").setValue(mSong.getAuthors());
        dbUsers.child("feats").setValue(mSong.getFeats());
        dbUsers.child("title").setValue(mSong.getTitle());
        progresso = mPlayer.getCurrentPosition();
        dbUsers.child("position").setValue(progresso);
    }

    //Funzione per andare indietro senza decrementare la posizione (PER NOTIFICA)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onTrackPreviousNoPlay() {
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

    //Funzione per andare avanti senza incrementare la posizione (PER NOTIFICA)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onTrackNextNoPlay() {
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
        Boolean b = Boolean.TRUE;
        dbUsers.child("isSharing").setValue(b);
        dbUsers.child("songUrl").setValue(mSong.getUrl());
        dbUsers.child("author").setValue(mSong.getAuthors());
        dbUsers.child("feats").setValue(mSong.getFeats());
        dbUsers.child("title").setValue(mSong.getTitle());
        progresso = mPlayer.getCurrentPosition();
        dbUsers.child("position").setValue(progresso);
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
        dbUsers.child("songUrl").setValue(mSong.getUrl());
        dbUsers.child("author").setValue(mSong.getAuthors());
        dbUsers.child("feats").setValue(mSong.getFeats());
        dbUsers.child("title").setValue(mSong.getTitle());
        progresso = mPlayer.getCurrentPosition();
        dbUsers.child("position").setValue(progresso);
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
        Boolean b = Boolean.FALSE;
        dbUsers.child("isSharing").setValue(b);
        progresso = mPlayer.getCurrentPosition();
        dbUsers.child("position").setValue(progresso);
    }

    /*LISTENER E ALTRE FUNZIONI PER LE GESTURES CUSTOMIZZATE*/

    //un dito
    float startX = -1, startY = -1, middleX = -1, middleY = -1, endX = -1, endY = -1;
    static final float SOGLIA_MIN_MEDIUM_X = 120;
    static final float SOGLIA_MAX_MEDIUM_X = 250;
    static final float SOGLIA_MIN_Y = 200;
    static final float SOGLIA_MAX_Y = 450;
    static final float RANGE_MAX_ERRORE = 140;
    boolean medium = false, finaleSin = false, finaleDes = false;

    //due dita
    float startFingerY1 = -1, startFingerY2 = -1, endFingerY1 = -1, endFingerY2 = -1;
    static final float SOGLIA_MIN_DUE_DITA = 100;

    //se true allora il range del valore medio (la 'punta') è rispettato
    boolean rangeMedium(float medX) {
        return Math.abs(medX - startX) >= SOGLIA_MIN_MEDIUM_X && Math.abs(medX - startX) <= SOGLIA_MAX_MEDIUM_X;
    }

    //se true la punta è rivolta a sinistra e ho disegnato un arco (o una freccia) rivolto a sinistra
    boolean rangeSinFinale() {
        return Math.abs(endX - startX) <= RANGE_MAX_ERRORE && endX != -1 && startX != -1 && endX > middleX && endY > middleY && middleY != -1 && Math.abs(endY - startY) >= SOGLIA_MIN_Y && Math.abs(endY - startY) <= SOGLIA_MAX_Y;
    }

    //se true la punta è rivolta a destra e ho disegnato un arco (o una freccia) rivolto a destra
    boolean rangeDesFinale() {
        return Math.abs(endX - startX) <= RANGE_MAX_ERRORE && endX != -1 && startX != -1 && endX < middleX && endY > middleY && middleY != -1 && Math.abs(endY - startY) >= SOGLIA_MIN_Y && Math.abs(endY - startY) <= SOGLIA_MAX_Y;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouch(View view, final MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case (MotionEvent.ACTION_DOWN):
                if (motionEvent.getPointerCount() == 1) {
                    startX = motionEvent.getX();
                    startY = motionEvent.getY();
                } else if (motionEvent.getPointerCount() > 1) {
                    startFingerY1 = motionEvent.getY(0);
                    startFingerY2 = motionEvent.getY(1);
                }

                return true;
            case (MotionEvent.ACTION_MOVE):
                if (motionEvent.getPointerCount() == 1) {
                    if (rangeMedium(motionEvent.getX())) {
                        middleX = motionEvent.getX();
                        middleY = motionEvent.getY();
                        medium = true;
                    }
                }
                return true;
            case (MotionEvent.ACTION_UP):
                if (motionEvent.getPointerCount() == 1) {
                    endX = motionEvent.getX();
                    endY = motionEvent.getY();
                    if (rangeSinFinale()) {
                        finaleSin = true;
                    } else if (rangeDesFinale()) {
                        finaleDes = true;
                    }
                    if (medium) {
                        if (finaleDes) {
                            //vibra per 200 millisecondi
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vib.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vib.vibrate(200);
                            }
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
                            dbUsers.child("songUrl").setValue(mSong.getUrl());
                            dbUsers.child("author").setValue(mSong.getAuthors());
                            dbUsers.child("feats").setValue(mSong.getFeats());
                            dbUsers.child("title").setValue(mSong.getTitle());
                            progresso = mPlayer.getCurrentPosition();
                            dbUsers.child("position").setValue(progresso);
                            initializeMusicUI();
                        } else if (finaleSin) {
                            //vibra per 200 millisecondi
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vib.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vib.vibrate(200);
                            }
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
                            dbUsers.child("songUrl").setValue(mSong.getUrl());
                            dbUsers.child("author").setValue(mSong.getAuthors());
                            dbUsers.child("feats").setValue(mSong.getFeats());
                            dbUsers.child("title").setValue(mSong.getTitle());
                            progresso = mPlayer.getCurrentPosition();
                            dbUsers.child("position").setValue(progresso);
                            initializeMusicUI();
                        }
                    } else {
                        Thread volumeThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    volumeBar.setVisibility(View.VISIBLE);
                                    Toast.makeText(getApplicationContext(), "Volume alzato", Toast.LENGTH_SHORT).show();
                                    Thread.sleep(1000);
                                    volumeBar.setVisibility(View.INVISIBLE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        volumeThread.start();
                    }
                }
                //reinizializzo le variabili
                middleY = -1;
                middleX = -1;
                startX = -1;
                startY = -1;
                endX = -1;
                endY = -1;
                finaleSin = false;
                finaleDes = false;
                medium = false;
                return true;
            case (MotionEvent.ACTION_CANCEL):
            case (MotionEvent.ACTION_OUTSIDE):
                return true;
            case (MotionEvent.ACTION_POINTER_DOWN):
                if (motionEvent.getPointerCount() > 1) {
                    startFingerY1 = motionEvent.getY(0);
                    startFingerY2 = motionEvent.getY(1);
                }
            case (MotionEvent.ACTION_POINTER_UP):
                if (motionEvent.getPointerCount() > 1) {
                    endFingerY1 = motionEvent.getY(0);
                    endFingerY2 = motionEvent.getY(1);
                    if (endFingerY1 - startFingerY1 >= SOGLIA_MIN_DUE_DITA && endFingerY2 - startFingerY2 >= SOGLIA_MIN_DUE_DITA) {
                        //GESTIONE PLAY E PAUSE
                        //Pause:
                        if (mPlayer.isPlaying()) {
                            Toast.makeText(getApplicationContext(), "Pausa", Toast.LENGTH_SHORT).show();
                            //vibra per 200 millisecondi
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vib.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vib.vibrate(300);
                            }
                            mPlayer.pause();
                            mPlay.setImageResource(R.drawable.ic_play);
                            onTrackPause();
                            Boolean b = Boolean.FALSE;
                            dbUsers.child("isSharing").setValue(b);
                            dbUsers.child("author").setValue(mSong.getAuthors());
                            dbUsers.child("feats").setValue(mSong.getFeats());
                            dbUsers.child("title").setValue(mSong.getTitle());
                            dbUsers.child("position").setValue(progresso);
                        } else {
                            Toast.makeText(getApplicationContext(), "Play", Toast.LENGTH_SHORT).show();
                            //vibra per 200 millisecondi
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vib.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vib.vibrate(200);
                            }
                            //Play:
                            mPlayer.start();
                            onTrackPlay();
                            Boolean b = Boolean.TRUE;
                            dbUsers.child("isSharing").setValue(b);
                            dbUsers.child("songUrl").setValue(mSong.getUrl());
                            dbUsers.child("author").setValue(mSong.getAuthors());
                            dbUsers.child("feats").setValue(mSong.getFeats());
                            dbUsers.child("title").setValue(mSong.getTitle());
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
                                        progresso = mPlayer.getCurrentPosition();
                                        dbUsers.child("position").setValue(progresso);
                                    }
                                    mHandler.postDelayed(this, 1000);
                                }
                            });
                            //Cambio l'icona da Play a Pause:
                            mPlay.setImageResource(R.drawable.ic_pause);
                        }
                    }
                }
            default:
                return super.onTouchEvent(motionEvent);
        }
    }
}