package ml.luiggi.geosongfy.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ml.luiggi.geosongfy.MainPageActivity;
import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.SongActivity;
import ml.luiggi.geosongfy.scaffoldings.Friend;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.utils.FriendListAdapter;
import ml.luiggi.geosongfy.utils.Iso2Phone;

public class FragmentPeople extends Fragment {
    private RecyclerView.Adapter mAdapter;
    public Button btn;
    private ArrayList<Friend> songListFriends;
    public View bkpView;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    //Qui salverò i contatti che hanno l'app installata
    ArrayList<Friend> registeredList, currentSelected;
    Set<Friend> allRegistered,contactList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_people, container, false);
        bkpView = v;
        initView(v);
        return v;
    }

    private void initView(final View v) {
        songListFriends = new ArrayList<>();
        contactList = new LinkedHashSet<>();
        registeredList = new ArrayList<>();
        allRegistered = new LinkedHashSet<>();
        currentSelected = new ArrayList<>();
        //Carico tutti i contatti e poi solo quelli registrati all'app
        getContactList();
        //prelevo solo i contatti registrati, togliendo quelli non registrati dalla lista dei contatti presa sopra
        getAllRegisteredUsers();
        initPlaylistFragment(v);
        for(int i=0;i<songListFriends.size();i++){
            songListFriends.get(i).setCurrentSong(SongActivity.mSong);
            songListFriends.get(i).setSongPosition((int) SongActivity.progresso);
        }
    }

    static int condividi = 0;
    //Funzione che inizializza il fragment con il recyclerView
    private void initPlaylistFragment(final View v) {
        //riferimento all'oggetto
        recyclerView = (RecyclerView) v.findViewById(R.id.songListFriends);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        mLayoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        mAdapter = new FriendListAdapter(songListFriends);
        recyclerView.setAdapter(mAdapter);
        btn = v.findViewById(R.id.crea_podcast);
        if(condividi == 0){
            btn.setText(R.string.condividi_musica);
        }else{
            btn.setText(R.string.ferma_condivisione);
        }
        //TODO Aggiustare roba non funzionante
        btn.setOnClickListener(new View.OnClickListener() {
            DatabaseReference dbUsers;
            @Override
            public void onClick(View view) {
                dbUsers = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("isSharing");
                if(condividi == 0){
                    Boolean b = Boolean.TRUE;
                    dbUsers.setValue(b);
                    condividi=1;
                    btn.setText(R.string.ferma_condivisione);
                    //mAdapter.notifyDataSetChanged();
                }else{
                    Boolean b = Boolean.FALSE;
                    dbUsers.setValue(b);
                    condividi=0;
                    btn.setText(R.string.condividi_musica);
                    //mAdapter.notifyDataSetChanged();
                }
            }
        });
        initGestures(v);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void initGestures(View v) {
        recyclerView = v.findViewById(R.id.songListFriends);
        recyclerView.setOnTouchListener(new OnSwipeTouchListener(v.getContext()){
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                Log.d("TAG","SwipeLeft");
                //non fa nulla
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                Log.d("TAG","SwipeRight");
                loadFragment(); //playlist
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
        ((MainPageActivity) getActivity()).changeFocus(R.id.fragment_tue_playlist);

        return ((MainPageActivity)getActivity()).onNavigationItemSelected(menuItem);
    }

    private String getCountryISO() {
        String iso = null;

        TelephonyManager tmngr = (TelephonyManager) getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
        if (tmngr.getNetworkCountryIso() != null) {
            if (!tmngr.getNetworkCountryIso().toString().equals(""))
                iso = tmngr.getNetworkCountryIso();
        }
        return Iso2Phone.getPhone(iso);
    }

    //funzione che prende tutta la lista dei miei contatti
    private void getContactList() {
        String ISOPrefix = getCountryISO();

        Cursor phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String curName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String curNumb = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            curNumb = curNumb.replace(" ", "");
            curNumb = curNumb.replace("-", "");
            curNumb = curNumb.replace("(", "");
            curNumb = curNumb.replace(")", "");

            if (!String.valueOf(curNumb.charAt(0)).equals("+"))
                curNumb = ISOPrefix + curNumb;
            Friend mContacts = new Friend("", curName, curNumb, "");
            contactList.add(mContacts);
        }
    }
    private void getAllRegisteredUsers() {
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");
        ValueEventListener friendListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //songListFriends.clear();
                for (DataSnapshot ss : snapshot.getChildren()) {
                    String friend = ss.child("phone").getValue(String.class);
                    Boolean sharing = ss.child("isSharing").getValue(Boolean.class);
                    String songUrl = ss.child("songUrl").getValue(String.class);
                    Long position = ss.child("position").getValue(Long.class);
                    if(sharing){
                        Friend actFr = new Friend(friend);
                        actFr.setUid(ss.getKey());
                        actFr.setSongPosition(Math.toIntExact(position));
                        Song curSong = new Song("","","","",songUrl);
                        String name;
                        actFr.setCurrentSong(curSong);
                        if(contactList.contains(actFr)) {
                            Iterator<Friend> iter = contactList.iterator();
                            while(iter.hasNext()){
                                Friend tmp = iter.next();
                                if(tmp.equals(actFr)){
                                    actFr.setName(tmp.getName());
                                    break;
                                }
                            }
                            contactList.remove(actFr);
                            contactList.add(actFr);
                        }
                        if(!actFr.getUid().equals(FirebaseAuth.getInstance().getUid()))
                            allRegistered.add(actFr);
                        //Toast.makeText(getContext(),allRegistered.,Toast.LENGTH_LONG).show();
                    }

                }
                contactList.retainAll(allRegistered);
                recyclerView.setLayoutManager(mLayoutManager);
                //imposto un adapter per i dati della recycler view
                //Metto nella lista solo i contatti che stanno condividendo
                if(songListFriends.isEmpty()){
                    songListFriends.addAll(contactList);
                }
                mAdapter = new FriendListAdapter(songListFriends);
                recyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        userDB.addValueEventListener(friendListener);
    }
}
