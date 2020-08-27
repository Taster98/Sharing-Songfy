package ml.luiggi.geosongfy.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Playlist;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.utils.DialogListAdapter;
import ml.luiggi.geosongfy.utils.JsonParserUrl;
import ml.luiggi.geosongfy.utils.PlayListAdapter;

public class PlaylistFragment extends Fragment {
    private RecyclerView.Adapter mAdapter;
    public Button btn;
    public EditText editText;
    public AudioManager audioManager;
    int currentVolume;
    private ArrayList<Playlist> playlistList;
    private ArrayList<Song> songList;
    public String curName;
    public View bkpView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tue_playlist, container, false);
        bkpView = v;
        initView(v);
        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            int halfWidth = getActivity().getWindow().getDecorView().getWidth() / 2;
                            if (e1.getX() < halfWidth) {
                                //lato sinistro: luminosità
                                cambiaLuminosita(e1, e2, velocityX, velocityY, SWIPE_MIN_DISTANCE, SWIPE_THRESHOLD_VELOCITY);
                            } else {
                                //lato destro: volume
                                cambiaVolume(e1, e2, velocityX, velocityY, SWIPE_MIN_DISTANCE, SWIPE_THRESHOLD_VELOCITY);
                            }
                            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                                Toast.makeText(getContext(), "DOWN TO UP - DESTRA", Toast.LENGTH_SHORT).show();
                                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                                Toast.makeText(getContext(), "UP TO DOWN - DESTRA", Toast.LENGTH_SHORT).show();
                                currentVolume = audioManager
                                        .getStreamVolume(AudioManager.STREAM_MUSIC);
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                        currentVolume - 1, 0);
                            }
                        } catch (Exception e) {
                            // nothing
                        }
                        return false;
                    }

                });
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });
        return v;
    }

    private void initView(final View v) {
        //Caricare dallo shared preferences
        loadPlaylists(v);
        if (playlistList == null)
            playlistList = new ArrayList<>();
        Collections.sort(playlistList, new Comparator<Playlist>() {
            @Override
            public int compare(Playlist lhs, Playlist rhs) {
                return lhs.getPlaylistName().compareTo(rhs.getPlaylistName());
            }
        });
        initPlaylistFragment(v);
        initSongs();
        btn = v.findViewById(R.id.crea_playlist);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getLayoutInflater();
                final View view1 = inflater.inflate(R.layout.nuova_playlist, null);
                builder.setView(view1)
                        .setPositiveButton(R.string.crea, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                editText = view1.findViewById(R.id.nuovo_nome_playlist);
                                if (editText.getText().toString().equals(""))
                                    Toast.makeText(getContext(), "Devi inserire un nome valido per la Playlist!", Toast.LENGTH_SHORT).show();
                                else {
                                    curName = editText.getText().toString();
                                    scegliCanzoni(v);
                                }
                            }
                        })
                        .setNegativeButton(R.string.annulla, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void scegliCanzoni(final View v) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.scegli_canzoni, null);
        RecyclerView list = view.findViewById(R.id.song_list_dialog);
        list.setLayoutManager(new LinearLayoutManager(v.getContext()));
        list.setHasFixedSize(true);
        final DialogListAdapter adapter = new DialogListAdapter(songList);
        list.setAdapter(adapter);
        builder.setView(view)
                .setPositiveButton(R.string.aggiungi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Aggiungi canzoni
                        Playlist curPlaylist = new Playlist(curName, adapter.checkedList);
                        playlistList.add(curPlaylist);
                        Toast.makeText(getContext(), "Playlist " + curName + " creata con successo!", Toast.LENGTH_SHORT).show();
                        mAdapter.notifyDataSetChanged();
                        //salvare nello shared preferences
                        savePlaylists(v);
                    }
                })
                .setNegativeButton(R.string.annulla, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        adapter.notifyDataSetChanged();
        dialog.show();
    }

    //Funzione che carica tutte le canzoni
    private void initSongs() {
        //Uso un thread per non intasare l'UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    loadSongs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //funzione che parsa dal server il file json contenente l'elenco di canzoni presenti in esso
    private void loadSongs() {
        songList = new ArrayList<>();
        JsonParserUrl mp = new JsonParserUrl("http://luiggi.altervista.org/song_db.json");
        songList = mp.getSongs();
    }

    //Funzione che inizializza il fragment con il recyclerView
    private void initPlaylistFragment(View v) {
        //riferimento all'oggetto
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.playlistList);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        mAdapter = new PlayListAdapter(playlistList);
        recyclerView.setAdapter(mAdapter);
    }

    private void cambiaVolume(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY, int swipe_min_distance, int swipe_threshold_velocity) {
        if (e1.getX() - e2.getX() > swipe_min_distance
                && Math.abs(velocityX) > swipe_threshold_velocity) {
            Toast.makeText(getContext(), "RIGHT TO LEFT - DESTRA", Toast.LENGTH_SHORT).show();
        } else if (e2.getX() - e1.getX() > swipe_min_distance
                && Math.abs(velocityX) > swipe_threshold_velocity) {
            Toast.makeText(getContext(), "LEFT TO RIGHT - DESTRA", Toast.LENGTH_SHORT).show();
        }
    }

    private void cambiaLuminosita(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY, int swipe_min_distance, int swipe_threshold_velocity) {
        if (e1.getY() - e2.getY() > swipe_min_distance
                && Math.abs(velocityY) > swipe_threshold_velocity) {
            Toast.makeText(getContext(), "DOWN TO UP - SINISTRA", Toast.LENGTH_SHORT).show();
        } else if (e2.getY() - e1.getY() > swipe_min_distance
                && Math.abs(velocityY) > swipe_threshold_velocity) {
            Toast.makeText(getContext(), "UP TO DOWN - SINISTRA", Toast.LENGTH_SHORT).show();
        }
    }

    //funzione che salva le playlists
    private void savePlaylists(View v) {
        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("saved_playlists", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(playlistList);
        editor.putString("playlist_list", json);
        editor.apply();

    }

    //funzione che ricarica le playlist salvate
    private void loadPlaylists(View v) {
        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("saved_playlists", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("playlist_list", null);
        Type type = new TypeToken<ArrayList<Playlist>>() {
        }.getType();
        playlistList = gson.fromJson(json, type);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlaylists(bkpView);
        Collections.sort(playlistList, new Comparator<Playlist>() {
            @Override
            public int compare(Playlist lhs, Playlist rhs) {
                return lhs.getPlaylistName().compareTo(rhs.getPlaylistName());
            }
        });
        initPlaylistFragment(bkpView);
        mAdapter.notifyDataSetChanged();
    }
}
