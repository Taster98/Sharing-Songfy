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
import ml.luiggi.geosongfy.scaffoldings.Chat;
import ml.luiggi.geosongfy.scaffoldings.Friend;
import ml.luiggi.geosongfy.scaffoldings.FriendSelected;

/*
 * Questa classe rappresenta l'Adapter per poter correttamente visualizzare la lista delle canzoni all'interno dell'oggetto RecyclerView.
 * */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {
    private ArrayList<Chat> chatList;

    public ChatListAdapter(ArrayList<Chat> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        ChatListViewHolder mChatVH = new ChatListViewHolder(layoutView);
        return mChatVH;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListViewHolder holder, final int position) {
        holder.mName.setText(chatList.get(position).getChatName());
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    //Questa classe mi serve per poter gestire le viste varie (il ViewHolder)
    public class ChatListViewHolder extends RecyclerView.ViewHolder {
        public TextView mName;
        public LinearLayout mLayout;
        public RelativeLayout mRelativeLayout;

        public ChatListViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.nome_chat);
            mLayout = view.findViewById(R.id.chat_linear_item_id);
            mRelativeLayout = view.findViewById(R.id.chat_item_id);
        }
    }
}
