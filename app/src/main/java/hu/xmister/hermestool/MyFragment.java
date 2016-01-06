package hu.xmister.hermestool;


import android.app.Fragment;
import android.os.Bundle;

public abstract class MyFragment extends Fragment {
    public abstract void loadDefaults();
    public void loadValues() { loadDefaults();}
    public void beforeSave() {};

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        loadValues();
    }
}
