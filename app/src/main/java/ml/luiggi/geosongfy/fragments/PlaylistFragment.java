package ml.luiggi.geosongfy.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ml.luiggi.geosongfy.MainPageActivity;
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
    private ArrayList<Playlist> playlistList;
    private ArrayList<Song> songList;
    public String curName;
    public View bkpView;
    RecyclerView recyclerView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tue_playlist, container, false);
        bkpView = v;
        initView(v);
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
        recyclerView = (RecyclerView) v.findViewById(R.id.playlistList);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        mAdapter = new PlayListAdapter(playlistList);
        recyclerView.setAdapter(mAdapter);
        initGestures(v);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void initGestures(View v) {
        recyclerView = v.findViewById(R.id.playlistList);
        recyclerView.setOnTouchListener(new OnSwipeTouchListener(v.getContext()){
            Fragment mFragment = null;
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                Log.d("TAG","SwipeLeft");
                //non fa niente
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                Log.d("TAG","SwipeRight");
                loadFragment();
            }
        });
        //Gestisco le gestures per passare da un fragment all'altro
    }
    //funzione per caricare un fragment specifico
    public boolean loadFragment() {
        if(getActivity() == null)
            return true;
        BottomNavigationView navigationView = getActivity().findViewById(R.id.bottom_navigation);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.home);
        ((MainPageActivity)getActivity()).changeFocus(R.id.home);
        return ((MainPageActivity)getActivity()).onNavigationItemSelected(menuItem);
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
        if(playlistList == null)
            playlistList = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlaylists(bkpView);
        if(playlistList != null) {
            Collections.sort(playlistList, new Comparator<Playlist>() {
                @Override
                public int compare(Playlist lhs, Playlist rhs) {
                    return lhs.getPlaylistName().compareTo(rhs.getPlaylistName());
                }
            });
        }
        initPlaylistFragment(bkpView);
        mAdapter.notifyDataSetChanged();
    }
}
