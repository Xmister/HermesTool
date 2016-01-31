package hu.xmister.hermestool;


import android.content.Context;
import android.content.res.Resources;

import java.util.List;
import java.util.StringTokenizer;

import eu.chainfire.libsuperuser.Shell;

public class Constants {
    private static String[] fi,frequencyItems = null; /*new String[]  {"0",           "806",      "1183",     "1326",     "1469",     "1625",     "1781",     "1950"};*/
    private static String[] fn,frequencyNames = null; /*new String[]  {"Unlimited",   "806MHz",   "1183MHz",  "1326MHz",  "1469MHz",  "1625MHz",  "1781MHz",  "1950MHz"};*/
    private static boolean filling=false;
    public static int defFRPos=2;
    public static int defTBPos=5;

    private synchronized static void fillArrays(final Context c) {
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
                defFRPos=(Constants.getFrequencyItem(c,1).equals("1950000") ? 2 : 3);
                defTBPos=Constants.getFrequencyItems(c).length-2;
            }
        },30000);
        filling=false;
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
}
