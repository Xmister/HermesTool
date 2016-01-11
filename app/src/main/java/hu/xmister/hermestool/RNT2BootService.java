package hu.xmister.hermestool;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class RNT2BootService extends Service {
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    private void sendNotify(String text) {
        // prepare intent which is triggered if the
// notification is selected

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

// build notification
// the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("Redmi Note 2 Tool")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
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
                    SUCommand.mountSD(new Shell.OnCommandResultListener() {
                        @Override
                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                            if ( exitCode > 0 ) {
                                sendNotify(getString(R.string.mount_error));
                            }
                            else {
                                sendNotify(getString(R.string.mount_success));
                            }
                        }
                    });
                    Log.i("Boot Service-MT", "Done");
                }
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {}
                if ( Boolean.valueOf(sharedPreferences.getString("onboot","false"))) {
                    Log.i("Boot Service-IT", "Updating values...");
                    if (Constants.getFrequencyNames() == null)  {
                        Log.e("Boot Service-IT", "Couldn't load frequency values...");
                        sendNotify(getString(R.string.freq_error));
                    }
                    else {
                        SUCommand.interTweak(RNT2BootService.this, new Shell.OnCommandResultListener() {
                            @Override
                            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                                if ( exitCode > 0 ) {
                                    sendNotify(getString(R.string.apply_error));
                                }
                                else {
                                    sendNotify(getString(R.string.apply_success));
                                }
                            }
                        });
                    }
                    Log.i("Boot Service-IT", "Done");
                }
            }
        }).start();
        return super.onStartCommand(pIntent, flags, startId);
    }
}
