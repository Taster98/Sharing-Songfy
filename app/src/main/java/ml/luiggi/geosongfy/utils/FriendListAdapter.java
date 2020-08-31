package ml.luiggi.geosongfy.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.SongActivity;
import ml.luiggi.geosongfy.scaffoldings.Friend;
import ml.luiggi.geosongfy.services.FriendPlayerService;

/*
Adapter customizzato per l'elenco degli amici che condividono gli ascolti nella recycler view.
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder> {
    private final ArrayList<Friend> friendList;

    public FriendListAdapter(ArrayList<Friend> friendList) {
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, null, false);
        return new FriendListViewHolder(layoutView);
    }

    int checked = 0;

    @Override
    public void onBindViewHolder(@NonNull final FriendListViewHolder holder, final int position) {
        holder.mName.setText(friendList.get(holder.getAdapterPosition()).getName());
        holder.mNumber.setText(friendList.get(holder.getAdapterPosition()).getPhoneNumber());
        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(final View view) {
                final Intent intent = new Intent(view.getContext(), FriendPlayerService.class);
                if (checked == 0) {
                    if (!isMyServiceRunning(FriendPlayerService.class, view.getContext())) {
                        holder.mMute.setImageResource(R.drawable.ic_volume);
                        checked = 1;
                        //avviare musica
                        intent.putExtra("uid", (friendList.get(holder.getAdapterPosition()).getUid()));
                        intent.putExtra("songUrl", friendList.get(holder.getAdapterPosition()).getCurrentSong().getUrl());
                        intent.putExtra("position", friendList.get(holder.getAdapterPosition()).getSongPosition());
                        if (SongActivity.notificationManager != null)
                            SongActivity.notificationManager.cancelAll();
                        if (SongActivity.mPlayer != null)
                            SongActivity.mPlayer.stop();

                        view.getContext().startForegroundService(intent);
                        String info = "In riproduzione: " + friendList.get(holder.getAdapterPosition()).getCurrentSong().getTitle()
                                + " di " + friendList.get(holder.getAdapterPosition()).getCurrentSong().getAuthors();
                        if (!friendList.get(holder.getAdapterPosition()).getCurrentSong().getFeats().equals(""))
                            info = info + " ft. " + friendList.get(holder.getAdapterPosition()).getCurrentSong().getFeats();
                        Toast.makeText(view.getContext(), info, Toast.LENGTH_LONG).show();
                    } else {
                        holder.mMute.setImageResource(R.drawable.ic_mute);
                        checked = 0;
                        //fermare musica
                        view.getContext().stopService(intent);
                    }
                } else {
                    if (isMyServiceRunning(FriendPlayerService.class, view.getContext()) || SongActivity.mPlayer == null) {
                        checked = 0;
                        holder.mMute.setImageResource(R.drawable.ic_mute);
                        //fermare musica
                        view.getContext().stopService(intent);
                    } else {
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

    public void clear() {
        friendList.clear();
        notifyDataSetChanged();
    }

    public static class FriendListViewHolder extends RecyclerView.ViewHolder {
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

    //Controllo se il servizio in questione è attivo o no (da https://gist.github.com/kevinmcmahon/2988931)
    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
