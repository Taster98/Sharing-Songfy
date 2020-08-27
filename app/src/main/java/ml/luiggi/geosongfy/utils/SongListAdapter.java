package ml.luiggi.geosongfy.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.SongActivity;
import ml.luiggi.geosongfy.scaffoldings.Playlist;
import ml.luiggi.geosongfy.scaffoldings.Song;

/*
 * Questa classe rappresenta l'Adapter per poter correttamente visualizzare la lista delle canzoni all'interno dell'oggetto RecyclerView.
 * */
public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongListViewHolder> {
    private ArrayList<Song> songList;
    public Playlist isPlaylist;
    public ArrayList<Playlist> allPlaylists;
    public SongListAdapter(ArrayList<Song> songList) {
        this.songList = songList;
        this.isPlaylist = null;
        this.allPlaylists = null;
    }

    public SongListAdapter(ArrayList<Song> songList, Playlist isPlaylist){
        this.isPlaylist = isPlaylist;
        this.songList = songList;
    }
    @NonNull
    @Override
    public SongListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, null, false);
        SongListViewHolder mSongVH = new SongListViewHolder(layoutView);
        return mSongVH;
    }

    @Override
    public void onBindViewHolder(@NonNull final SongListViewHolder holder, final int position) {
        holder.mTitle.setText(songList.get(position).getTitle());

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
                loadPlaylists(view);
                Intent intent = new Intent(view.getContext(), SongActivity.class);
                intent.putExtra("songSelected", songList.get(holder.getAdapterPosition()));
                //passo anche l'arraylist totale perchè potrebbe servirmi per implementare poi il pulsante avanti/indietro
                intent.putExtra("allSongs", songList);
                view.getContext().startActivity(intent);

            }
        });
        holder.mCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SongActivity.class);
                intent.putExtra("songSelected", songList.get(holder.getAdapterPosition()));
                //passo anche l'arraylist totale perchè potrebbe servirmi per implementare poi il pulsante avanti/indietro
                intent.putExtra("allSongs", songList);
                view.getContext().startActivity(intent);

            }
        });
        holder.mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SongActivity.class);
                intent.putExtra("songSelected", songList.get(holder.getAdapterPosition()));
                //passo anche l'arraylist totale perchè potrebbe servirmi per implementare poi il pulsante avanti/indietro
                intent.putExtra("allSongs", songList);
                view.getContext().startActivity(intent);

            }
        });
        if(isPlaylist != null){
            holder.mLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showMenu(view,position);
                    return true;
                }
            });
        }
    }

    private void showMenu(final View v, final int i) {
        PopupMenu popup = new PopupMenu(v.getContext(),v);
        popup.getMenuInflater()
                .inflate(R.menu.delete_only_menu,popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.delete_one_menu){
                    deleteDialog(v,i);
                }
                return true;
            }
        });
        popup.show();
    }

    private void deleteDialog(final View v, final int pos) {
        AlertDialog.Builder alertbox = new AlertDialog.Builder(v.getRootView().getContext());
        alertbox.setMessage("Sicuro di voler eliminare la canzone '"+songList.get(pos).getTitle()+"' di '"+songList.get(pos).getAuthors()+"'?");
        alertbox.setTitle("Attenzione");
        alertbox.setIcon(R.drawable.ic_delete);

        alertbox.setPositiveButton("Sì",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //implementare eliminazione
                    }
                })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        notifyDataSetChanged();
        alertbox.show();
    }
    @Override
    public int getItemCount() {
        return songList.size();
    }

    //Questa classe mi serve per poter gestire le viste varie (il ViewHolder)
    public class SongListViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle, mAuthors;
        public ImageView mCover, mPlay;
        public LinearLayout mLayout;

        public SongListViewHolder(View view) {
            super(view);
            mTitle = view.findViewById(R.id.titolo_canzone);
            mAuthors = view.findViewById(R.id.autore_canzone);
            mLayout = view.findViewById(R.id.item_songs_id);
            mCover = view.findViewById(R.id.cover_image);
            mPlay = view.findViewById(R.id.play_right_icon);
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
    }
}
