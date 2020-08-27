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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ml.luiggi.geosongfy.PlaylistActivity;
import ml.luiggi.geosongfy.R;
import ml.luiggi.geosongfy.scaffoldings.Playlist;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.PlayListViewHolder> {
    private ArrayList<Playlist> playlists;

    public PlayListAdapter(ArrayList<Playlist> playlists) {
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public PlayListAdapter.PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, null, false);
        PlayListAdapter.PlayListViewHolder mPlayVH = new PlayListAdapter.PlayListViewHolder(layoutView);
        return mPlayVH;
    }

    @Override
    public void onBindViewHolder(@NonNull final PlayListAdapter.PlayListViewHolder holder, final int position) {
        holder.mName.setText(playlists.get(position).getPlaylistName());
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPlaylists(view);
                Intent intent = new Intent(view.getContext(),PlaylistActivity.class);
                intent.putExtra("playlistSelected", playlists.get(holder.getAdapterPosition()));
                intent.putExtra("allPlaylists",playlists);
                view.getContext().startActivity(intent);
            }

        });
        holder.mLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showMenu(view,position);
                return true;
            }
        });
    }

    private void showMenu(final View v, final int i) {
        PopupMenu popup = new PopupMenu(v.getContext(),v);
        popup.getMenuInflater()
                .inflate(R.menu.delete_rename_menu,popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.delete_bi_menu:
                        deleteDialog(v,i);
                        notifyItemRangeChanged(i, playlists.size());
                        break;
                    case R.id.rename_bi_menu:
                        renameDialog(v,i);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void deleteDialog(final View v, final int pos) {
        AlertDialog.Builder alertbox = new AlertDialog.Builder(v.getRootView().getContext());
        alertbox.setMessage("Sicuro di voler eliminare la Playlist '"+playlists.get(pos).getPlaylistName()+"'?");
        alertbox.setTitle("Attenzione");
        alertbox.setIcon(R.drawable.ic_delete);

        alertbox.setPositiveButton("Sì",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        playlists.remove(pos);
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

    private void renameDialog(final View v, final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
        final View view = LayoutInflater.from(v.getRootView().getContext()).inflate(R.layout.rename_playlist, null);
        builder.setView(view)
                .setPositiveButton(R.string.rinomina, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = view.findViewById(R.id.nuovo_nome_playlist);
                        if (editText.getText().toString().equals(""))
                            Toast.makeText(v.getRootView().getContext(), "Devi inserire un nome valido per la Playlist!", Toast.LENGTH_SHORT).show();
                        else {
                            String curName = editText.getText().toString();
                            playlists.get(pos).setPlaylistName(curName);
                            notifyItemChanged(pos);
                            savePlaylists(view);
                        }
                    }
                })
                .setNegativeButton(R.string.annulla, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    //Questa classe mi serve per poter gestire le viste varie (il ViewHolder)
    public class PlayListViewHolder extends RecyclerView.ViewHolder {

        public TextView mName;
        public LinearLayout mLayout;

        public PlayListViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.titolo_playlist);
            mLayout = view.findViewById(R.id.item_playlist);
        }
    }

    //funzione che salva le playlists
    private void savePlaylists(View v) {
        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences("saved_playlists", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(playlists);
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
        playlists = gson.fromJson(json, type);
    }
}
