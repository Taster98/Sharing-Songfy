package ml.luiggi.geosongfy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongListViewHolder> {
    private ArrayList<Song> songList;

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public SongViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

    public SongListAdapter(ArrayList<Song> songList){
        this.songList=songList;
    }

    @NonNull
    @Override
    public SongListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song,null,false);
        SongListViewHolder mSongVH = new SongListViewHolder(layoutView);
        return mSongVH;
    }

    @Override
    public void onBindViewHolder(@NonNull SongListViewHolder holder, int position) {
        holder.mTitle.setText(songList.get(position).getTitle());
        for(String s : songList.get(position).getAuthors())
            holder.mAuthors.setText(s);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    //creo una classe per gestire il viewHolder personalizzato
    public class SongListViewHolder extends RecyclerView.ViewHolder{
        public TextView mTitle,mAuthors, mFeats;
        public ImageView mCover;
        public LinearLayout mLayout;
        public SongListViewHolder(View view){
            super(view);
            mTitle = view.findViewById(R.id.titolo_canzone);
            mAuthors = view.findViewById(R.id.autore_canzone);
            mLayout = view.findViewById(R.id.item_songs_id);
        }
    }
}
