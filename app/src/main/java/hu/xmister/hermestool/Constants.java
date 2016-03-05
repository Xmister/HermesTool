package hu.xmister.hermestool;


import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import eu.chainfire.libsuperuser.Shell;

public class Constants {
    private static String[] fi,frequencyItems = null; /*new String[]  {"0",           "806",      "1183",     "1326",     "1469",     "1625",     "1781",     "1950"};*/
    private static String[] fn,frequencyNames = null; /*new String[]  {"Unlimited",   "806MHz",   "1183MHz",  "1326MHz",  "1469MHz",  "1625MHz",  "1781MHz",  "1950MHz"};*/
    private static String[] gFN,gpuFrequencyNames = null;
    private static String[] gFI,gpuFrequencyItems = null;
    private static boolean filling=false;
    public static int defFRPos=2;
    public static int defTBPos=5;
    public static int defGPUPos=0;
    public static final int TWRP_VER=46;
    public static final long TWRP_SIZE=13910016L;
    public static final int MIREC_VER=47;
    public static final long MIREC_SIZE=10330112L;

    public interface InitComplete {
        public void onInitComplete();
    }
    private synchronized static void fillArrays(final Context c) { fillArrays(c,null);}
    private synchronized static void fillArrays(final Context c,final InitComplete iC) {
        if (filling) return;
        filling=true;
        SUCommand.executeSu("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies", new Shell.OnCommandResultListener() {
            @Override
            public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                for (String line : output) {
                    if (line.length() > 0) {
                        StringTokenizer st = new StringTokenizer(line, " ");
                        fi=new String[st.countTokens() + 1];
                        fn=new String[st.countTokens() + 1];
                        int i = 0;
                        fi[i] = "0";
                        fn[i++] = c.getString(R.string.unlimited);
                        while (st.hasMoreTokens()) {
                            String cur = st.nextToken().trim();
                            fi[i] = cur;
                            fn[i++] = cur.substring(0, cur.length() - 3) + "MHz";
                        }
                    }
                }
                frequencyItems = fi;
                frequencyNames = fn;
                defFRPos=2;
                defTBPos=Constants.getFrequencyItems(c).length-2;
                SUCommand.executeSu("cat /proc/gpufreq/gpufreq_power_dump", new Shell.OnCommandResultListener() {
                    @Override
                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                        LinkedList<String> freqs = new LinkedList<String>();
                        for (String line : output) {
                            if (line.length() > 0 && line.contains("_khz")) {
                                StringTokenizer st = new StringTokenizer(line, " = ");
                                st.nextToken();
                                freqs.add(st.nextToken());
                            }
                        }
                        gFI=new String[freqs.size()];
                        gFN=new String[freqs.size()];
                        int i = 0;
                        for (String fr : freqs) {
                            gFI[i] = fr;
                            gFN[i++] = fr.substring(0, fr.length() - 3) + "MHz";
                        }
                        gpuFrequencyItems = gFI;
                        gpuFrequencyNames = gFN;
                        filling=false;
                        if (iC != null) {
                            iC.onInitComplete();
                        }
                    }
                });
            }
        });
    }

    public static void init(final Context c,final InitComplete iC) {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        final Handler h=new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                fillArrays(c, new InitComplete() {
                    @Override
                    public void onInitComplete() {
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                Looper.myLooper().quitSafely();
                                iC.onInitComplete();
                            }
                        });
                    }
                });
            }
        }).start();
        Looper.loop();
    }

    public static String[] getFrequencyNames(final Context c) {
        if (frequencyNames == null) fillArrays(c);
        return frequencyNames;
    }

    public static String getFrequencyName(final Context c,int index) throws IndexOutOfBoundsException {
        if ( getFrequencyNames(c) != null )
            if (index >= 0 && index < getFrequencyNames(c).length)
                return getFrequencyNames(c)[index];
            else throw new IndexOutOfBoundsException();
        else throw new IndexOutOfBoundsException();
    }

    public static String getFrequencyItem(final Context c,int index) throws IndexOutOfBoundsException {
        if ( getFrequencyItems(c) != null )
            if (index >= 0 && index < getFrequencyItems(c).length)
                return getFrequencyItems(c)[index];
            else throw new IndexOutOfBoundsException();
        else throw new IndexOutOfBoundsException();
    }

    public static String[] getFrequencyItems(final Context c) {
        if (frequencyItems == null) fillArrays(c);
        return frequencyItems;
    }

    public static int getNamesPos(final Context c,String f) throws Resources.NotFoundException {
        if ( getFrequencyNames(c) == null ) throw new Resources.NotFoundException(null);
        for (int i=0; i<getFrequencyNames(c).length; i++) {
            if (getFrequencyNames(c)[i].toLowerCase().equals(f.toLowerCase())) return i;
        }
        throw new Resources.NotFoundException(null);
    }

    public static int getItemsPos(final Context c,String f) throws Resources.NotFoundException {
        if ( getFrequencyItems(c) == null ) throw new Resources.NotFoundException(null);
        for (int i=0; i<getFrequencyItems(c).length; i++) {
            if (getFrequencyItems(c)[i].toLowerCase().equals(f.toLowerCase())) return i;
        }
        throw new Resources.NotFoundException(null);
    }

    public static String[] getGpuFrequencyNames(final Context c) {
        if (gpuFrequencyNames == null) fillArrays(c);
        return gpuFrequencyNames;
    }

    public static String getGpuFrequencyName(final Context c,int index) throws IndexOutOfBoundsException {
        if ( getGpuFrequencyNames(c) != null )
            if (index >= 0 && index < getGpuFrequencyNames(c).length)
                return getGpuFrequencyNames(c)[index];
            else throw new IndexOutOfBoundsException();
        else throw new IndexOutOfBoundsException();
    }

    public static String getGpuFrequencyItem(final Context c,int index) throws IndexOutOfBoundsException {
        if ( getGpuFrequencyItems(c) != null )
            if (index >= 0 && index < getGpuFrequencyItems(c).length)
                return getGpuFrequencyItems(c)[index];
            else throw new IndexOutOfBoundsException();
        else throw new IndexOutOfBoundsException();
    }

    public static String[] getGpuFrequencyItems(final Context c) {
        if (gpuFrequencyItems == null) fillArrays(c);
        return gpuFrequencyItems;
    }

    public static int getGpuNamesPos(final Context c,String f) throws Resources.NotFoundException {
        if ( getGpuFrequencyNames(c) == null ) throw new Resources.NotFoundException(null);
        for (int i=0; i<getGpuFrequencyNames(c).length; i++) {
            if (getGpuFrequencyNames(c)[i].toLowerCase().equals(f.toLowerCase())) return i;
        }
        throw new Resources.NotFoundException(null);
    }

    public static int getGpuItemsPos(final Context c,String f) throws Resources.NotFoundException {
        if ( getGpuFrequencyItems(c) == null ) throw new Resources.NotFoundException(null);
        for (int i=0; i<getGpuFrequencyItems(c).length; i++) {
            if (getGpuFrequencyItems(c)[i].toLowerCase().equals(f.toLowerCase())) return i;
        }
        throw new Resources.NotFoundException(null);
    }
}
