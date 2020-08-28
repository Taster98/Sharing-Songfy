package ml.luiggi.geosongfy.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import ml.luiggi.geosongfy.MainPageActivity;
import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.utils.SongListAdapter;
import ml.luiggi.geosongfy.utils.JsonParserUrl;
/*
 * Questa classe rappresenta il frammento della pagina principale, ossia quello contenente il Recycler View con l'elenco di tutte le canzoni presenti nel server
 */
public class FragmentHome extends Fragment {
    //lista di canzoni da riempire poi prelevandole dal server
    private ArrayList<Song> songList;
    //Prendo il layout per gestirci le gestures
    RecyclerView recyclerView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
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
                try {
                    loadSongs(v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //funzione che parsa dal server il file json contenente l'elenco di canzoni presenti in esso
    private void loadSongs(final View v) {
        songList = new ArrayList<>();
        JsonParserUrl mp = new JsonParserUrl("http://luiggi.altervista.org/song_db.json");
        songList = mp.getSongs();
        if(getActivity() == null)
            return;
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
    private void initHomeFragment(View v) {
        //riferimento all'oggetto
        recyclerView = (RecyclerView) v.findViewById(R.id.songList);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        RecyclerView.Adapter mAdapter = new SongListAdapter(songList);
        recyclerView.setAdapter(mAdapter);
        initGestures(v);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGestures(View v) {
        recyclerView = v.findViewById(R.id.songList);
        recyclerView.setOnTouchListener(new OnSwipeTouchListener(v.getContext()){
            Fragment mFragment = null;
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                Log.d("TAG","SwipeLeft");
                loadFragment();
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                Log.d("TAG","SwipeRight");
                //non fa niente
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
        MenuItem menuItem = menu.findItem(R.id.fragment_tue_playlist);
        ((MainPageActivity)getActivity()).changeFocus(R.id.fragment_tue_playlist);
        return ((MainPageActivity)getActivity()).onNavigationItemSelected(menuItem);
    }
}
