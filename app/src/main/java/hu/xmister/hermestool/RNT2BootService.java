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
                Log.w("boot_broadcast_poc", "Updating values...");
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {}
                SUCommand.mountSD();
                SUCommand.interTweak(RNT2BootService.this);
                Log.w("boot_broadcast_poc", "Done...");
            }
        }).start();
        return super.onStartCommand(pIntent, flags, startId);
    }
}
