package ml.luiggi.geosongfy.utils;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Friend;
import ml.luiggi.geosongfy.scaffoldings.FriendSelected;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.scaffoldings.SongSelected;
import ml.luiggi.geosongfy.services.FriendPlayerService;

/*
 * Questa classe rappresenta l'Adapter per poter correttamente visualizzare la lista delle canzoni all'interno dell'oggetto RecyclerView.
 * */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder> {
    private ArrayList<Friend> friendList;
    public FriendListAdapter(ArrayList<Friend> friendList) {
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, null, false);
        FriendListViewHolder mFriendVH = new FriendListViewHolder(layoutView);
        return mFriendVH;
    }
    int checked = 0;
    @Override
    public void onBindViewHolder(@NonNull final FriendListViewHolder holder, final int position) {
        holder.mName.setText(friendList.get(position).getName());
        holder.mNumber.setText(friendList.get(position).getPhoneNumber());
        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            int checkedDue = 0;
            @Override
            public void onClick(final View view) {
                final Intent intent = new Intent(view.getContext(), FriendPlayerService.class);
                if(checked == 0){
                    if(checkedDue == 0){
                        holder.mMute.setImageResource(R.drawable.ic_volume);
                        checkedDue=1;
                        checked=1;
                        //avviare musica
                        intent.putExtra("uid",(friendList.get(position).getUid()).toString());
                        intent.putExtra("songUrl",friendList.get(position).getCurrentSong().getUrl());
                        intent.putExtra("position",friendList.get(position).getSongPosition());
                        view.getContext().startService(intent);
                    }else{
                        holder.mMute.setImageResource(R.drawable.ic_mute);
                        checkedDue=0;
                        checked=0;
                        //fermare musica
                        view.getContext().stopService(intent);
                    }
                }else{
                    if(checkedDue==1){
                        checkedDue=0;
                        checked=0;
                        holder.mMute.setImageResource(R.drawable.ic_mute);
                        //fermare musica
                        view.getContext().stopService(intent);
                    }else {
                        Toast.makeText(view.getContext(), "Musica già in riproduzione!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    //Questa classe mi serve per poter gestire le viste varie (il ViewHolder)
    public class FriendListViewHolder extends RecyclerView.ViewHolder {
        public TextView mName, mNumber;
        public LinearLayout mLayout;
        public ImageView mMute;
        public RelativeLayout mRelativeLayout;

        public FriendListViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.nome_friend);
            mNumber = view.findViewById(R.id.numero_friend);
            mLayout = view.findViewById(R.id.linear_item_id);
            mMute = view.findViewById(R.id.friend_mute_id);
            mRelativeLayout = view.findViewById(R.id.friend_item_id);
        }
    }
}
