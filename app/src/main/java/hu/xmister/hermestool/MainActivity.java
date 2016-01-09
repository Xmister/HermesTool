package hu.xmister.hermestool;

import android.app.Activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import eu.chainfire.libsuperuser.Shell;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

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
    public static boolean isSuperSU=false;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private MyFragment curFrag=null;

    private void init() {
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences =getSharedPreferences("default", 0);
        Map<String, String> all;
        all=(Map<String, String>)sharedPreferences.getAll();
        for (String key : all.keySet()) {
            String value = all.get(key);
            p.setProperty(key,value);
        }
        onBoot=Boolean.valueOf(sharedPreferences.getString("onboot","false"));
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        findViewById(R.id.container).setVisibility(View.INVISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (Shell.SU.available()) {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.container).setVisibility(View.VISIBLE);
                        }
                    });
                }
            }).start();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fT=fragmentManager.beginTransaction();
        switch (position) {
            case 0:
                curFrag = MainFragment.newInstance(position + 1);
                break;
            case 1:
                curFrag = OtherFragment.newInstance(position + 1);
                break;
        }
        fT.replace(R.id.container, curFrag);
        fT.commit();
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
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_setOnBoot).setChecked(onBoot);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
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
        curFrag.loadDefaults();
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
                item.setChecked(!item.isChecked());
                onBoot=item.isChecked();
                return true;
            case R.id.action_save:
                saveValues();
                SUCommand.interTweak(this);
                if ( getP("cbTouchBoost").equals("true") ) {
                    SUCommand.getTouchBoost(new SUCommand.tbCallback() {
                        @Override
                        public void onGotTB(String freq, String cores) {
                            if (freq == null || cores == null) return;
                            if (!freq.equals(Constants.frequencyItems[Integer.valueOf(getP("tbFreq"))]+ "000") || !cores.equals(getP("tCores"))) {
                                updateTB();
                            }
                        }
                    });
                }
                return true;
            case R.id.action_debug:
                SUCommand.executeSu(new String[]{"mke3fs","mke2fs"}, new Shell.OnCommandResultListener() {
                    @Override
                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                        return;
                    }
                });
        }


        return super.onOptionsItemSelected(item);
    }

    private void updateTB() {
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
                                        SUCommand.saveTouchBoost(getP("tCores"), getP("tbFreq"), new Shell.OnCommandResultListener() {
                                            @Override
                                            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                                SUCommand.getTouchBoost(new SUCommand.tbCallback() {
                                                    @Override
                                                    public void onGotTB(String freq, String cores) {
                                                        boolean error=false;
                                                            if (freq == null || cores == null) error=true;
                                                            else if (!freq.equals(Constants.frequencyItems[Integer.valueOf(getP("tbFreq"))]+"000") || !cores.equals(getP("tCores")) ) {
                                                                error=true;
                                                            }
                                                        if (error) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                                    builder.setTitle(getString(R.string.sys_mod_error))
                                                                            .setMessage(getString(R.string.sys_mod_error_message))
                                                                            .setPositiveButton("OK", null);
                                                                    builder.show();
                                                                }
                                                            });
                                                        } else {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                                    builder.setTitle(getString(R.string.sys_mod_success))
                                                                            .setMessage(getString(R.string.sys_mod_success_message))
                                                                            .setPositiveButton(getString(R.string.reboot_now), new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    SUCommand.executeSu("sync;reboot", null);
                                                                                }
                                                                            })
                                                                            .setNegativeButton(getString(R.string.reboot_later), null);
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
                        .setNegativeButton("Cancel",null);
                builder.show();
            }
        });

    }

    public void saveValues() {
        curFrag.beforeSave();
        p.setProperty("onboot",""+onBoot);
        SharedPreferences sharedPreferences =getSharedPreferences("default", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : p.stringPropertyNames()) {
            String value=p.getProperty(key);
            editor.putString(key,value);
        }
        editor.commit();
    }


}
