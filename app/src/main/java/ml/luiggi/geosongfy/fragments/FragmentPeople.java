package ml.luiggi.geosongfy.fragments;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Friend;
import ml.luiggi.geosongfy.utils.FriendListAdapter;
import ml.luiggi.geosongfy.utils.Iso2Phone;

public class FragmentPeople extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    ArrayList<Friend> contactList, registeredList, allRegistered, currentSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_people, container, false);
        initPeopleFragment(v);
        return v;
    }

    private void initPeopleFragment(View v) {
        contactList = new ArrayList<>();
        registeredList = new ArrayList<>();
        allRegistered = new ArrayList<>();
        currentSelected = new ArrayList<>();
        //riferimento alla recycler view
        recyclerView = (RecyclerView) v.findViewById(R.id.friendList);
        //associo il floating action button
        FloatingActionButton mFloatingActionButton = (FloatingActionButton) v.findViewById(R.id.floating_send);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        mLayoutManager = new LinearLayoutManager(v.getContext());
        //prelevo tutti i contatti
        getContactList();
        //prelevo solo i contatti registrati, togliendo quelli non registrati dalla lista dei contatti presa sopra
        getAllRegisteredUsers();
        //gestisco il click sul bottone
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((FriendListAdapter) mAdapter).checkedList.size() > 0) {
                    Toast.makeText(getContext(), "EUREKA", Toast.LENGTH_LONG).show();
                    //avvio la chat di gruppo con gli utenti selezionati

                } else {
                    Toast.makeText(getContext(), "Devi selezionare almeno un amico dalla lista", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String getCountryISO() {
        String iso = null;

        TelephonyManager tmngr = (TelephonyManager) getActivity().getApplicationContext().getSystemService(getActivity().getApplicationContext().TELEPHONY_SERVICE);
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
                for (DataSnapshot ss : snapshot.getChildren()) {
                    String friend = ss.child("phone").getValue(String.class);
                    allRegistered.add(new Friend(friend));
                    //Toast.makeText(getContext(),allRegistered.,Toast.LENGTH_LONG).show();
                }
                contactList.retainAll(allRegistered);
                Set<Friend> contactNoDupli = new LinkedHashSet<Friend>(contactList);
                contactList.clear();
                contactList.addAll(contactNoDupli);
                recyclerView.setLayoutManager(mLayoutManager);
                //imposto un adapter per i dati della recycler view
                mAdapter = new FriendListAdapter(contactList);
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
