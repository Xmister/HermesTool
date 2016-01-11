package hu.xmister.hermestool;


import android.util.Property;

import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import eu.chainfire.libsuperuser.Shell;

public class Constants {
    private static String[] fi,frequencyItems = null; /*new String[]  {"0",           "806",      "1183",     "1326",     "1469",     "1625",     "1781",     "1950"};*/
    private static String[] fn,frequencyNames = null; /*new String[]  {"Unlimited",   "806MHz",   "1183MHz",  "1326MHz",  "1469MHz",  "1625MHz",  "1781MHz",  "1950MHz"};*/
    private static boolean filling=false;

    private synchronized static void fillArrays() {
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
                        fn[i++] = "Unlimited";
                        while (st.hasMoreTokens()) {
                            String cur = st.nextToken().trim();
                            fi[i] = cur;
                            fn[i++] = cur.substring(0, cur.length() - 3) + "MHz";
                        }
                    }
                }
                frequencyItems = fi;
                frequencyNames = fn;
            }
        },10000);
        filling=false;
    }

    public static String[] getFrequencyNames() {
        if (frequencyNames == null) fillArrays();
        return frequencyNames;
    }

    public static String getFrequencyName(int index) {
        if ( getFrequencyNames() != null )
            return getFrequencyNames()[index];
        else return null;
    }

    public static String getFrequencyItem(int index) {
        if ( getFrequencyItems() != null )
            return getFrequencyItems()[index];
        else return null;
    }

    public static String[] getFrequencyItems() {
        if (frequencyItems == null) fillArrays();
        return frequencyItems;
    }

    public static int getNamesPos(String f) {
        if ( getFrequencyNames() == null ) return -1;
        for (int i=0; i<getFrequencyNames().length; i++) {
            if (getFrequencyNames()[i].toLowerCase().equals(f.toLowerCase())) return i;
        }
        return -1;
    }

    public static int getItemsPos(String f) {
        if ( getFrequencyItems() == null ) return -1;
        for (int i=0; i<getFrequencyItems().length; i++) {
            if (getFrequencyItems()[i].toLowerCase().equals(f.toLowerCase())) return i;
        }
        return -1;
    }
}
