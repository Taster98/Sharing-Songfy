package ml.luiggi.sharingsongfy.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import ml.luiggi.sharingsongfy.MainPageActivity;
import ml.luiggi.sharingsongfy.R;
import ml.luiggi.sharingsongfy.SongActivity;
import ml.luiggi.sharingsongfy.scaffoldings.Friend;
import ml.luiggi.sharingsongfy.scaffoldings.Song;
import ml.luiggi.sharingsongfy.utils.FriendListAdapter;
import ml.luiggi.sharingsongfy.utils.Iso2Phone;

public class FragmentPeople extends Fragment {

    private RecyclerView.Adapter mAdapter;
    public Button btn;
    private ArrayList<Friend> songListFriends;
    public View bkpView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Set<Friend> allRegistered, contactList;
    private TextView emptyList;
    //variabile per la gestione del tasto condividi
    static int condividi = 0;
    //listeners
    private ValueEventListener friendListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bkpView = inflater.inflate(R.layout.fragment_people, container, false);
        //inizializzo la vista del fragment
        initView();
        return bkpView;
    }

    private void initView() {
        //Inizializzo innanzitutto la lista degli amici che condividono e i due insiemi
        //(che conterranno tutti i contatti e tutti gli utenti registrati)
        if(songListFriends == null)
            songListFriends = new ArrayList<>();
        if(contactList == null)
            contactList = new LinkedHashSet<>();
        if(allRegistered == null)
            allRegistered = new LinkedHashSet<>();
        //inizializzo il fragment
        initPlaylistFragment();
        //Carico tutti i contatti della mia rubrica
        getContactList();
        //prelevo solo i contatti registrati, togliendo quelli non registrati dalla lista dei contatti presa sopra
        getAllRegisteredUsers();

        for (int i = 0; i < songListFriends.size(); i++) {
            songListFriends.get(i).setCurrentSong(SongActivity.mSong);
            songListFriends.get(i).setSongPosition((int) SongActivity.progresso);
        }
    }

    //Funzione che inizializza il fragment con il recyclerView
    private void initPlaylistFragment() {
        //riferimento al recycler view
        recyclerView = (RecyclerView) bkpView.findViewById(R.id.songListFriends);
        //imposto la dimensione del layout come fissata
        recyclerView.setHasFixedSize(true);
        //controllo che non siano già stati impostati il layoutmanager e l'adapter, altrimenti lo faccio
        //imposto un layout manager per la recycler view
        if (mLayoutManager == null)
            mLayoutManager = new LinearLayoutManager(bkpView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        if (mAdapter == null)
            mAdapter = new FriendListAdapter(songListFriends);
        recyclerView.setAdapter(mAdapter);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            int check=0;
            private void checkChange(){
                check++;
                if(check>1){
                    reloadFragment();
                }
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

            @Override
            public void onChanged() {
                super.onChanged();
                checkChange();
            }
        });
        //riferisco ora il bottone per condividere la musica
        btn = (Button)bkpView.findViewById(R.id.crea_podcast);
        //controllo in che stato è il tasto condivisione, e a seconda di ciò ne cambio il test
        if (condividi == 0) {
            btn.setText(R.string.condividi_musica);
        } else {
            btn.setText(R.string.ferma_condivisione);
        }
        //imposto un listener ora per il bottone della condivisione
        btn.setOnClickListener(new View.OnClickListener() {
            //intanto creo un riferimento al database utenti
            DatabaseReference dbUsers;

            @Override
            public void onClick(View view) {
                //FirebaseAuth.getInstance().getUid() non può essere null in quanto questo fragment
                //è accessibile solamente da chi ha effettuato l'autenticazione con numero di telefono
                assert FirebaseAuth.getInstance().getUid() != null;
                //quindi creo un riferimento al database nella mia posizione, per settare a true o false il valore isSharing, che permette di capire se sto
                //condividendo musica o no
                dbUsers = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("isSharing");
                //Imposto il valore booleano e cambio il layout di conseguenza
                if (condividi == 0) {
                    Boolean b = Boolean.TRUE;
                    dbUsers.setValue(b);
                    condividi = 1;
                    btn.setText(R.string.ferma_condivisione);
                } else {
                    Boolean b = Boolean.FALSE;
                    dbUsers.setValue(b);
                    condividi = 0;
                    btn.setText(R.string.condividi_musica);
                }
            }
        });
        //inizializzo le gestures
        initGestures();
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
            MenuItem menuItem = menu.findItem(R.id.fragment_people);
            //uso i metodi implementati nel mainpageactivity
            ((MainPageActivity) getActivity()).changeFocus(R.id.fragment_people);
            ((MainPageActivity) getActivity()).onNavigationItemSelected(menuItem);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGestures() {
        //imposto un listener sul touch del recycler view, per poter scorrere i menù con dei semplici swipe
        recyclerView.setOnTouchListener(new OnSwipeTouchListener(bkpView.getContext()) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                //non fa nulla
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                loadFragment(); //carica fragment playlist
            }
        });
    }

    //funzione per caricare un fragment specifico
    public boolean loadFragment() {
        //se l'activity risulta null, per evitare NPE ritorno true
        if (getActivity() == null)
            return true;
        //altrimenti, come sopra, carico il fragment precedente (quello delle playlist)
        BottomNavigationView navigationView = (BottomNavigationView)getActivity().findViewById(R.id.bottom_navigation);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.fragment_tue_playlist);
        ((MainPageActivity) getActivity()).changeFocus(R.id.fragment_tue_playlist);

        return ((MainPageActivity) getActivity()).onNavigationItemSelected(menuItem);
    }

    //funzione che preleva l'ISO del paese di provenienza del numero telefonico.
    private String getCountryISO() {
        String iso = "";
        //posso asserire che l'activity sia caricata ed esista
        assert getActivity() != null;
        TelephonyManager tmngr = (TelephonyManager) getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
        if (tmngr.getNetworkCountryIso() != null) {
            if (!tmngr.getNetworkCountryIso().equals(""))
                iso = tmngr.getNetworkCountryIso();
        }
        return Iso2Phone.getPhone(iso);
    }

    //funzione che prende tutta la lista dei miei contatti
    private void getContactList() {
        //Mi salvo il prefisso (per nazionalità differenti)
        String ISOPrefix = getCountryISO();
        //controllo che l'activity non sia vuota altrimenti NullPointerException
        if (getActivity() != null) {
            //Creo un cursore per poter scorrere la lista di contatti (con un contentResolver)
            Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            //Scorro quindi finchè ho contatti
            while (phones.moveToNext()) {
                //Salvo nome in rubrica e numero
                String curName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String curNumb = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                //Rimuovo eventuali simboli strani contenuti nel numero
                curNumb = curNumb.replace(" ", "");
                curNumb = curNumb.replace("-", "");
                curNumb = curNumb.replace("(", "");
                curNumb = curNumb.replace(")", "");
                //Se il numero non inizia con + allora il prefisso manca; lo aggiungo
                if (!String.valueOf(curNumb.charAt(0)).equals("+"))
                    curNumb = ISOPrefix + curNumb;
                //Mi salvo un nuovo Friend per ora solo con nome e numero
                Friend mContacts = new Friend("", curName, curNumb);
                //Aggiungo il contatto all'insieme dei contatti
                contactList.add(mContacts);
            }
        }
    }

    private void getAllRegisteredUsers() {
        //Creo un riferimento al database su firebase
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");
        //Creo ora un listener per la gestione dell'aggiornamento dei dati nel database
        if(friendListener == null) {
            friendListener = new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Scorro ogni id degli utenti registrati
                    for (DataSnapshot ss : snapshot.getChildren()) {
                        //prelevo i dati che mi servono
                        String friend = ss.child("phone").getValue(String.class);
                        Boolean sharing = ss.child("isSharing").getValue(Boolean.class);
                        String songUrl = ss.child("songUrl").getValue(String.class);
                        String title = ss.child("title").getValue(String.class);
                        String authors = ss.child("author").getValue(String.class);
                        String feats = ss.child("feats").getValue(String.class);
                        Long position = ss.child("position").getValue(Long.class);
                        //mi assicuro che non siano null
                        if (sharing != null && position != null && sharing) {
                            //creo un nuovo amico (prendendo il numero di telefono dal db)
                            Friend actFr = new Friend(friend);
                            //cambio l'uid con quello di firebase
                            actFr.setUid(ss.getKey());
                            //aggiorno la posizione
                            actFr.setSongPosition(Math.toIntExact(position));
                            //TODO titolo, autore, feat
                            Song curSong = new Song(title, "", authors, feats, songUrl);
                            //imposto quindi la nuova canzone corrente
                            actFr.setCurrentSong(curSong);
                            //a questo punto, se actFr (della lista dei registrati) è anche
                            //presente nella rubrica, allora posso prelevarne il nome
                            if (contactList.contains(actFr)) {
                                //dato che è un insieme lo devo cercare, appena lo trovo esco
                                for (Friend tmp : contactList) {
                                    if (tmp.equals(actFr)) {
                                        actFr.setName(tmp.getName());
                                        break;
                                    }
                                }
                                //posso quindi togliere l'amico dalla contact list (in quanto non avrebbe l'uid)
                                contactList.remove(actFr);
                                //e inserisco quello aggiornato
                                contactList.add(actFr);
                            }
                            //actFr è un utente registrato, ma ovviamente devo escludervi me stesso
                            //(questo perchè potrei avere il mio stesso numero salvato in rubrica,
                            //e con questo controllo evito crash)
                            if (!actFr.getUid().equals(FirebaseAuth.getInstance().getUid()))
                                allRegistered.add(actFr);
                        }

                    }
                    //Ora aggiorno la mia contactList prendendo solamente gli utenti che sono registrati e che ho in rubrica
                    contactList.retainAll(allRegistered);
                    //pulisco la lista di amici precedentemente creata
                    songListFriends.clear();
                    //la aggiorno
                    songListFriends.addAll(contactList);
                    //notifico il mio adapter che dei dati sono stati cambiati, per aggiornare la recycler View
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
        }
        //aggiungo il listener al database utenti
        userDB.addValueEventListener(friendListener);
    }
}