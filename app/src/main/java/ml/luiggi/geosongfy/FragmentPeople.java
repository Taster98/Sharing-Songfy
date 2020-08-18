package ml.luiggi.geosongfy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentPeople extends Fragment {
    private TextView peopleTextView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_people,container,false);
        initPeopleFragment(v);
        return v;
    }

    private void initPeopleFragment(View v){
        peopleTextView = (TextView)v.findViewById(R.id.titolo_people);
        peopleTextView.setText("Ascolta coi tuoi amici!");
    }
}
