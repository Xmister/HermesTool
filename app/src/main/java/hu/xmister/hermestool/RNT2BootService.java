package hu.xmister.hermestool;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class RNT2BootService extends Service {
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent pIntent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.i("HermesTools", "RNT2Service");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {}
                SharedPreferences sharedPreferences =RNT2BootService.this.getSharedPreferences("default", 0);
                if ( Boolean.valueOf(sharedPreferences.getString("cbAutoMount","false"))) {
                    Log.i("Boot Service-MT", "Mounting SD card...");
                    SUCommand.mountSD();
                    Log.i("Boot Service-MT", "Done");
                }
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {}
                if ( Boolean.valueOf(sharedPreferences.getString("onboot","false"))) {
                    Log.i("Boot Service-IT", "Updating values...");
                    if (Constants.getFrequencyNames() == null)  {
                        Log.e("Boot Service-IT", "Couldn't load frequency values...");
                    }
                    else {
                        SUCommand.interTweak(RNT2BootService.this, null);
                    }
                    Log.i("Boot Service-IT", "Done");
                }
            }
        }).start();
        return super.onStartCommand(pIntent, flags, startId);
    }
}
