package ml.luiggi.geosongfy.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Friend;
import ml.luiggi.geosongfy.utils.DrawerLocker;
import ml.luiggi.geosongfy.utils.FriendListAdapter;
import ml.luiggi.geosongfy.utils.Iso2Phone;

public class FragmentPeople extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    ArrayList<Friend> contactList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_people, container, false);
        ((DrawerLocker) getActivity()).setDrawerEnabled(true);
        initPeopleFragment(v);
        return v;
    }

    private void initPeopleFragment(View v) {
        contactList = new ArrayList<>();
        //riferimento all'oggetto
        recyclerView = (RecyclerView) v.findViewById(R.id.friendList);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        mLayoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //imposto un adapter per i dati della recycler view
        mAdapter = new FriendListAdapter(contactList);
        recyclerView.setAdapter(mAdapter);
        getContactList();
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
            getUserDetails(mContacts);
        }
    }

    private void getUserDetails(Friend mContacts) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(mContacts.getPhoneNumber());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String phone = "", name = "";
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if (childSnapshot.child("phone").getValue() != null)
                            phone = childSnapshot.child("phone").getValue().toString();
                        if (childSnapshot.child("name").getValue() != null)
                            name = childSnapshot.child("name").getValue().toString();

                        Friend mUser = new Friend(childSnapshot.getKey(), name, phone, "");
                        if (name.equals(phone)) {
                            for (Friend mUserIt : contactList) {
                                if (mUserIt.getPhoneNumber().equals(mUser.getPhoneNumber())) {
                                    mUser.setName(mUserIt.getName());
                                }
                            }
                        }

                        contactList.add(mUser);
                        mAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
