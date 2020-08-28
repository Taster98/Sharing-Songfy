package ml.luiggi.geosongfy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import ml.luiggi.geosongfy.fragments.FragmentHome;
import ml.luiggi.geosongfy.fragments.FragmentPeople;
import ml.luiggi.geosongfy.fragments.PlaylistFragment;

/*
 * Questa classe rappresenta l'activity principale dell'app. Al suo interno è presente un BottomNavigationView che consente di navigare tra Fragment.*/
public class MainPageActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView mBottomNavView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        initTutorial();
        //carico il fragment principale (quello contenente la lista delle canzoni nel server)
        loadFragment(new FragmentHome());

        //Inizializzo il bottom nav menu
        initBottomView();
    }

    public void initTutorial() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainPageActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.tutorial_gestures, null);
        CheckBox mCheck = mView.findViewById(R.id.tutorial_checkbox);
        mBuilder.setTitle("Utilizza le Gestures per ascoltare la musica!");
        mBuilder.setMessage("1- Puoi scorrere col dito a sinistra e a destra per navigare tra le sezioni dell'app. \n2- Premi tap per avviare o fermare la musica. \n3- Tieni premuto per far ricominciare il brano dall'inizio. \n4- Fai swipe a destra o a sinistra per andare alla canzone successiva o precedente. \n5- Fai swipe verso l'alto o verso il basso per regolare il volume. \n6- Per rivedere questo tutorial, fai swipe verso sinistra nella home.");
        mBuilder.setView(mView);
        mBuilder.setPositiveButton("OK, Ho capito", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();

        mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    storeDialogStatus(true);
                }else{
                    storeDialogStatus(false);
                }
            }
        });
        if(getDialogStatus()){
            mDialog.hide();
        }else{
            mDialog.show();
        }
    }

    //Metodi per il tutorial
    //funzione che salva, se selezionato, il valore della cella "non mostrare più"
    public void storeDialogStatus(boolean isChecked){
        SharedPreferences mSharedPreferences = getSharedPreferences("rememberMe", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("ricordami", isChecked);
        mEditor.apply();
    }
    //funzione che legge lo stato della variabile precedentemente salvata
    public boolean getDialogStatus(){
        SharedPreferences mSharedPreferences = getSharedPreferences("rememberMe", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("ricordami", false);
    }

    public void initBottomView(){
        mBottomNavView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNavView.setOnNavigationItemSelectedListener(this);
        mBottomNavView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {

            }
        });
    }
    public void changeFocus(int id){
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
                mFragment = new FragmentPeople();
                break;
        }
        return loadFragment(mFragment);
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }
}