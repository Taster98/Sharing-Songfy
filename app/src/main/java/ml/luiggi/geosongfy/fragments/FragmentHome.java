package ml.luiggi.geosongfy.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.utils.SongListAdapter;
import ml.luiggi.geosongfy.utils.JsonParserUrl;

/*
* Questa classe rappresenta il frammento della pagina principale, ossia quello contenente il Recycler View con l'elenco di tutte le canzoni presenti nel server
*/
public class FragmentHome extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //lista di canzoni da riempire poi prelevandole dal server
    private ArrayList<Song> songList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_home,container,false);
        //carico canzoni
        initSongs(v);
        return v;
    }
    //Funzione che carica tutte le canzoni
    private void initSongs(final View v) {
        //Uso un thread per non intasare l'UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    loadSongs(v);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //funzione che parsa dal server il file json contenente l'elenco di canzoni presenti in esso
    private void loadSongs(final View v){
        songList = new ArrayList<>();
        JsonParserUrl mp = new JsonParserUrl("http://luiggi.altervista.org/song_db.json");
        songList = mp.getSongs();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    initHomeFragment(v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //Funzione che inizializza il fragment con il recyclerView
    private void initHomeFragment(View v){
        //riferimento all'oggetto
        recyclerView = (RecyclerView)v.findViewById(R.id.songList);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        mLayoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        mAdapter = new SongListAdapter(songList);
        recyclerView.setAdapter(mAdapter);

    }
}
