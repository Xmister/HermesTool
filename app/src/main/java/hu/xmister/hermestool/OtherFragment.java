package hu.xmister.hermestool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import eu.chainfire.libsuperuser.Shell;


public class OtherFragment extends MyFragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static OtherFragment self=null;
    private static CheckBox cbAutoMount;
    private static Button   btFormat,
                            btMount,
                            sched,
                            flash_recovery,
                            convert_backup;
    private static String[] schedulers=null;
    private static int selectSched=0;
    public int flashRec=-1;
    public final DialogInterface.OnClickListener recButtonOnClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            flashRecovery(getActivity(),which);
        }
    };
    public final Shell.OnCommandResultListener ocr_Recovery = new Shell.OnCommandResultListener() {
        @Override
        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
            if (exitCode == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog(R.string.set_inter_suc,R.string.recovery_flash_success);
                        flash_recovery.setEnabled(true);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog(R.string.error, R.string.recovery_flash_error);
                        flash_recovery.setEnabled(true);
                    }
                });
            }
        }
    };

    public void flashRecovery(Context ctx, int which) {
        AlertDialog waitDialog = new AlertDialog.Builder(ctx)
                .setTitle(R.string.recovery_download)
                .setCancelable(false)
                .setMessage(R.string.please_wait)
                .create();
        waitDialog.setCanceledOnTouchOutside(false);
        // Start the download service (if required)
        Intent notifierIntent = new Intent(ctx, MainActivity.class);
        notifierIntent.setAction("download");
        notifierIntent.putExtra("which",which);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0,
                notifierIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            int startResult =
                    DownloaderClientMarshaller.startDownloadServiceIfRequired(ctx,
                            pendingIntent, DownloaderService.class);
            if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
                pendingIntent.send();
            }
            else {
                flash_recovery.setEnabled(false);
                if (which == 0) {
                    SUCommand.flashTWRP(ctx,ocr_Recovery);
                } else if (which == 1) {
                    SUCommand.flashMIRecovery(ctx,ocr_Recovery);
                }
            }
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(R.string.recovery_download)
                    .setMessage(R.string.recovery_download_error)
                    .show();
        }
        waitDialog.dismiss();
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OtherFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OtherFragment newInstance(int sectionNumber) {
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
            a.setP("sched", tmp);
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
                    runOnUiThread(new Runnable() {
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

    private void showRecoveryDialog() {
        ChoiceDialog md = new ChoiceDialog(getString(R.string.choose_recovery), new String[]{"TWRP3", "MiRecovery"}, recButtonOnClick, null, null);
        md.show(getFragmentManager(), "recovery");
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        cbAutoMount=(CheckBox)a.findViewById(R.id.cbAutoMount);
        btFormat=(Button)a.findViewById(R.id.btFormat);
        btMount=(Button)a.findViewById(R.id.btMount);
        sched=(Button)a.findViewById(R.id.sched);
        flash_recovery =(Button)a.findViewById(R.id.b_flash_recovery);
        convert_backup =(Button)a.findViewById(R.id.convert_backup);
        cbAutoMount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                a.setP("cbAutoMount", "" + isChecked);
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
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                        builder.setTitle(R.string.missing_binaries)
                                                                .setMessage(getString(R.string.missing_binaries_message))
                                                                .show();
                                                    }
                                                });
                                            } else {
                                                if (SUCommand.linkBinaries(a) == false ) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                            builder.setTitle(getString(R.string.busybox_install_failed))
                                                                    .setMessage(getString(R.string.busybox_install_failed_message))
                                                                    .show();
                                                        }
                                                    });
                                                }else {
                                                    runOnUiThread(new Runnable() {
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
                                                                                    SUCommand.mountSD(new Shell.OnCommandResultListener() {
                                                                                        @Override
                                                                                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                                                                            runOnUiThread(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    a.setP("cbAutoMount", "true");
                                                                                                    cbAutoMount.setChecked(true);
                                                                                                    a.saveValues();
                                                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                                                    builder.setTitle(getString(R.string.format_complete))
                                                                                                            .setMessage(getString(R.string.format_complete_message))
                                                                                                            .show();
                                                                                                    a.findViewById(R.id.prog).setVisibility(View.GONE);
                                                                                                    a.findViewById(R.id.container).setVisibility(View.VISIBLE);
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });
                                                                                }
                                                                                else {
                                                                                    runOnUiThread(new Runnable() {
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (exitCode > 0) {
                                        StringBuilder sb = new StringBuilder();
                                        for (String line : output) {
                                            sb.append(line + "\n");
                                        }
                                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                        builder.setTitle(getString(R.string.mount_label))
                                                .setMessage(getString(R.string.mount_error) + "\n" + sb.toString())
                                                .show();
                                    } else {
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
        flash_recovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SUCommand.linkBinaries(a) == false) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                            builder.setTitle(getString(R.string.busybox_install_failed))
                                    .setMessage(getString(R.string.busybox_install_failed_message))
                                    .show();
                            flash_recovery.setEnabled(true);
                        }
                    });
                } else {
                    showRecoveryDialog();
                }
            }
            }

            );

            convert_backup.setOnClickListener(new View.OnClickListener()

                                              {
                                                  @Override
                                                  public void onClick(View v) {
                                                      convert_backup.setEnabled(false);
                /*File storagePath=new File("/storage");
                if (storagePath.isDirectory()) {
                    for (String storage :storagePath.list() ) {
                        File storage_F=new File(storage);
                        if (storage_F.isDirectory() && storage_F.getName().startsWith("sdcard")) {
                            for ( String sdcard : storage_F.list() ) {
                                File sdcard_F=new File(sdcard);
                                if (sdcard_F.getName().toLowerCase().equals("twrp")) {
                                    for (String twrp : sdcard_F.list()) {
                                        File twrp_F = new File(twrp);
                                        if (twrp_F.getName().toLowerCase().equals("backups")) {
                                            for (String backup : twrp_F.list() ) {
                                                final File backup_F = new File(backup);
                                                if (backup_F.getName().toLowerCase().equals("reno2")) {
                                                    SUCommand.executeSu("mv " + backup_F.getAbsolutePath() + " " + backup_F.getParent() + "/Redmi_Note_2", new Shell.OnCommandResultListener() {
                                                        @Override
                                                        public void onCommandResult(int commandCode, final int exitCode, List<String> output) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (exitCode == 0) {
                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                        builder.setTitle(R.string.set_inter_suc)
                                                                                .setMessage(R.string.convert_success+": "+backup_F.getAbsolutePath())
                                                                                .show();
                                                                    }
                                                                    else {
                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                        builder.setTitle(R.string.error)
                                                                                .setMessage(R.string.convert_failed+": "+backup_F.getAbsolutePath())
                                                                                .show();
                                                                    }
                                                                    convert_backup.setEnabled(true);
                                                                }
                                                            });
                                                        }
                                                    });
                                                    found=true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //Only if not found
                if (!found) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(a);
                    builder.setTitle(R.string.error)
                            .setMessage(R.string.no_backup)
                            .show();
                    convert_backup.setEnabled(true);
                }*/
                                                      if (SUCommand.linkBinaries(a) == false) {
                                                          runOnUiThread(new Runnable() {
                                                              @Override
                                                              public void run() {
                                                                  AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                  builder.setTitle(getString(R.string.busybox_install_failed))
                                                                          .setMessage(getString(R.string.busybox_install_failed_message))
                                                                          .show();
                                                                  convert_backup.setEnabled(true);
                                                              }
                                                          });
                                                      } else {
                                                          SUCommand.renameTWRPBackup(new Shell.OnCommandResultListener() {
                                                              @Override
                                                              public void onCommandResult(int commandCode, final int exitCode, List<String> output) {
                                                                  getActivity().runOnUiThread(new Runnable() {
                                                                      @Override
                                                                      public void run() {
                                                                          if (exitCode == 0) {
                                                                              AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                              builder.setTitle(R.string.set_inter_suc)
                                                                                      .setMessage(R.string.convert_success)
                                                                                      .show();
                                                                          } else {
                                                                              AlertDialog.Builder builder = new AlertDialog.Builder(a);
                                                                              builder.setTitle(R.string.error)
                                                                                      .setMessage(R.string.convert_failed)
                                                                                      .show();
                                                                          }
                                                                          convert_backup.setEnabled(true);
                                                                      }
                                                                  });

                                                              }
                                                          });
                                                      }
                                                  }

                                              }

            );

            super.

            onViewStateRestored(savedInstanceState);
        }
    }
