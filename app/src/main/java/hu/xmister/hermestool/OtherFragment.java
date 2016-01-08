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
import java.util.StringTokenizer;

import eu.chainfire.libsuperuser.Shell;


public class OtherFragment extends MyFragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static OtherFragment self=null;
    private static MainActivity a;
    private static CheckBox cbAutoMount;
    private static Button   btFormat,
                            btMount,
                            sched;
    private static String[] schedulers=null;
    private static int selectSched=0;


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
        if (schedulers == null) {
            SUCommand.executeSu(new String[]{"cd /sys/block/mmcblk0/queue", "cat scheduler"}, new Shell.OnCommandResultListener() {
                @Override
                public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                    for (String line: output) {
                        if (line.length()>1) {
                            StringTokenizer st=new StringTokenizer(line," ");
                            int i=0;
                            schedulers=new String[st.countTokens()];
                            while (st.hasMoreTokens()) {
                                String curr=st.nextToken();
                                if (curr.indexOf('[')==0) {
                                    schedulers[i]=curr.substring(1,curr.length()-1);
                                    selectSched=i;
                                }
                                else schedulers[i]=curr;
                                i++;
                            }
                        }
                    }
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sched.setText(schedulers[selectSched]);
                        }
                    });
                }
            });
        }
        if (a.getP("cbAutoMount") != null) {
            cbAutoMount.setChecked(Boolean.valueOf(a.getP("cbAutoMount")));
            sched.setText(a.getP("sched"));
        } else super.loadValues();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        cbAutoMount=(CheckBox)a.findViewById(R.id.cbAutoMount);
        btFormat=(Button)a.findViewById(R.id.btFormat);
        btMount=(Button)a.findViewById(R.id.btMount);
        sched=(Button)a.findViewById(R.id.sched);
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
                        .setMessage("This will format your SD Card, and erase all data on it! Only continue if you made backup, and know what your are doing!\nMake sure you have busybox installed!\nMake sure you have disabled \"Mount namespace separation\" in SuperSU!")
                        .setPositiveButton("Format card", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SUCommand.executeSu("mke2fs", new Shell.OnCommandResultListener() {
                                    @Override
                                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                        if (exitCode == 127) {
                                            a.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                    builder.setTitle("Missing binaries")
                                                            .setMessage("mke2fs not found! Do you have busybox installed?")
                                                            .show();
                                                }
                                            });
                                        } else {
                                            SUCommand.executeSu("umount /storage/sdcard1", new Shell.OnCommandResultListener() {
                                                @Override
                                                public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                                    if (exitCode == 0) {
                                                        SUCommand.executeSu("umount /mnt/media_rw/sdcard1", new Shell.OnCommandResultListener() {
                                                            @Override
                                                            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                                                if (exitCode == 0) {
                                                                    SUCommand.formatSD(new Shell.OnCommandResultListener() {
                                                                        @Override
                                                                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                                                            a.setP("cbAutoMount", "true");
                                                                            cbAutoMount.setChecked(true);
                                                                            a.saveValues();
                                                                            SUCommand.mountSD();
                                                                            a.runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                                    builder.setTitle("Format completed")
                                                                                            .setMessage("SD card formatted, mounted, and set for auto-mount on each reboot. Please check if you have enabled autostart for this app!")
                                                                                            .show();
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                } else {
                                                                    a.runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                            builder.setTitle("Unable to umount")
                                                                                    .setMessage("Couldn't umount /mnt/media/rw/sdcard1. Please stop every other applications. If the problem persists, make a reboot.")
                                                                                    .show();
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        a.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                builder.setTitle("Unable to umount")
                                                                        .setMessage("Couldn't umount /storage/sdcard1. Please stop every other applications. If the problem persists, make a reboot.")
                                                                        .show();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
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
                        a.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                builder.setTitle("Mount completed")
                                        .setMessage("SD card mounted.")
                                        .show();
                            }
                        });
                    }
                });
            }
        });
        sched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceDialog md = new ChoiceDialog("Scheduler", schedulers, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectSched=which;
                        sched.setText(schedulers[selectSched]);
                        a.setP("sched",schedulers[selectSched]);
                    }
                }, null, null);
                md.show(getFragmentManager(), "scheduler");
            }
        });
        super.onViewStateRestored(savedInstanceState);
    }
}
