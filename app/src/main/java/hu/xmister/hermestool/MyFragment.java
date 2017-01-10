package hu.xmister.hermestool;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        a=(MainActivity) activity;
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

    protected void setP(String param, String value) {
        if (a != null) {
            a.setP(param, value);
        }
    }

    protected String getP(String param) {
        if (a != null) {
            return a.getP(param);
        }
        return null;
    }

    protected void finish() {
        if (a != null) {
            a.finish();
        }
    }
}
