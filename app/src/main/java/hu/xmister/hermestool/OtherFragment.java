package hu.xmister.hermestool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
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
        if ( self == null ) {
            self = new OtherFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            self.setArguments(args);
        }
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
        if (schedulers != null) {
            List<String> al = Arrays.asList(schedulers);
            String tmp;
            if (al.contains("noop")) {
                tmp="noop";
            }
            else if (al.contains("deadline")) {
                tmp="deadline";
            }
            else {
                tmp="cfq";
            }
            a.setP("sched",tmp);
            sched.setText(tmp);
        }
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
        //if ( a.isSuperSU ) {
            btFormat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(a);
                    builder.setTitle(getString(R.string.warning))
                            .setMessage(getString(R.string.format_sd_message))
                            .setPositiveButton(getString(R.string.do_format), new DialogInterface.OnClickListener() {
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
                                                        builder.setTitle(getString(R.string.missing_binaries))
                                                                .setMessage(getString(R.string.missing_binaries_message))
                                                                .show();
                                                    }
                                                });
                                            } else {
                                                if (SUCommand.linkBusybox(a) == false ) {
                                                    a.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                            builder.setTitle(getString(R.string.busybox_install_failed))
                                                                    .setMessage(getString(R.string.busybox_install_failed_message))
                                                                    .show();
                                                        }
                                                    });
                                                }else {
                                                    a.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(a, "Please wait...", Toast.LENGTH_LONG).show();
                                                            a.findViewById(R.id.container).setVisibility(View.GONE);
                                                            ((TextView)a.findViewById(R.id.tStatus)).setText(getString(R.string.take_a_minute));
                                                            a.findViewById(R.id.prog).setVisibility(View.VISIBLE);
                                                            if (SUCommand.uMountSD()) {
                                                                        SUCommand.formatSD(new Shell.OnCommandResultListener() {
                                                                            @Override
                                                                            public void onCommandResult(int commandCode, int exitCode, final List<String> output) {
                                                                                if (exitCode == 0) {
                                                                                    a.setP("cbAutoMount", "true");
                                                                                    cbAutoMount.setChecked(true);
                                                                                    a.saveValues();
                                                                                    SUCommand.mountSD();
                                                                                    a.runOnUiThread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                                            builder.setTitle(getString(R.string.format_complete))
                                                                                                    .setMessage(getString(R.string.format_complete_message))
                                                                                                    .show();
                                                                                            a.findViewById(R.id.prog).setVisibility(View.GONE);
                                                                                            a.findViewById(R.id.container).setVisibility(View.VISIBLE);
                                                                                        }
                                                                                    });
                                                                                }
                                                                                else {
                                                                                    a.runOnUiThread(new Runnable() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                                            builder.setTitle(getString(R.string.error))
                                                                                                    .setMessage(TextUtils.join("\n", output))
                                                                                                    .show();
                                                                                            a.findViewById(R.id.prog).setVisibility(View.GONE);
                                                                                            a.findViewById(R.id.container).setVisibility(View.VISIBLE);
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                }
                                                            else {
                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                        builder.setTitle(getString(R.string.unable_umount))
                                                                                .setMessage(getString(R.string.unable_umount_message))
                                                                                .show();
                                                                a.findViewById(R.id.prog).setVisibility(View.GONE);
                                                                a.findViewById(R.id.container).setVisibility(View.VISIBLE);
                                                            }
                                                    }});

                                                }
                                            }
                                        }
                                    });
                                }
                            })
                            .setNegativeButton(getString(R.string.nothanks), null);
                    builder.show();
                }
            });
            btMount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SUCommand.mountSD(new Shell.OnCommandResultListener() {
                        @Override
                        public void onCommandResult(int commandCode, final int exitCode, final List<String> output) {
                            a.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (exitCode > 0) {
                                        StringBuilder sb = new StringBuilder();
                                        for (String line: output) {
                                            sb.append(line+"\n");
                                        }
                                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                        builder.setTitle(getString(R.string.mount_label))
                                                .setMessage(getString(R.string.mount_error) + "\n"+sb.toString())
                                                .show();
                                    }
                                    else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                        builder.setTitle(getString(R.string.mount_label))
                                                .setMessage(getString(R.string.mount_success))
                                                .show();
                                    }
                                }
                            });
                        }
                    });
                }
            });
        /*}
        else {
            btFormat.setEnabled(false);
            btMount.setEnabled(false);
        }*/
        sched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceDialog md = new ChoiceDialog("Scheduler", schedulers, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectSched = which;
                        sched.setText(schedulers[selectSched]);
                        a.setP("sched", schedulers[selectSched]);
                    }
                }, null, null);
                md.show(getFragmentManager(), "scheduler");
            }
        });
        super.onViewStateRestored(savedInstanceState);
    }
}
