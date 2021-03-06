package hu.xmister.hermestool;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;

import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import eu.chainfire.libsuperuser.Shell;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, IDownloaderClient {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private String[] items;

    public String getP(String property) {
        return p.getProperty(property.toLowerCase());
    }

    public void setP(String property, String value) {
        p.setProperty(property.toLowerCase(),value);
    }

    private Properties p = new Properties();
    private boolean onBoot =false;
    private boolean firstInit=true;
    public static boolean isSuperSU=false,
                            noCPU=false;
    private IStub mDownloaderClientStub;
    private IDownloaderService mRemoteService;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private MyFragment curFrag=null;
    private ProgressDialog progress;

    private void init() {
        SharedPreferences sharedPreferences =getSharedPreferences("default", 0);
        Map<String, String> all;
        all=(Map<String, String>)sharedPreferences.getAll();
        for (String key : all.keySet()) {
            String value = all.get(key);
            p.setProperty(key,value);
        }
        onBoot=Boolean.valueOf(sharedPreferences.getString("onboot","false"));
    }

    private void guiInit() {
        if (firstInit) {
            firstInit=false;
            setContentView(R.layout.activity_main);
            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.check_root)
                .setMessage(R.string.check_root_message);
        final AlertDialog rootCheckDialog = builder.create();
        rootCheckDialog.setCancelable(false);
        rootCheckDialog.setCanceledOnTouchOutside(false);
        rootCheckDialog.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (Shell.SU.available()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                rootCheckDialog.dismiss();
                            }
                        });
                        String ver = Shell.SU.version(false);
                        if (!ver.toLowerCase().contains("supersu")) {
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle(getString(R.string.warning))
                                            .setMessage(getString(R.string.no_supersu_message))
                                            .setPositiveButton("OK", null);
                                    builder.show();
                                }
                            });*/
                        } else {
                            isSuperSU = true;
                        }
                        Constants.init(MainActivity.this, new Constants.InitComplete() {
                            @Override
                            public void onInitComplete() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        guiInit();
                                        if (Constants.getFrequencyName(MainActivity.this, 0) == null) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                            builder.setTitle(getString(R.string.no_scaling))
                                                    .setMessage(getString(R.string.no_scaling_message))
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            finish();
                                                        }
                                                    })
                                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                        @Override
                                                        public void onDismiss(DialogInterface dialog) {
                                                            finish();
                                                        }
                                                    });
                                            builder.show();
                                        } else {
                                            findViewById(R.id.container).setVisibility(View.INVISIBLE);
                                        }
                                    }
                                });
                                if (!noCPU) {
                                    SUCommand.executeSu("cd /sys/devices/system/cpu/cpufreq/interactive", new Shell.OnCommandResultListener() {
                                        @Override
                                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                            if (exitCode > 0) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                        builder.setTitle(getString(R.string.no_inter))
                                                                .setMessage(getString(R.string.no_inter_message))
                                                                .setPositiveButton(R.string.change_inter, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        SUCommand.executeSu(new String[]{
                                                                                "chmod 644 /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                                                                                "chmod 644 /sys/devices/system/cpu/cpu1/cpufreq/scaling_governor",
                                                                                "chmod 644 /sys/devices/system/cpu/cpu2/cpufreq/scaling_governor",
                                                                                "chmod 644 /sys/devices/system/cpu/cpu3/cpufreq/scaling_governor",
                                                                                "chmod 644 /sys/devices/system/cpu/cpu4/cpufreq/scaling_governor",
                                                                                "chmod 644 /sys/devices/system/cpu/cpu5/cpufreq/scaling_governor",
                                                                                "chmod 644 /sys/devices/system/cpu/cpu6/cpufreq/scaling_governor",
                                                                                "chmod 644 /sys/devices/system/cpu/cpu7/cpufreq/scaling_governor",
                                                                                "echo 'interactive' > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
                                                                                "echo 'interactive' > /sys/devices/system/cpu/cpu1/cpufreq/scaling_governor",
                                                                                "echo 'interactive' > /sys/devices/system/cpu/cpu2/cpufreq/scaling_governor",
                                                                                "echo 'interactive' > /sys/devices/system/cpu/cpu3/cpufreq/scaling_governor",
                                                                                "echo 'interactive' > /sys/devices/system/cpu/cpu4/cpufreq/scaling_governor",
                                                                                "echo 'interactive' > /sys/devices/system/cpu/cpu5/cpufreq/scaling_governor",
                                                                                "echo 'interactive' > /sys/devices/system/cpu/cpu6/cpufreq/scaling_governor",
                                                                                "echo 'interactive' > /sys/devices/system/cpu/cpu7/cpufreq/scaling_governor",
                                                                        }, new Shell.OnCommandResultListener() {
                                                                            @Override
                                                                            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                                                                SUCommand.executeSu("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", new Shell.OnCommandResultListener() {
                                                                                    @Override
                                                                                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                                                                        for (String line : output) {
                                                                                            if (line.length() > 0 && line.trim().equals("interactive")) {
                                                                                                runOnUiThread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                                                                        builder.setTitle(getString(R.string.set_inter_suc))
                                                                                                                .setMessage(getString(R.string.set_inter_suc_message))
                                                                                                                .setPositiveButton("OK", null);
                                                                                                        builder.show();
                                                                                                    }
                                                                                                });
                                                                                            } else {
                                                                                                runOnUiThread(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                                                                        builder.setTitle(getString(R.string.set_inter_fail))
                                                                                                                .setMessage(getString(R.string.set_inter_fail_message))
                                                                                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                                                                    @Override
                                                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                                                        finish();
                                                                                                                    }
                                                                                                                })
                                                                                                                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                                                                                    @Override
                                                                                                                    public void onDismiss(DialogInterface dialog) {
                                                                                                                        finish();
                                                                                                                    }
                                                                                                                });
                                                                                                        builder.show();
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                })
                                                                .setNeutralButton(getString(R.string.disable_cpu), new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        noCPU = true;
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                onNavigationDrawerItemSelected(1);
                                                                                onSectionAttached(2);
                                                                            }
                                                                        });
                                                                    }
                                                                })
                                                                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        finish();
                                                                    }
                                                                })
                                                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                                    @Override
                                                                    public void onCancel(DialogInterface dialog) {
                                                                        finish();
                                                                    }
                                                                });
                                                        builder.show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.container).setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(getString(R.string.no_root))
                                        .setMessage(getString(R.string.no_root_message))
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                finish();
                                            }
                                        });
                                builder.show();
                            }
                        });
                    }
                }
            }).start();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        if (position==2) {
            String url="https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=xmisterhu@gmail.com&item_name=Donation&currency_code=EUR";
            Intent i=new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
        else {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fT = fragmentManager.beginTransaction();
            switch (position) {
                case 0:
                    if (!noCPU)
                        curFrag = MainFragment.newInstance(position + 1);
                    else
                        curFrag = OtherFragment.newInstance(position + 1);
                    break;
                case 1:
                    curFrag = OtherFragment.newInstance(position + 1);
                    break;
            }
            fT.replace(R.id.container, curFrag);
            fT.commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if ( menu == null ) return false;
        super.onPrepareOptionsMenu(menu);
        if (noCPU) onBoot=false;
        menu.findItem(R.id.action_setOnBoot).setChecked(onBoot);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mNavigationDrawerFragment== null || !mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show frequencyItems in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void loadDefaults() {
        if ( curFrag != null ) {
            curFrag.loadDefaults();
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id)  {
            case R.id.action_default:
                loadDefaults();
                return true;
            case R.id.action_setOnBoot:
                if (!noCPU) {
                    item.setChecked(!item.isChecked());
                    onBoot = item.isChecked();
                }
                return true;
            case R.id.action_save:
                saveValues();
                if (!noCPU) {
                    final Shell.OnCommandResultListener interCB=new Shell.OnCommandResultListener() {
                        @Override
                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                            if (exitCode > 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle(getString(R.string.sys_inter_error))
                                                .setMessage(getString(R.string.sys_inter_error_message))
                                                .setPositiveButton("OK", null);
                                        builder.show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle(getString(R.string.sys_inter_suc))
                                                .setMessage(getString(R.string.sys_inter_suc_message))
                                                .setPositiveButton("OK", null);
                                        builder.show();
                                    }
                                });
                            }
                        }
                    };
                    if (getP("cbTouchBoost").equals("true")) {
                        SUCommand.getTouchBoost(new SUCommand.tbCallback() {
                            @Override
                            public void onGotTB(String freq, String cores) {
                                if (freq == null || cores == null || !freq.equals(Constants.getFrequencyItem(MainActivity.this,Integer.valueOf(getP("tbFreq")))) || !cores.equals(getP("tCores"))) {
                                    updateTB(new SUCommand.tbCallback() {
                                        @Override
                                        public void onGotTB(String freq, String cores) {
                                            SUCommand.interTweak(MainActivity.this, interCB);
                                        }
                                    });
                                }
                                else {
                                    SUCommand.interTweak(MainActivity.this, interCB);
                                }
                            }
                        });
                    }
                    else {
                        SUCommand.interTweak(MainActivity.this, interCB);
                    }
                }
                return true;
            case R.id.action_debug:


        }


        return super.onOptionsItemSelected(item);
    }

    private void updateTB(final SUCommand.tbCallback tb) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getString(R.string.warning))
                        .setMessage(getString(R.string.modify_tb_warning_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SUCommand.saveTouchBoost(MainActivity.this, getP("tCores"), getP("tbFreq"), new Shell.OnCommandResultListener() {
                                            @Override
                                            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                                SUCommand.getTouchBoost(new SUCommand.tbCallback() {
                                                    @Override
                                                    public void onGotTB(final String freq, final String cores) {
                                                        boolean error = false;
                                                        if (freq == null || cores == null)
                                                            error = true;
                                                        else if (!freq.equals(Constants.getFrequencyItem(MainActivity.this, Integer.valueOf(getP("tbFreq")))) || !cores.equals(getP("tCores"))) {
                                                            error = true;
                                                        }
                                                        if (error) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                                    builder.setTitle(getString(R.string.sys_mod_error))
                                                                            .setMessage(getString(R.string.sys_mod_error_message))
                                                                            .setPositiveButton("OK", null)
                                                                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                                                @Override
                                                                                public void onDismiss(DialogInterface dialog) {
                                                                                    tb.onGotTB("0", "0");
                                                                                }
                                                                            });
                                                                    builder.show();
                                                                }
                                                            });
                                                        } else {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    curFrag.loadValues();
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                                    builder.setTitle(getString(R.string.sys_mod_success))
                                                                            .setMessage(getString(R.string.sys_mod_success_message))
                                                                            .setNegativeButton(getString(R.string.reboot_later), null)
                                                                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                                                @Override
                                                                                public void onDismiss(DialogInterface dialog) {
                                                                                    tb.onGotTB(freq, cores);
                                                                                }
                                                                            });
                                                                    builder.show();
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tb.onGotTB("0", "0");
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                tb.onGotTB("0", "0");
                            }
                        });
                builder.show();
            }
        });
    }

    public void saveValues() {
        if (curFrag != null) {
            curFrag.beforeSave();
        }
        p.setProperty("onboot",""+onBoot);
        SharedPreferences sharedPreferences =getSharedPreferences("default", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : p.stringPropertyNames()) {
            String value=p.getProperty(key);
            editor.putString(key,value);
        }
        editor.commit();
    }


    @Override
    public void onServiceConnected(Messenger m) {
        mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
        mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
    }

    private void createProgress() {
        if (progress == null) {
            progress =new ProgressDialog(this);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
        }
    }

    @Override
    public void onDownloadStateChanged(int newState) {
        createProgress();
        AlertDialog.Builder builder;
        switch (newState) {
            case STATE_IDLE:
                mRemoteService.requestContinueDownload();
                break;
            case STATE_CONNECTING:
            case STATE_FETCHING_URL:
                progress.setTitle(R.string.recovery_download);
                progress.setIndeterminate(true);
                progress.show();
                break;
            case STATE_DOWNLOADING:
                progress.setTitle(R.string.recovery_download);
                progress.setIndeterminate(false);
                progress.show();
                break;
            case STATE_COMPLETED:
                progress.setIndeterminate(true);
                onNavigationDrawerItemSelected(1);
                if (getIntent().hasExtra("which")) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                                if (curFrag instanceof OtherFragment) {
                                    ((OtherFragment)curFrag).flashRecovery(MainActivity.this, getIntent().getIntExtra("which", 0));
                                    progress.dismiss();
                                }
                        }
                    },2500);
                }
                else {
                    builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.recovery_download)
                            .setMessage(R.string.recovery_push_again)
                            .show();
                }
                break;
            default:
                progress.dismiss();
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.recovery_download)
                        .setMessage(R.string.recovery_download_error)
                        .show();
        }
    }


    @Override
    public void onDownloadProgress(DownloadProgressInfo progress) {
        createProgress();
        this.progress.setIndeterminate(false);
        this.progress.setMax(100);
        this.progress.setProgress((int) (((double) progress.mOverallProgress / (double) progress.mOverallTotal) * 100.0));
    }

    @Override
    protected void onResume() {
        if (getIntent() != null && getIntent().getAction() != null && getIntent().getAction().equals("download")) {
            mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this,
                    DownloaderService.class);
            if (null != mDownloaderClientStub) {
                mDownloaderClientStub.connect(this);
            }
        }
        super.onResume();
    }


    @Override
    protected void onStop() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.disconnect(this);
        }
        super.onStop();
    }
}
