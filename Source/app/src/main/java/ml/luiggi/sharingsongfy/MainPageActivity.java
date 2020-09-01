package ml.luiggi.sharingsongfy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ml.luiggi.sharingsongfy.fragments.FragmentHome;
import ml.luiggi.sharingsongfy.fragments.FragmentPeople;
import ml.luiggi.sharingsongfy.fragments.PlaylistFragment;
import ml.luiggi.sharingsongfy.services.FriendPlayerService;

/*
 * Questa classe rappresenta l'activity principale dell'app. Al suo interno è presente un BottomNavigationView che consente di navigare tra Fragment.*/
public class MainPageActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView mBottomNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //STOP SERVIZIO
        if (getIntent().getAction() != null) {
            if (getIntent().getAction().equals("STOP"))
                stopService(new Intent(getApplicationContext(), FriendPlayerService.class));
        }
        initTutorial();
        //carico il fragment principale (quello contenente la lista delle canzoni nel server)
        loadFragment(new FragmentHome());
        //Inizializzo il bottom nav menu
        initBottomView();
    }

    public void initTutorial() {
        //Il mini tutorial sulle gestures è un semplice alert dialog testuale
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainPageActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.tutorial_gestures, null);
        CheckBox mCheck = mView.findViewById(R.id.tutorial_checkbox);
        mBuilder.setTitle("Utilizza le Gestures per ascoltare la musica!");
        mBuilder.setMessage("1- Puoi scorrere col dito a sinistra e a destra per navigare tra le sezioni dell'app. \n2- Scorri con due dita verso il basso per riprodurre o mettere in pausa il brano. \n3- Disegna una freccia a sinistra (o a destra) per andare al brano precedente (o successivo) \n5- Fai swipe verso l'alto o verso il basso, nell'immagine di copertina, per regolare il volume. \n6- Per rivedere questo tutorial, fai swipe verso sinistra dalla home.");
        mBuilder.setView(mView);
        mBuilder.setPositiveButton("OK, Ho capito", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
        //se il dialogo non dovrà essere più visualizzato, salvo questa scelta nello Shared Preferences
        mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                storeDialogStatus(compoundButton.isChecked());
            }
        });
        if (getDialogStatus()) {
            mDialog.hide();
        } else {
            mDialog.show();
        }
    }

    //Metodi per il tutorial
    //funzione che salva, se selezionato, il valore della cella "non mostrare più"
    public void storeDialogStatus(boolean isChecked) {
        SharedPreferences mSharedPreferences = getSharedPreferences("rememberMe", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("ricordami", isChecked);
        mEditor.apply();
    }

    //funzione che legge lo stato della variabile precedentemente salvata
    public boolean getDialogStatus() {
        SharedPreferences mSharedPreferences = getSharedPreferences("rememberMe", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("ricordami", false);
    }

    public void initBottomView() {
        mBottomNavView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNavView.setOnNavigationItemSelectedListener(this);
        //imposto questo listener per evitare che il fragment si ricarichi se riselezionato
        mBottomNavView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

            }
        });
    }

    //funzione per cambiare l'itemId selezionato del bottomNav
    public void changeFocus(int id) {
        mBottomNavView.setSelectedItemId(id);
    }

    //funzione per caricare un fragment specifico
    public boolean loadFragment(Fragment mFragment) {
        if (mFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mFragment)
                    .commit();
            return true;
        }
        return false;
    }

    //funzione per fare lo switch tra i fragment qualora uno di essi viene selezionato dal BottomNavigationView
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment mFragment = null;
        switch (item.getItemId()) {
            case R.id.home:
                mFragment = new FragmentHome();
                break;
            case R.id.fragment_tue_playlist:
                mFragment = new PlaylistFragment();
                break;
            case R.id.fragment_people:
                getPermissions();
                return true;
        }
        return loadFragment(mFragment);
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (permissions[0].equals(Manifest.permission.READ_CONTACTS)
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Menu menu = mBottomNavView.getMenu();
                MenuItem menuItem = menu.findItem(R.id.home_fragment);
                changeFocus(menuItem.getItemId());
                FragmentHome.result = true;
                loadFragment(new FragmentHome());
            } else if (permissions[0].equals(Manifest.permission.WRITE_CONTACTS)
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Menu menu = mBottomNavView.getMenu();
                MenuItem menuItem = menu.findItem(R.id.home);
                changeFocus(menuItem.getItemId());
                FragmentHome.result = true;
                loadFragment(new FragmentHome());
            } else {
                loadFragment(new FragmentPeople());
            }
        }
    }

    DatabaseReference dbUsers;

    @Override
    protected void onDestroy() {
        //L'accesso a FirebaseAuth è perforza non nullo in quanto senza di esso non sarebbe possibile accedere a questa activity
        assert FirebaseAuth.getInstance().getUid() != null;
        dbUsers = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("isSharing");
        Boolean b = Boolean.FALSE;
        dbUsers.setValue(b);
        super.onDestroy();
    }
}