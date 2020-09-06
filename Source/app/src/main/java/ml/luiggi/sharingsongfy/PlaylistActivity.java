package ml.luiggi.sharingsongfy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import ml.luiggi.sharingsongfy.scaffoldings.Playlist;
import ml.luiggi.sharingsongfy.scaffoldings.Song;
import ml.luiggi.sharingsongfy.utils.DialogListAdapter;
import ml.luiggi.sharingsongfy.utils.JsonParserUrl;
import ml.luiggi.sharingsongfy.utils.SongListAdapter;
//costanti private
import static ml.luiggi.sharingsongfy.utils.Constants.*;

public class PlaylistActivity extends AppCompatActivity {
    private ArrayList<Song> songList;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Playlist> newPlaylists;
    private ArrayList<Song> allSongs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPlaylists();
        setContentView(R.layout.fragment_home);
        initUI();
        initSongs();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        //
        super.onResume();
        loadPlaylists();
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
        mAdapter = new SongListAdapter(songList, curPlaylist);
        recyclerView.setAdapter(mAdapter);
    }

    //Bottone indietro
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onSupportNavigateUp() {
        savePlaylists();
        finish();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onDestroy() {
        savePlaylists();
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

        if (id == R.id.add_playlist) {
            addDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.scegli_canzoni, null);
        RecyclerView list = (RecyclerView)view.findViewById(R.id.song_list_dialog);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setHasFixedSize(true);
        final DialogListAdapter adapter = new DialogListAdapter(allSongs);
        list.setAdapter(adapter);
        final Playlist curPlaylist = (Playlist) getIntent().getSerializableExtra("playlistSelected");
        final ArrayList<Song> actualSongs = (ArrayList<Song>) curPlaylist.getSongList();
        builder.setView(view)
                .setPositiveButton(R.string.aggiungi, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Aggiungi canzoni
                        actualSongs.addAll(adapter.checkedList);
                        curPlaylist.setSongList(actualSongs);
                        newPlaylists.remove(curPlaylist);
                        newPlaylists.add(curPlaylist);
                        //salvare nello shared preferences
                        savePlaylists();
                        initSongs();
                        mAdapter.notifyDataSetChanged();
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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void savePlaylists(){
        Gson gson = new Gson();
        String json = gson.toJson(newPlaylists);
        String filename = "saved_playlists.txt";
        try {
            FileOutputStream fos = this.openFileOutput(filename,Context.MODE_PRIVATE);
            fos.write(json.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //funzione che ricarica le playlist salvate
    private void loadPlaylists(){
        String filename = "saved_playlists.txt";
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Playlist>>() {
        }.getType();
        try {
            FileInputStream fis = this.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader streamReader = new BufferedReader(isr);

            StringBuilder json = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                json.append(inputStr);

            newPlaylists = gson.fromJson(String.valueOf(json),type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (newPlaylists == null)
            newPlaylists = new ArrayList<>();
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
        JsonParserUrl mp = new JsonParserUrl(SERVER_LINK);
        allSongs = mp.getSongs();
    }
}
