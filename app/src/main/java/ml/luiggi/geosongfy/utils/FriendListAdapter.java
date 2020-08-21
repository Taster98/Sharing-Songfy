package ml.luiggi.geosongfy.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

    @Override
    public void onBindViewHolder(@NonNull final FriendListViewHolder holder, final int position) {
        holder.mName.setText(friendList.get(position).getName());
        holder.mNumber.setText(friendList.get(position).getPhoneNumber());

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                TODO


                Intent intent = new Intent(view.getContext(), FriendActivity.class);
                intent.putExtra("friendSelected",friendList.get(holder.getAdapterPosition()));
                view.getContext().startActivity(intent);*/
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

        public FriendListViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.nome_friend);
            mNumber = view.findViewById(R.id.numero_friend);
            mLayout = view.findViewById(R.id.linear_item_id);
        }
    }
}
