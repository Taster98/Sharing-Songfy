package ml.luiggi.geosongfy.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Song;
import ml.luiggi.geosongfy.scaffoldings.SongSelected;

/*
 * Questa classe rappresenta l'Adapter per poter correttamente visualizzare la lista delle canzoni all'interno dell'oggetto RecyclerView.
 * */
public class DialogListAdapter extends RecyclerView.Adapter<DialogListAdapter.DialogListViewHolder> {
    private ArrayList<Song> songList;
    public ArrayList<Song> checkedList;

    public DialogListAdapter(ArrayList<Song> songList) {
        checkedList = new ArrayList<>();
        this.songList = songList;
    }

    @NonNull
    @Override
    public DialogListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_dialog, null, false);
        DialogListViewHolder mSongVH = new DialogListViewHolder(layoutView);
        return mSongVH;
    }

    @Override
    public void onBindViewHolder(@NonNull final DialogListViewHolder holder, final int position) {
        final Song curSong = songList.get(position);
        holder.mTitle.setText(songList.get(position).getTitle());
        final SongSelected songSelected = new SongSelected() {
            @Override
            public void newSongsSelected(ArrayList<Song> selectedList) {
                checkedList = selectedList;
            }
        };
        if (!songList.get(position).getFeats().equals("")) {
            String aut_feat = songList.get(position).getAuthors() + " ft. " + songList.get(position).getFeats();
            holder.mAuthors.setText(aut_feat);
        } else {
            holder.mAuthors.setText(songList.get(position).getAuthors());
        }
        Picasso.get().load(songList.get(position).getCover()).into(holder.mCover);
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mCheckBox.setChecked(!holder.mCheckBox.isChecked());
                if (holder.mCheckBox.isChecked()) {
                    checkedList.add(curSong);
                } else {
                    checkedList.remove(curSong);
                }
                songSelected.newSongsSelected(checkedList);
            }
        });
        holder.mCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mCheckBox.setChecked(!holder.mCheckBox.isChecked());
                if (holder.mCheckBox.isChecked()) {
                    checkedList.add(curSong);
                } else {
                    checkedList.remove(curSong);
                }
                songSelected.newSongsSelected(checkedList);
            }
        });

    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    //Questa classe mi serve per poter gestire le viste varie (il ViewHolder)
    public class DialogListViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle, mAuthors;
        public ImageView mCover;
        public LinearLayout mLayout;
        public CheckBox mCheckBox;

        public DialogListViewHolder(View view) {
            super(view);
            mTitle = view.findViewById(R.id.titolo_canzone_dialog);
            mAuthors = view.findViewById(R.id.autore_canzone_dialog);
            mLayout = view.findViewById(R.id.item_songs_id_dialog);
            mCover = view.findViewById(R.id.cover_image_dialog);
            mCheckBox = view.findViewById(R.id.checkbox_dialog);
            if (mCheckBox != null)
                mCheckBox.setClickable(false);
        }
    }
}
