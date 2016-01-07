package hu.xmister.hermestool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;


public class OtherFragment extends MyFragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static OtherFragment self=null;
    private static MainActivity a;
    private static CheckBox cbAutoMount;
    private static Button   btFormat,
                            btMount;



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OtherFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OtherFragment newInstance(int sectionNumber) {
        if ( self == null )
            self = new OtherFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        self.setArguments(args);
        return self;
    }

    public OtherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_other, container, false);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
        a = (MainActivity) activity;
    }


    @Override
    public void loadDefaults() {
        a.setP("cbAutoMount", "false");
        cbAutoMount.setChecked(false);
    }

    @Override
    public void loadValues() {
        if (a.getP("cbAutoMount") != null) {
            cbAutoMount.setChecked(Boolean.valueOf(a.getP("cbAutoMount")));
        } else super.loadValues();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        cbAutoMount=(CheckBox)a.findViewById(R.id.cbAutoMount);
        btFormat=(Button)a.findViewById(R.id.btFormat);
        btMount=(Button)a.findViewById(R.id.btMount);
        cbAutoMount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                a.setP("cbAutoMount",""+isChecked);
            }
        });
        btFormat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(a);
                builder.setTitle("WARNING")
                        .setMessage("This will format your SD Card, and erase all data on it! Only continue if you made backup, and know what your are doing! Make sure you have busybox installed!")
                        .setPositiveButton("Format card", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SUCommand.formatSD(new Shell.OnCommandResultListener() {
                                    @Override
                                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                        a.setP("cbAutoMount", "true");
                                        cbAutoMount.setChecked(true);
                                        a.saveValues();
                                        a.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                builder.setTitle("Format completed")
                                                        .setMessage("SD card formatted and set for auto-mount on each reboot. Please check if you have enabled autostart for this app!")
                                                        .show();
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No, thanks", null);
                builder.show();
            }
        });
        btMount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SUCommand.mountSD(new Shell.OnCommandResultListener() {
                    @Override
                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                        builder.setTitle("Mount completed")
                                .setMessage("SD card mounted.")
                                .show();
                    }
                });
            }
        });
        super.onViewStateRestored(savedInstanceState);
    }
}
