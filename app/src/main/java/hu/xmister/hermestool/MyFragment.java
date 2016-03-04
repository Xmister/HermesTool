package hu.xmister.hermestool;


import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;

public abstract class MyFragment extends Fragment {
    public abstract void loadDefaults();
    protected MainActivity a;
    public void loadValues() { loadDefaults();}
    public void beforeSave() {};

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        loadValues();
    }

    @Override
    public void onStart() {
        a=(MainActivity)getActivity();
        super.onStart();
    }

    @Override
    public void onStop() {
        a=null;
        super.onStop();
    }

    protected void runOnUiThread(Runnable r) {
        if (a != null) {
            a.runOnUiThread(r);
        }
    }

    protected void dialog(String title, String message) {
        if (a != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(a);
            builder.setTitle(title)
                    .setMessage(message)
                    .show();
        }
    }

    protected void dialog(int titleID, int messageID) {
        if (a != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(a);
            builder.setTitle(titleID)
                    .setMessage(messageID)
                    .show();
        }
    }
}
