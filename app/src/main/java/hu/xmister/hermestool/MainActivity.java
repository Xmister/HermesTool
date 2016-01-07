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

    private Thread needReboot=null;

    private Runnable needRebootRun = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {

            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("System modified")
                            .setMessage("System partition modified. You need to reboot for the changes to take effect")
                            .setPositiveButton("Reboot Now", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SUCommand.executeSu("sync;reboot");
                                }
                            })
                            .setNegativeButton("Reboot Later",null);
                    builder.show();
                }
            });
        }
    };

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private MyFragment curFrag=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        SharedPreferences sharedPreferences =getSharedPreferences("default", 0);
        Map<String, String> all;
        all=(Map<String, String>)sharedPreferences.getAll();
        for (String key : all.keySet()) {
            String value = all.get(key);
            p.setProperty(key,value);
        }
        onBoot=Boolean.valueOf(sharedPreferences.getString("onboot","false"));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fT=fragmentManager.beginTransaction();
        switch (position) {
            case 0:
                curFrag=MainFragment.newInstance(position + 1);
                fT.replace(R.id.container, curFrag);
                break;

        }
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
                    SUCommand.getTouchBoost(new Shell.OnCommandLineListener() {
                        @Override
                        public void onCommandResult(int commandCode, int exitCode) {

                        }

                        @Override
                        public void onLine(String line) {
                            if (line.length()>1) {
                                StringTokenizer st = new StringTokenizer(line,", ");
                                if (st.hasMoreTokens()) {
                                    String token=st.nextToken();
                                    if (token.equals("CMD_SET_CPU_CORE")) {
                                        st.nextToken();
                                        if (st.hasMoreTokens()) {
                                            if (!st.nextToken().equals(getP("tCores"))) {
                                                updateTB();
                                            }
                                        }
                                    }
                                    else if (token.equals("CMD_SET_CPU_FREQ")) {
                                        st.nextToken();
                                        if (st.hasMoreTokens()) {
                                            if (!st.nextToken().equals(Constants.frequencyItems[Integer.valueOf(getP("tbFreq"))]+"000")) {
                                                updateTB();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
                return true;
            case R.id.action_mount:
                SUCommand.mountSD();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void updateTB() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Please wait...", Toast.LENGTH_LONG).show();
            }
        });
        SUCommand.saveTouchBoost(getP("tCores"), getP("tbFreq"));
        if (needReboot == null || !needReboot.isAlive()) {
            needReboot = new Thread(needRebootRun);
            needReboot.start();
        }
    }

    private void saveValues() {
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
