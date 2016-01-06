package hu.xmister.hermestool;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {
        SharedPreferences sharedPreferences =context.getSharedPreferences("default", 0);
        Log.w("boot_broadcast_poc", "Updating values...");
        SUCommand.executeSu("e2fsck -fy /dev/block/mmcblk1p1");
        SUCommand.executeSu("chown media_rw:media_rw /mnt/media_rw/sdcard1");
        SUCommand.executeSu("mount -rw -t ext4 -o noatime /dev/block/mmcblk1p1 /mnt/media_rw/sdcard1");
        SUCommand.executeSu("setenforce 0");
        SUCommand.executeSu("/system/bin/sdcard -u 1023 -g 1023 -d /mnt/media_rw/sdcard1 /storage/sdcard1 &");
        SUCommand.executeSu("/system/bin/vold &");
        Log.w("boot_broadcast_poc", "Done...");
    }

}
