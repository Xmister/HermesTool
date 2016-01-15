package hu.xmister.hermestool;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

import eu.chainfire.libsuperuser.Shell;

public class SUCommand {
    private static String bb=null;
    private static boolean umountOk =false;
    public interface tbCallback {
        public void onGotTB(String freq, String cores);
    }
    /**
     * Executes command in SuperUser Shell
     * @param cmd the command to execute
     * @return the result of the execution
     */
    public static void executeSu(final String cmd, Shell.OnCommandResultListener ll, int wait) {
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
        if (wait > 0) try {
            abc.join(wait);
        } catch (Exception e) {}
    }

    public static void executeSu(final String cmd, Shell.OnCommandResultListener ll) {
        executeSu(cmd, ll, 0);
    }


    public static void executeSu(final String[] cmds, Shell.OnCommandResultListener ll) {
        executeSu(cmds, ll, 0);
    }

    public static void executeSu(final String[] cmds, Shell.OnCommandResultListener ll, int wait) {
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
        if (wait > 0) try {
            abc.join(wait);
        } catch (Exception e) {}
    }

    public static void mountSD(final Shell.OnCommandResultListener ll) {
        final String cmds1[] = {
                "e2fsck -fy /dev/block/mmcblk1p1",
                "mount -o remount,rw /",
                "mkdir /mnt/media_rw/sdcard1",
                //"chcon u:object_r:rootfs:s1 /mnt/media_rw/sdcard1",
                "chown 1023:1023 /mnt/media_rw/sdcard1",
                "chmod 777 /mnt/media_rw/sdcard1",
                "mkdir /storage/sdcard1",
                "chown 1023:1023 /storage/sdcard1",
                "chmod 777 /storage/sdcard1",
                "mount -o remount,ro /",
                "mount -rw -t ext4 -o noatime /dev/block/mmcblk1p1 /mnt/media_rw/sdcard1",
                //"supolicy --live \"allow sdcardd unlabeled dir { append create execute write relabelfrom link unlink ioctl getattr setattr read rename lock mounton quotaon swapon rmdir audit_access remove_name add_name reparent execmod search open }\"",
                //"supolicy --live \"allow sdcardd unlabeled file { append create write relabelfrom link unlink ioctl getattr setattr read rename lock mounton quotaon swapon audit_access open }\"",
                //"supolicy --live \"allow unlabeled unlabeled filesystem associate\"",
        };
        final String cmds2[] = {
                "chmod 777 /mnt/media_rw/sdcard1",
                "/system/bin/sdcard -u 1023 -g 1023 -d /mnt/media_rw/sdcard1 /storage/sdcard1 &",
                "/system/bin/vold",
                "ture",
        };
        final Shell.Builder builder = new Shell.Builder();
        builder.addCommand(cmds1, 0, new Shell.OnCommandResultListener() {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                if (exitCode > 0) {
                    if ( ll != null ) ll.onCommandResult(commandCode, exitCode, output);
                }
                else {
                    final Shell.Builder builder2 = new Shell.Builder();
                    builder2.addCommand(cmds2, 0, new Shell.OnCommandResultListener() {
                        @Override
                        public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                            if ( ll != null ) ll.onCommandResult(commandCode, 0, output);
                        }
                    });
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            builder2.setShell("su -mm -c sh");
                            Shell.Interactive sh = builder2.open();
                            sh.close();
                        }
                    }).start();
                }
            }
        });
        Thread abc = new Thread(new Runnable() {
            @Override
            public void run() {
                builder.setShell("su -mm -c sh");
                Shell.Interactive sh=builder.open();
                sh.close();
            }
        });
        abc.start();
    }

    public static boolean linkBusybox(Context c) {
        final String oldF=c.getApplicationInfo().dataDir+"/lib/lib_busybox_.so";
        final String newF=c.getApplicationInfo().dataDir+"/lib/busybox";
        SUCommand.executeSu("ln -sf " + oldF + " " + newF, null, 10000);
        File f = new File(newF);
        if (f.exists()) {
            bb=newF;
            return true;
        }
        return false;
    }

    public static void boxExec(final String cmd, Shell.OnCommandResultListener ll) {
        executeSu(bb + " " + cmd, ll);
    }

    public static boolean uMountSD() {
        umountOk =true;
        Thread abc=new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> out = Shell.run("su -mm -c sh", new String[]{bb + " umount /storage/sdcard1"},null,true);
                if (out.size() > 0) {
                    if (out.get(0).contains("busy")) {
                        umountOk = false;
                    } else if (out.get(0).contains("Invalid")) {
                        List<String> out2 = Shell.run("su -mm -c sh", new String[]{bb + " umount /storage/sdcard2"},null,true);
                        if (out2.size() > 0) {
                            if (out2.get(0).contains("busy")) {
                                umountOk = false;
                            }
                        }
                    }
                }
                if ( umountOk ) {
                    List<String> out2 = Shell.run("su -mm -c sh", new String[]{bb + " umount /mnt/media_rw/sdcard1"},null,true);
                    if (out2.size() > 0) {
                        if (out2.get(0).contains("busy")) {
                            umountOk = false;
                        } else if (out2.get(0).contains("Invalid")) {
                            out2 = Shell.run("su -mm -c sh", new String[]{bb + " umount /mnt/media_rw/sdcard2"},null,true);
                            if (out2.size() > 0 && out2.get(0).contains("busy")) {
                                umountOk = false;
                            }
                        }
                    }
                }
            }
        });
        abc.start();
        try {
            abc.join(20000);
        } catch (Exception e) { umountOk =false; }
        return umountOk;
    }

    public static void mountSD() {
        mountSD(null);
    }

    public static void formatSD(Shell.OnCommandResultListener ll) {
        Thread abc = new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> out2 = Shell.run("su -c sh", new String[]{"(echo o; echo n; echo p; echo 1; echo ; echo; echo w) | "+bb+" fdisk /dev/block/mmcblk1"},null,true);
            }
        });
        abc.start();
        try {
            abc.join(60000);
            String cmds[] = {
                    bb+" mke2fs -T ext4 -m 0 /dev/block/mmcblk1p1",
            };
            executeSu(cmds, ll);
        } catch (Exception e) { ll.onCommandResult(0,1,null);}
    }

    public static void interTweak(Context context, Shell.OnCommandResultListener ll) {
        SharedPreferences sharedPreferences =context.getSharedPreferences("default", 0);
        try {
            String cmds[] = {
                    "cd /proc/cpufreq",
                    "chmod 644 cpufreq_limited_max_freq_by_user",
                    "echo " + Constants.getFrequencyItem(Integer.valueOf(sharedPreferences.getString("maxfreq", "0"))) + " > cpufreq_limited_max_freq_by_user",
                    "cd /sys/devices/system/cpu/cpufreq/interactive",
                    "chmod 644 *",
                    "echo \"10000\" > timer_rate",
                    "echo \"806000\" > hispeed_freq",
                    "echo \"10000 1183000:20000 1326000:20000 1469000:40000\" > above_hispeed_delay",
                    "echo \"10000\" > min_sample_time",
                    "echo \"800000\" > timer_slack",
                    "echo \"85 806000:92 1183000:93 1326000:94 1469000:95\" >  target_loads",
                    "echo \"99\" > go_hispeed_load",
            };
            executeSu(cmds, ll);
        } catch (IndexOutOfBoundsException e) {
            if (ll != null) ll.onCommandResult(0,120,null);
        }
        if ( sharedPreferences.contains("sched") ) {
            String cmds[]=new String[]{
                    "cd /sys/block/mmcblk0/queue",
                    "chmod 644 *",
                    "echo "+sharedPreferences.getString("sched","cfq")+" > scheduler",
                    "cd /sys/block/mmcblk1/queue",
                    "chmod 644 *",
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
                "echo \"CMD_SET_CPU_CORE, SCN_APP_TOUCH, "+cores+"\nCMD_SET_CPU_FREQ, SCN_APP_TOUCH, "+Constants.getFrequencyItems()[Integer.valueOf(freq)]+"\n\" > perfservscntbl.txt || exit 1",
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

    public static void getTouchBoost(final tbCallback ll) {
        getTouchBoost(new Shell.OnCommandResultListener() {
            String fr=null, cr=null;
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                for (String line : output) {
                    if ( onLine(line) ) {
                    }
                }
                ll.onGotTB(fr,cr);
            }

            private boolean onLine(String line) {
                if (line.length()>1) {
                    StringTokenizer st = new StringTokenizer(line,",");
                    if (st.hasMoreTokens()) {
                        String token=st.nextToken().trim();
                        if (token.equals("CMD_SET_CPU_CORE")) {
                            st.nextToken();
                            if (st.hasMoreTokens()) {
                                cr=st.nextToken().trim();
                            }
                        }
                        else if (token.equals("CMD_SET_CPU_FREQ")) {
                            st.nextToken();
                            if (st.hasMoreTokens()) {
                                fr=st.nextToken().trim();
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

}
