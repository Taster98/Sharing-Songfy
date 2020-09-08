package ml.luiggi.sharingsongfy.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import ml.luiggi.sharingsongfy.PlaylistActivity;
import ml.luiggi.sharingsongfy.R;
import ml.luiggi.sharingsongfy.scaffoldings.Playlist;
import ml.luiggi.sharingsongfy.scaffoldings.Song;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.PlayListViewHolder> {
    private ArrayList<Playlist> playlists;

    public PlayListAdapter(ArrayList<Playlist> playlists) {
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public PlayListAdapter.PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, null, false);
        return new PlayListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlayListAdapter.PlayListViewHolder holder, final int position) {
        holder.mName.setText(playlists.get(holder.getAdapterPosition()).getPlaylistName());
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPlaylists(view);
                Intent intent = new Intent(view.getContext(), PlaylistActivity.class);
                //INIZIO TEST
                Playlist curPlaylist = playlists.get(holder.getAdapterPosition());
                Gson gson = new Gson();
                String curPlaylistJson = gson.toJson(curPlaylist);
                String allPlaylistJson = gson.toJson(playlists);
                Bundle playlistBundle = new Bundle();
                playlistBundle.putString("playlistSelected",curPlaylistJson);
                Bundle arrayBundle = new Bundle();
                arrayBundle.putString("allPlaylists",allPlaylistJson);
                intent.putExtras(playlistBundle);
                intent.putExtras(arrayBundle);
                //FINE TEST
                //intent.putExtra("playlistSelected", playlists.get(holder.getAdapterPosition()));
                //intent.putExtra("allPlaylists", playlists);
                view.getContext().startActivity(intent);
            }

        });
        holder.mLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showMenu(view, holder.getAdapterPosition());
                return true;
            }
        });
    }

    private void showMenu(final View v, final int i) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater()
                .inflate(R.menu.delete_rename_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.delete_bi_menu:
                        deleteDialog(v, i);
                        notifyItemRangeChanged(i, playlists.size());
                        break;
                    case R.id.rename_bi_menu:
                        renameDialog(v, i);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void deleteDialog(final View v, final int pos) {
        AlertDialog.Builder alertbox = new AlertDialog.Builder(v.getRootView().getContext());
        alertbox.setMessage("Sicuro di voler eliminare la Playlist '" + playlists.get(pos).getPlaylistName() + "'?");
        alertbox.setTitle("Attenzione");
        alertbox.setIcon(R.drawable.ic_delete);

        alertbox.setPositiveButton("Sì",
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        playlists.remove(pos);
                        savePlaylists(v);
                        notifyItemRemoved(pos);
                        notifyItemRangeChanged(pos,getItemCount());
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
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = (EditText)view.findViewById(R.id.nuovo_nome_playlist);
                        if (editText.getText().toString().equals(""))
                            Toast.makeText(v.getRootView().getContext(), "Devi inserire un nome valido per la Playlist!", Toast.LENGTH_SHORT).show();
                        else {
                            String curName = editText.getText().toString();
                            playlists.get(pos).setPlaylistName(curName);
                            savePlaylists(view);
                            notifyItemChanged(pos);
                            notifyDataSetChanged();
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
    public static class PlayListViewHolder extends RecyclerView.ViewHolder {

        public TextView mName;
        public LinearLayout mLayout;

        public PlayListViewHolder(View view) {
            super(view);
            mName = (TextView)view.findViewById(R.id.titolo_playlist);
            mLayout = (LinearLayout)view.findViewById(R.id.item_playlist);
        }
    }

    //funzione che salva le playlists
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void savePlaylists(View v){
        Gson gson = new Gson();
        String json = gson.toJson(playlists);
        String filename = "saved_playlists.txt";
        try {
            FileOutputStream fos = v.getContext().openFileOutput(filename,Context.MODE_PRIVATE);
            fos.write(json.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //funzione che ricarica le playlist salvate
    private void loadPlaylists(View v){
        String filename = "saved_playlists.txt";
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Playlist>>() {
        }.getType();
        try {
            FileInputStream fis = v.getContext().openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader streamReader = new BufferedReader(isr);

            StringBuilder json = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                json.append(inputStr);

            playlists = gson.fromJson(String.valueOf(json),type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (playlists == null)
            playlists = new ArrayList<>();
    }
}
