package ml.luiggi.sharingsongfy.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import ml.luiggi.sharingsongfy.MainPageActivity;
import ml.luiggi.sharingsongfy.R;
import ml.luiggi.sharingsongfy.scaffoldings.Song;
import ml.luiggi.sharingsongfy.utils.SongListAdapter;
import ml.luiggi.sharingsongfy.utils.JsonParserUrl;
//Costanti private
import static ml.luiggi.sharingsongfy.utils.Constants.*;
/*
 * Questa classe rappresenta il frammento della pagina principale, ossia quello contenente il Recycler View con l'elenco di tutte le canzoni presenti nel server
 */
public class FragmentHome extends Fragment {
    //lista di canzoni da riempire poi prelevandole dal server
    private ArrayList<Song> songList;
    private RecyclerView recyclerView;
    //Vista pubblica poichè usata anche in altre funzioni.
    public View bkView;
    public static boolean result = false;
    private TextView title;
    private View.OnClickListener listener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bkView = inflater.inflate(R.layout.fragment_home, container, false);
        if (result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Per poter utilizzare questa feature è necessario autorizzare l'accesso ai contatti.")
                    .setTitle("Attenzione")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            AlertDialog ad = builder.create();
            ad.show();
            result = false;
        }
        //carico canzoni
        initSongs();
        return bkView;
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
        JsonParserUrl mp = new JsonParserUrl(SERVER_LINK);
        songList = mp.getSongs();
        if (getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    initHomeFragment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Funzione che inizializza il fragment con il recyclerView
    private void initHomeFragment() {
        //riferimento all'oggetto
        recyclerView = (RecyclerView) bkView.findViewById(R.id.songList);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(bkView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        RecyclerView.Adapter mAdapter = new SongListAdapter(songList);
        recyclerView.setAdapter(mAdapter);
        title = (TextView)bkView.findViewById(R.id.header_home);
        if(listener == null){
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getActivity() != null) {
                        ((MainPageActivity) getActivity()).storeDialogStatus(false);
                        ((MainPageActivity) getActivity()).initTutorial();
                    }
                }
            };
        }
        title.setOnClickListener(listener);
        initGestures();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGestures() {
        recyclerView = (RecyclerView) bkView.findViewById(R.id.songList);
        recyclerView.setOnTouchListener(new OnSwipeTouchListener(bkView.getContext()) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                Log.d("TAG", "SwipeLeft");
                loadFragment();
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                Log.d("TAG", "SwipeRight");
                //tutorial
                if (getActivity() != null) {
                    ((MainPageActivity) getActivity()).storeDialogStatus(false);
                    ((MainPageActivity) getActivity()).initTutorial();
                }
            }
        });
        //Gestisco le gestures per passare da un fragment all'altro
        //In più gestisco il tutorial

    }

    //funzione per caricare un fragment specifico
    public boolean loadFragment() {
        if (getActivity() == null)
            return true;
        BottomNavigationView navigationView = (BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.fragment_tue_playlist);
        ((MainPageActivity) getActivity()).changeFocus(R.id.fragment_tue_playlist);
        return ((MainPageActivity) getActivity()).onNavigationItemSelected(menuItem);
    }
}
