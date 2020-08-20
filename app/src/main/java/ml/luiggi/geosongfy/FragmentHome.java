package ml.luiggi.geosongfy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FragmentHome extends Fragment {
    private TextView homeTextView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    //lista di canzoni da riempire poi prelevandole dal server
    private ArrayList<Song> songList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_home,container,false);
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

        return v;
    }

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
