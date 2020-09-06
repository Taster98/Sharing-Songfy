package ml.luiggi.sharingsongfy.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ml.luiggi.sharingsongfy.R;
import ml.luiggi.sharingsongfy.SongActivity;
import ml.luiggi.sharingsongfy.scaffoldings.Playlist;
import ml.luiggi.sharingsongfy.scaffoldings.Song;

/*
 * Questa classe rappresenta l'Adapter per poter correttamente visualizzare la lista delle canzoni all'interno dell'oggetto RecyclerView.
 * */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongListViewHolder> {
    private final ArrayList<Song> songList;
    public Playlist isPlaylist;
    public ArrayList<Playlist> allPlaylists;

    public SongListAdapter(ArrayList<Song> songList) {
        this.songList = songList;
        this.isPlaylist = null;
        this.allPlaylists = null;
    }

    public SongListAdapter(ArrayList<Song> songList, Playlist isPlaylist) {
        this.isPlaylist = isPlaylist;
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, null, false);
        return new SongListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongListViewHolder holder, final int position) {
        holder.mTitle.setText(songList.get(holder.getAdapterPosition()).getTitle());

        if (!songList.get(holder.getAdapterPosition()).getFeats().equals("")) {
            String aut_feat = songList.get(holder.getAdapterPosition()).getAuthors() + " ft. " + songList.get(holder.getAdapterPosition()).getFeats();
            holder.mAuthors.setText(aut_feat);
        } else {
            holder.mAuthors.setText(songList.get(holder.getAdapterPosition()).getAuthors());
        }
        Picasso.get().load(songList.get(holder.getAdapterPosition()).getCover()).into(holder.mCover);
        //creo il listener
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPlaylists(view);
                Intent intent = new Intent(view.getContext(), SongActivity.class);
                //INIZIO TEST
                Song curSong = songList.get(holder.getAdapterPosition());
                Gson gson = new Gson();
                String curSongJson = gson.toJson(curSong);
                String allSongsJson = gson.toJson(songList);
                Bundle songBundle = new Bundle();
                songBundle.putString("songSelected",curSongJson);
                Bundle arrayBundle = new Bundle();
                arrayBundle.putString("allSongs",allSongsJson);
                intent.putExtras(songBundle);
                intent.putExtras(arrayBundle);
                //FINE TEST
                //intent.putExtra("songSelected", songList.get(holder.getAdapterPosition()));
                //passo anche l'arraylist totale perchè potrebbe servirmi per implementare poi il pulsante avanti/indietro
                //intent.putExtra("allSongs", songList);
                view.getContext().startActivity(intent);
            }
        };
        holder.mLayout.setOnClickListener(listener);
        holder.mCover.setOnClickListener(listener);
        holder.mPlay.setOnClickListener(listener);
        if (isPlaylist != null) {
            holder.mLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showMenu(view, holder.getAdapterPosition());
                    return true;
                }
            });
        }
    }

    private void showMenu(final View v, final int i) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater()
                .inflate(R.menu.delete_only_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.delete_one_menu) {
                    deleteDialog(v, i);
                }
                return true;
            }
        });
        popup.show();
    }

    private void deleteDialog(final View v, final int pos) {
        AlertDialog.Builder alertbox = new AlertDialog.Builder(v.getRootView().getContext());
        alertbox.setMessage("Sicuro di voler eliminare la canzone '" + songList.get(pos).getTitle() + "' di '" + songList.get(pos).getAuthors() + "'?");
        alertbox.setTitle("Attenzione");
        alertbox.setIcon(R.drawable.ic_delete);

        alertbox.setPositiveButton("Sì",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //implementare eliminazione
                        songList.remove(pos);
                        isPlaylist.setSongList(songList);
                        loadPlaylists(v);
                        allPlaylists.remove(isPlaylist);
                        allPlaylists.add(isPlaylist);
                        notifyItemRemoved(pos);
                        savePlaylists(v);
                    }
                })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        alertbox.show();
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    //Questa classe mi serve per poter gestire le viste varie (il ViewHolder)
    public static class SongListViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle, mAuthors;
        public ImageView mCover, mPlay;
        public LinearLayout mLayout;

        public SongListViewHolder(View view) {
            super(view);
            mTitle = (TextView)view.findViewById(R.id.titolo_canzone);
            mAuthors = (TextView)view.findViewById(R.id.autore_canzone);
            mLayout = (LinearLayout)view.findViewById(R.id.item_songs_id);
            mCover = (ImageView)view.findViewById(R.id.cover_image);
            mPlay = (ImageView)view.findViewById(R.id.play_right_icon);
        }
    }

    //funzione che salva le playlists
    private void savePlaylists(View v) {
        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("saved_playlists", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(allPlaylists);
        editor.putString("playlist_list", json);
        editor.apply();
    }

    //funzione che ricarica le playlist salvate
    private void loadPlaylists(View v) {
        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("saved_playlists", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("playlist_list", null);
        Type type = new TypeToken<ArrayList<Playlist>>() {
        }.getType();
        allPlaylists = gson.fromJson(json, type);
        if (allPlaylists == null)
            allPlaylists = new ArrayList<>();
    }
}
