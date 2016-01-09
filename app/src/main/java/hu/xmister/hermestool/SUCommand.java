package hu.xmister.hermestool;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import eu.chainfire.libsuperuser.Shell;
import eu.chainfire.libsuperuser.StreamGobbler;

public class SUCommand {
    /**
     * Executes command in SuperUser Shell
     * @param cmd the command to execute
     * @return the result of the execution
     */
    public static void executeSu(final String cmd, Shell.OnCommandResultListener ll) {
        /*try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(cmd);
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            return p.exitValue();
        }
        catch (Exception e) {
            return -1;
        }*/
        final Shell.Builder builder = new Shell.Builder();
        builder.addCommand(cmd, 0, ll);
        builder.useSU();
        Thread abc = new Thread(new Runnable() {
            @Override
            public void run() {
                Shell.Interactive sh=builder.open();
                sh.close();
            }
        });
        abc.start();
    }


    public static void executeSu(final String[] cmds, Shell.OnCommandResultListener ll) {
        final Shell.Builder builder = new Shell.Builder();
        builder.addCommand(cmds, 0, ll);
        builder.useSU();
        Thread abc = new Thread(new Runnable() {
            @Override
            public void run() {
                Shell.Interactive sh=builder.open();
                sh.close();
            }
        });
        abc.start();
    }

    public static void mountSD(Shell.OnCommandResultListener ll) {
        String cmds[] = {
                "e2fsck -fy /dev/block/mmcblk1p1",
                "mount -o remount,rw /",
                "mkdir /mnt/media_rw/sdcard1",
                "chown media_rw:media_rw /mnt/media_rw/sdcard1",
                "chmod 777 media_rw:media_rw /mnt/media_rw/sdcard1",
                "mkdir /storage/sdcard1",
                "chown media_rw:media_rw /storage/sdcard1",
                "chmod 777 media_rw:media_rw /storage/sdcard1",
                "mount -o remount,ro /",
                "mount -rw -t ext4 -o noatime /dev/block/mmcblk1p1 /mnt/media_rw/sdcard1",
                "/system/bin/sdcard -u 1023 -g 1023 -d /mnt/media_rw/sdcard1 /storage/sdcard1 &",
                "/system/bin/vold &",
                "supolicy --live \"allow sdcardd unlabeled dir { append create execute write relabelfrom link unlink ioctl getattr setattr read rename lock mounton quotaon swapon rmdir audit_access remove_name add_name reparent execmod search open }\"",
                "supolicy --live \"allow sdcardd unlabeled file { append create write relabelfrom link unlink ioctl getattr setattr read rename lock mounton quotaon swapon audit_access open }\"",
                "supolicy --live \"allow unlabeled unlabeled filesystem associate\"",
        };
        final Shell.Builder builder = new Shell.Builder();
        builder.addCommand(cmds, 0, ll);
        builder.useSU();
        builder.setShell(Shell.SU.shellMountMaster());
        Thread abc = new Thread(new Runnable() {
            @Override
            public void run() {
                Shell.Interactive sh=builder.open();
                sh.close();
            }
        });
        abc.start();
    }

    public static void mountSD() {
        mountSD(null);
    }

    public static void formatSD(Shell.OnCommandResultListener ll) {
        String cmds[] = {
                "umount /storage/sdcard1",
                "umount /storage/sdcard2",
                "killall sdcard",
                "umount /mnt/media_rw/sdcard1",
                "mke2fs -t ext4 -m 0 /dev/block/mmcblk1p1",
        };
        executeSu(cmds,ll);
    }

    public static void interTweak(Context context) {
        SharedPreferences sharedPreferences =context.getSharedPreferences("default", 0);
        String cmds[] = {
                "cd /proc/cpufreq",
                "echo "+Constants.frequencyItems[Integer.valueOf(sharedPreferences.getString("maxfreq","0"))]+"000 > cpufreq_limited_max_freq_by_user",
                "cd /sys/devices/system/cpu/cpufreq/interactive",
                "echo \"5000\" > timer_rate",
                "echo \"806000\" > hispeed_freq",
                "echo \"10000 1183000:20000 1326000:25000 1469000:20000\" > above_hispeed_delay",
                "echo \"5000\" > min_sample_time",
                "echo \"800000\" > timer_slack",
                "echo \"93 806000:95 1183000:95 1326000:96 1469000:98\" >  target_loads",
                "echo \"99\" > go_hispeed_load",
        };
        executeSu(cmds,null);
        if ( sharedPreferences.contains("sched") ) {
            cmds=new String[]{
                    "cd /sys/block/mmcblk0/queue",
                    "echo "+sharedPreferences.getString("sched","cfq")+" > scheduler",
                    "cd /sys/block/mmcblk1/queue",
                    "echo "+sharedPreferences.getString("sched","cfq")+" > scheduler",
            };
            executeSu(cmds,null);
        }
    }
    public static void saveTouchBoost(String cores, String freq, Shell.OnCommandResultListener ll) {
        String cmds[] = {
                "mount -o remount,rw /system",
                "cd /system/etc",
                "cat perfservscntbl.bak || cp -i perfservscntbl.txt perfservscntbl.bak",
                "echo \"CMD_SET_CPU_CORE, SCN_APP_TOUCH, "+cores+"\nCMD_SET_CPU_FREQ, SCN_APP_TOUCH, "+Constants.frequencyItems[Integer.valueOf(freq)]+"000\n\" > perfservscntbl.txt",
                "chmod 644 perfservscntbl.txt",
                "mount -o remount,ro /system",
        };
        executeSu(cmds,ll);
    }

    public static /*List<String>*/ void getTouchBoost(Shell.OnCommandResultListener ll) {
        String cmds[] = {
                "cd /system/etc",
                "cat perfservscntbl.txt",
        };
        executeSu(cmds,ll);
    }
}
