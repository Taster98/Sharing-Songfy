package ml.luiggi.sharingsongfy.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ml.luiggi.sharingsongfy.R;
import ml.luiggi.sharingsongfy.scaffoldings.Song;

/*
 * Questa classe rappresenta l'Adapter per poter correttamente visualizzare la lista delle canzoni all'interno dell'oggetto RecyclerView.
 * */
public class DialogListAdapter extends RecyclerView.Adapter<DialogListAdapter.DialogListViewHolder> {
    private final ArrayList<Song> songList;
    public ArrayList<Song> checkedList;

    public DialogListAdapter(ArrayList<Song> songList) {
        checkedList = new ArrayList<>();
        this.songList = songList;
    }

    @NonNull
    @Override
    public DialogListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_dialog, null, false);
        return new DialogListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DialogListViewHolder holder, final int position) {
        final Song curSong = songList.get(position);
        //inizializzo il checkbox per evitare errori
        holder.mCheckBox.setOnCheckedChangeListener(null);
        //imposto il checkbox in base all'oggetto
        holder.mCheckBox.setChecked(curSong.isSelected());
        //Imposto il layout
        holder.mTitle.setText(curSong.getTitle());
        if (!curSong.getFeats().equals("")) {
            String aut_feat = curSong.getAuthors() + " ft. " + curSong.getFeats();
            holder.mAuthors.setText(aut_feat);
        } else {
            holder.mAuthors.setText(curSong.getAuthors());
        }
        Picasso.get().load(curSong.getCover()).into(holder.mCover);
        //Imposto il listener per lo stato della checkbox
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //imposto "selected" della canzone selezionata
                curSong.setSelected(b);
                if(curSong.isSelected()){
                    checkedList.add(curSong);
                }else{
                    checkedList.remove(curSong);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    //Questa classe mi serve per poter gestire le viste varie (il ViewHolder)
    public static class DialogListViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle, mAuthors;
        public ImageView mCover;
        public LinearLayout mLayout;
        public CheckBox mCheckBox;

        public DialogListViewHolder(View view) {
            super(view);
            mTitle = (TextView)view.findViewById(R.id.titolo_canzone_dialog);
            mAuthors = (TextView)view.findViewById(R.id.autore_canzone_dialog);
            mLayout = (LinearLayout)view.findViewById(R.id.item_songs_id_dialog);
            mCover = (ImageView)view.findViewById(R.id.cover_image_dialog);
            mCheckBox = (CheckBox)view.findViewById(R.id.checkbox_dialog);
        }
    }
}
