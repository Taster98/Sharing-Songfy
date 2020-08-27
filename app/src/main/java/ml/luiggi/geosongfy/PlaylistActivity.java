package ml.luiggi.geosongfy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ml.luiggi.geosongfy.scaffoldings.Playlist;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.utils.DialogListAdapter;
import ml.luiggi.geosongfy.utils.JsonParserUrl;
import ml.luiggi.geosongfy.utils.SongListAdapter;

public class PlaylistActivity extends AppCompatActivity {
    private ArrayList<Song> songList;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Playlist> newPlaylists;
    private ArrayList<Song> allSongs;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //newPlaylists = (ArrayList<Playlist>) getIntent().getSerializableExtra("allPlaylists");
        loadPlaylists();
        Collections.sort(newPlaylists, new Comparator<Playlist>() {
            @Override
            public int compare(Playlist lhs, Playlist rhs) {
                return lhs.getPlaylistName().compareTo(rhs.getPlaylistName());
            }
        });
        setContentView(R.layout.fragment_home);
        initUI();
        initSongs();
    }

    @Override
    protected void onResume() {
        //
        super.onResume();
        loadPlaylists();
        Collections.sort(newPlaylists, new Comparator<Playlist>() {
            @Override
            public int compare(Playlist lhs, Playlist rhs) {
                return lhs.getPlaylistName().compareTo(rhs.getPlaylistName());
            }
        });
        initSongs();
        mAdapter.notifyDataSetChanged();

    }

    private void initUI() {
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }

    //Funzione che carica tutte le canzoni
    private void initSongs() {

        Playlist curPlaylist = (Playlist) getIntent().getSerializableExtra("playlistSelected");
        Playlist realCur = newPlaylists.get(newPlaylists.indexOf(curPlaylist));
        songList = (ArrayList<Song>) realCur.getSongList();
        initAllSongs();
        //riferimento all'oggetto
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.songList);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        mAdapter = new SongListAdapter(songList,curPlaylist);
        recyclerView.setAdapter(mAdapter);
    }

    //Bottone indietro
    @Override
    public boolean onSupportNavigateUp() {
        savePlaylists();
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        savePlaylists();
        Collections.sort(newPlaylists, new Comparator<Playlist>() {
            @Override
            public int compare(Playlist lhs, Playlist rhs) {
                return lhs.getPlaylistName().compareTo(rhs.getPlaylistName());
            }
        });
        super.onDestroy();
    }

    //Menù per la playlist
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.playlist_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Gestione bottoni menù
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.add_playlist) {
            addDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.scegli_canzoni, null);
        RecyclerView list = view.findViewById(R.id.song_list_dialog);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setHasFixedSize(true);
        final DialogListAdapter adapter = new DialogListAdapter(allSongs);
        list.setAdapter(adapter);
        final Playlist curPlaylist = (Playlist) getIntent().getSerializableExtra("playlistSelected");
        final Playlist remPlaylist = curPlaylist;
        final ArrayList<Song> actualSongs = (ArrayList<Song>) curPlaylist.getSongList();
        builder.setView(view)
                .setPositiveButton(R.string.aggiungi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Aggiungi canzoni
                        actualSongs.addAll(adapter.checkedList);
                        curPlaylist.setSongList(actualSongs);
                        newPlaylists.remove(curPlaylist);
                        newPlaylists.add(curPlaylist);
                        mAdapter.notifyDataSetChanged();
                        //salvare nello shared preferences
                        savePlaylists();
                        initSongs();
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

    //funzione che salva le playlists
    private void savePlaylists() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("saved_playlists", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(newPlaylists);
        editor.putString("playlist_list", json);
        editor.apply();
    }

    //funzione che ricarica le playlist salvate
    private void loadPlaylists() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("saved_playlists", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("playlist_list", null);
        Type type = new TypeToken<ArrayList<Playlist>>() {
        }.getType();
        newPlaylists = gson.fromJson(json, type);
    }

    //Funzione che carica tutte le canzoni
    private void initAllSongs() {
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
        allSongs = new ArrayList<>();
        JsonParserUrl mp = new JsonParserUrl("http://luiggi.altervista.org/song_db.json");
        allSongs = mp.getSongs();
    }
}
