package ml.luiggi.geosongfy;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/*
* Questa classe rappresenta l'Adapter per poter correttamente visualizzare la lista delle canzoni all'interno dell'oggetto RecyclerView.*/
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongListViewHolder> {
    private ArrayList<Song> songList;

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
    public void onBindViewHolder(@NonNull final SongListViewHolder holder, final int position) {
        holder.mTitle.setText(songList.get(position).getTitle());

        if(!songList.get(position).getFeats().equals("")) {
            String aut_feat = songList.get(position).getAuthors() + " ft. "+songList.get(position).getFeats();
            holder.mAuthors.setText(aut_feat);
        }else{
            holder.mAuthors.setText(songList.get(position).getAuthors());
        }
        Picasso.get().load(songList.get(position).getCover()).into(holder.mCover);
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),SongActivity.class);
                //Intent intent = new Intent(view.getContext(),SongActivityTwo.class);
                intent.putExtra("songSelected",songList.get(holder.getAdapterPosition()));
                //passo anche l'arraylist totale perchè potrebbe servirmi per implementare poi il pulsante avanti/indietro
                intent.putExtra("allSongs",songList);
                view.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    //Questa classe mi serve per poter gestire le viste varie (il ViewHolder)
    public class SongListViewHolder extends RecyclerView.ViewHolder{
        public TextView mTitle,mAuthors;
        public ImageView mCover;
        public LinearLayout mLayout;
        public SongListViewHolder(View view){
            super(view);
            mTitle = view.findViewById(R.id.titolo_canzone);
            mAuthors = view.findViewById(R.id.autore_canzone);
            mLayout = view.findViewById(R.id.item_songs_id);
            mCover = view.findViewById(R.id.cover_image);
        }
    }
}
