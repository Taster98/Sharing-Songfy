package ml.luiggi.geosongfy.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import ml.luiggi.geosongfy.ContactListActivity;
import ml.luiggi.geosongfy.MainPageActivity;
import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Chat;
import ml.luiggi.geosongfy.scaffoldings.Friend;
import ml.luiggi.geosongfy.utils.FriendListAdapter;
import ml.luiggi.geosongfy.utils.Iso2Phone;

public class FragmentPeople extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    ArrayList<Chat> chatList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_people, container, false);
        initPeopleFragment(v);
        return v;
    }

    private void initPeopleFragment(final View v) {
        chatList = new ArrayList<>();
        //riferimento alla recycler view
        recyclerView = (RecyclerView) v.findViewById(R.id.chatList);
        //associo il floating action button
        FloatingActionButton mFloatingActionButton = (FloatingActionButton) v.findViewById(R.id.floating_chat);
        //dimensione nel layout fissata
        recyclerView.setHasFixedSize(true);
        //imposto un layout manager per la recycler view
        mLayoutManager = new LinearLayoutManager(v.getContext());

        //prelevo tutte le chat che ho TODO
        getChatList();
        
        
        //gestisco il click sul bottone
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Intent intent = new Intent(v.getContext(),ContactListActivity.class);
               startActivity(intent);
            }
        });
        initGestures(v);
    }

    private void getChatList() {
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");

        mUserChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                        Chat mChat = new Chat(childSnapshot.getKey());
                        boolean  exists = false;
                        for (Chat mChatIterator : chatList){
                            if (mChatIterator.getChatId().equals(mChat.getChatId()))
                                exists = true;
                        }
                        if (exists)
                            continue;
                        chatList.add(mChat);
                        getChatData(mChat.getChatId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId).child("info");
        mChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String chatId = "";

                    if(dataSnapshot.child("id").getValue() != null)
                        chatId = dataSnapshot.child("id").getValue().toString();

                    for(DataSnapshot userSnapshot : dataSnapshot.child("users").getChildren()){
                        for(Chat mChat : chatList){
                            if(mChat.getChatId().equals(chatId)){
                                Friend mUser = new Friend(userSnapshot.getKey());
                                mChat.addFriendToArrayList(mUser);
                                getUserData(mUser);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserData(Friend mUser) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().getReference().child("user").child(mUser.getUid());
        mUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Friend mUser = new Friend(dataSnapshot.getKey());

                if(dataSnapshot.child("notificationKey").getValue() != null)
                    mUser.setNotificationKey(dataSnapshot.child("notificationKey").getValue().toString());

                for(Chat mChat : chatList){
                    for (Friend mUserIt : mChat.getFriendArrayList()){
                        if(mUserIt.getUid().equals(mUser.getUid())){
                            mUserIt.setNotificationKey(mUser.getNotificationKey());
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGestures(View v) {
        recyclerView = v.findViewById(R.id.chatList);
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

}
