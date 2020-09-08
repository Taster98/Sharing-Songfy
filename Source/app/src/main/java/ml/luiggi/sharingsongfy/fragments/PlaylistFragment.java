package ml.luiggi.sharingsongfy.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import ml.luiggi.sharingsongfy.MainPageActivity;
import ml.luiggi.sharingsongfy.R;
import ml.luiggi.sharingsongfy.scaffoldings.Playlist;
import ml.luiggi.sharingsongfy.scaffoldings.Song;
import ml.luiggi.sharingsongfy.utils.DialogListAdapter;
import ml.luiggi.sharingsongfy.utils.JsonParserUrl;
import ml.luiggi.sharingsongfy.utils.PlayListAdapter;
//costanti private
import static ml.luiggi.sharingsongfy.utils.Constants.*;

public class PlaylistFragment extends Fragment {
    private RecyclerView.Adapter mAdapter;
    public Button btn;
    public EditText editText;
    private ArrayList<Playlist> playlistList;
    private ArrayList<Song> songList;
    public String curName;
    public View bkpView;
    private RecyclerView recyclerView;
    private TextView emptyList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bkpView = inflater.inflate(R.layout.fragment_tue_playlist, container, false);
        initView();
        return bkpView;
    }

    private void initView() {
        //Caricare dallo shared preferences
        loadPlaylists();
        //se è null vuol dire che è la prima volta che apro l'app, non ho playlist
        if (playlistList == null)
            playlistList = new ArrayList<>();
        //A questo punto posso ordinarle in ordine alfabetico
        //inizializzo il fragment
        initPlaylistFragment();
        //inizializzo le canzoni
        initSongs();
        //imposto un listener al bottone per creare una playlist (mostro un dialog):
        btn = (Button)bkpView.findViewById(R.id.crea_playlist);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getLayoutInflater();
                //Fornisco il mio layout customizzato
                final View view1 = inflater.inflate(R.layout.nuova_playlist, null);
                builder.setView(view1)
                        .setPositiveButton(R.string.crea, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                editText = (EditText)view1.findViewById(R.id.nuovo_nome_playlist);
                                if (editText.getText().toString().equals(""))
                                    Toast.makeText(getContext(), "Devi inserire un nome valido per la Playlist!", Toast.LENGTH_SHORT).show();
                                else {
                                    curName = editText.getText().toString();
                                    scegliCanzoni();
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
        //Testo da mostrare se la lista è vuota
        emptyList = (TextView)bkpView.findViewById(R.id.emptySongs);
        if(mAdapter.getItemCount() == 0)
            emptyList.setVisibility(View.VISIBLE);
        else{
            emptyList.setVisibility(View.INVISIBLE);
        }
    }

    private void scegliCanzoni() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.scegli_canzoni, null);
        RecyclerView list = (RecyclerView)view.findViewById(R.id.song_list_dialog);
        list.setLayoutManager(new LinearLayoutManager(bkpView.getContext()));
        list.setHasFixedSize(true);
        final DialogListAdapter adapter = new DialogListAdapter(songList);
        list.setAdapter(adapter);
        builder.setView(view)
                .setPositiveButton(R.string.aggiungi, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Aggiungi canzoni
                        Playlist curPlaylist = new Playlist(curName, adapter.checkedList);
                        playlistList.add(curPlaylist);
                        Toast.makeText(getContext(), "Playlist " + curName + " creata con successo!", Toast.LENGTH_SHORT).show();
                        mAdapter.notifyDataSetChanged();
                        reloadFragment();
                        //salvare nello shared preferences
                        savePlaylists();
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
    //funzione con la quale ricarico il fragment attuale
    private void reloadFragment() {
        //mi assicuro che l'activity non sia null altrimenti NullPointerException
        if (getActivity() != null) {
            //Per poter fare in modo che tutto il fragment si aggiorni senza "rompere" il bottomNavMenu,
            //mi ci creo un riferimento e controllo anche quello (per lo stato di active/unactive di ciascun
            //fragment relativo al suo menù)
            BottomNavigationView navigationView = (BottomNavigationView)getActivity().findViewById(R.id.bottom_navigation);
            Menu menu = navigationView.getMenu();
            MenuItem menuItem = menu.findItem(R.id.fragment_tue_playlist);
            //uso i metodi implementati nel mainpageactivity
            ((MainPageActivity) getActivity()).changeFocus(R.id.fragment_tue_playlist);
            ((MainPageActivity) getActivity()).onNavigationItemSelected(menuItem);
        }
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
    }

    //Funzione che inizializza il fragment con il recyclerView
    private void initPlaylistFragment() {
        //riferimento all'oggetto
        recyclerView = (RecyclerView) bkpView.findViewById(R.id.playlistList);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(bkpView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        mAdapter = new PlayListAdapter(playlistList);
        recyclerView.setAdapter(mAdapter);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            private void checkChange(){
                //Testo da mostrare se la lista è vuota
                emptyList = (TextView)bkpView.findViewById(R.id.emptySongs);
                if(mAdapter.getItemCount() == 0)
                    emptyList.setVisibility(View.VISIBLE);
                else{
                    emptyList.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                checkChange();
            }
        });
        initGestures();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGestures() {
        recyclerView = (RecyclerView)bkpView.findViewById(R.id.playlistList);
        recyclerView.setOnTouchListener(new OnSwipeTouchListener(bkpView.getContext()) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                loadFragment(0); //amici
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                loadFragment(1); //home
            }
        });
        //Gestisco le gestures per passare da un fragment all'altro
    }

    //funzione per caricare un fragment specifico
    public boolean loadFragment(int i) {
        if (getActivity() == null)
            return true;
        BottomNavigationView navigationView = (BottomNavigationView)getActivity().findViewById(R.id.bottom_navigation);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem;
        if (i == 1) {
            menuItem = menu.findItem(R.id.home);
            ((MainPageActivity) getActivity()).changeFocus(R.id.home);
        } else {
            menuItem = menu.findItem(R.id.fragment_people);
            ((MainPageActivity) getActivity()).changeFocus(R.id.fragment_people);
        }
        return ((MainPageActivity) getActivity()).onNavigationItemSelected(menuItem);
    }

    //funzione che salva le playlists
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void savePlaylists(){
        Gson gson = new Gson();
        String json = gson.toJson(playlistList);
        String filename = "saved_playlists.txt";
        try {
            FileOutputStream fos = getActivity().openFileOutput(filename,Context.MODE_PRIVATE);
            fos.write(json.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        reloadFragment();
    }
    //funzione che ricarica le playlist salvate
    private void loadPlaylists(){
        String filename = "saved_playlists.txt";
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Playlist>>() {
        }.getType();
        try {
            FileInputStream fis = getActivity().openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader streamReader = new BufferedReader(isr);

            StringBuilder json = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                json.append(inputStr);

            playlistList = gson.fromJson(String.valueOf(json),type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (playlistList == null)
            playlistList = new ArrayList<>();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlaylists();
        initPlaylistFragment();
        mAdapter.notifyDataSetChanged();
    }
}
