package ml.luiggi.geosongfy.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Friend;

/*
 * Questa classe rappresenta l'Adapter per poter correttamente visualizzare la lista delle canzoni all'interno dell'oggetto RecyclerView.
 * */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder> {
    private ArrayList<Friend> friendList;
    public ArrayList<Friend> checkedList;

    public FriendListAdapter(ArrayList<Friend> friendList) {
        this.friendList = friendList;
        this.checkedList = new ArrayList<>();
    }

    @NonNull
    @Override
    public FriendListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, null, false);
        FriendListViewHolder mFriendVH = new FriendListViewHolder(layoutView);
        return mFriendVH;
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendListViewHolder holder, final int position) {
        final Friend curFriend = friendList.get(position);
        holder.mName.setText(friendList.get(position).getName());
        holder.mNumber.setText(friendList.get(position).getPhoneNumber());
        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mCheckBox.setChecked(!holder.mCheckBox.isChecked());
                if (holder.mCheckBox.isChecked()) {
                    checkedList.add(curFriend);
                } else {
                    checkedList.remove(curFriend);
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
        public CheckBox mCheckBox;
        public RelativeLayout mRelativeLayout;

        public FriendListViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.nome_friend);
            mNumber = view.findViewById(R.id.numero_friend);
            mLayout = view.findViewById(R.id.linear_item_id);
            mCheckBox = view.findViewById(R.id.checkbox);
            mRelativeLayout = view.findViewById(R.id.friend_item_id);
            if (mCheckBox != null)
                mCheckBox.setClickable(false);
        }
    }
}
