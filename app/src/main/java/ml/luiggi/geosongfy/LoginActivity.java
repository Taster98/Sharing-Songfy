package ml.luiggi.geosongfy;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ml.luiggi.geosongfy.utils.Iso2Phone;

public class LoginActivity extends AppCompatActivity {
    //intervallo massimo per immissione codice
    private static final long INTERVALLO = 77;
    //riferimenti varie viste
    private EditText mPrefix, mPhoneNumber, mCode;
    private Button mSend;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Inizializzo l'istanza di Firebase per il login nella piattaforma mediante numero di telefono
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        initView();
    }

    //funzione che inizializza la vista dell'activity
    private void initView() {
        //Imposto il layout principale
        setContentView(R.layout.login_layout);
        mSend = findViewById(R.id.send);
        //Se l'utente è già ammesso non riandrà in questa activity
        userAllowed();
        //riferimenti per il layout
        getRefs();
        //stampo il countrycode in maniera automatica
        mPrefix.setText(getCountryISO());

        //Listener per gestire la verifica del codice di autenticazione (click sul bottone)
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVerificationId != null) {
                    verifyPhoneNumberCode();
                } else {
                    //devo controllare il codice perchè il numero è stato inviato
                    startNumberVerification();
                }

                //tolgo la tastiera una volta immesso il numero (ingombra la vista)
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

            }
        });
        //Listener per la gestione della verifica
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInPhoneCred(phoneAuthCredential);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mVerificationId = s;
                mSend.setText("Verifica codice");
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }
        };
    }

    //Funzione che crea i riferimenti con gli elementi del Layout
    private void getRefs() {
        //riferisco gli elementi del layout
        mPrefix = findViewById(R.id.prefix);
        mPhoneNumber = findViewById(R.id.phoneNumber);
        mCode = findViewById(R.id.code);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void verifyPhoneNumberCode() {
        PhoneAuthCredential credentials = PhoneAuthProvider.getCredential(mVerificationId, mCode.getText().toString());
        signInPhoneCred(credentials);
    }

    private String getCountryISO() {
        String iso = "";
        TelephonyManager tmngr = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (tmngr.getNetworkCountryIso() != null) {
            if (!tmngr.getNetworkCountryIso().equals(""))
                iso = tmngr.getNetworkCountryIso();
        }
        return Iso2Phone.getPhone(iso);
    }

    //accedo con le credenziali in Firebase mediante l'autenticazione con numero di telefono:
    private void signInPhoneCred(PhoneAuthCredential phoneAuthCredential) {
        //aggiungo un listener, per gestire se la registrazione è andata a buon fine o meno
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //se ho avuto successo, l'utente attuale (io) viene inserito poi anche nel database (questo per poter
                    //sfruttare la funzionalità di condivisione senza problemi)
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    //naturalmente controllo se per qualche motivo l'utente fosse null
                    if (user != null) {
                        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    //Al momento della registrazione immetto i seguenti valori di default:
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    Boolean bool = Boolean.FALSE;
                                    Long lon = 0L;
                                    String str = "";
                                    userMap.put("isSharing", bool);
                                    userMap.put("position", lon);
                                    userMap.put("songUrl", str);
                                    mUserDB.updateChildren(userMap);
                                }
                                //se l'utente è ammesso, avvio l'activity successiva (quella principale)
                                userAllowed();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }
        });
    }

    private void userAllowed() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
            finish();
        }
    }

    //devo controllare che il numero sia valido:
    private void startNumberVerification() {
        String completeNumber = mPrefix.getText().toString() + mPhoneNumber.getText().toString();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                completeNumber,
                INTERVALLO,
                TimeUnit.SECONDS,
                this,
                mCallbacks
        );
    }
}
