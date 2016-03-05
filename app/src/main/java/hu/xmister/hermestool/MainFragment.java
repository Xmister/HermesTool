package hu.xmister.hermestool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioGroup;
import android.widget.TextView;


public class MainFragment extends MyFragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static MainFragment self=null;
        private static Button   maxFreq,
                                gpuFreq,
                                freq=null;
        private static EditText tCores,
                                tLimitCores;
        private static CheckBox cbTouchBoost,
                                cbLimitGpuFreq,
                                cbLimitCores;
        private static GridLayout grTouch;
        private static TextView textCore,
                                textFreq;
        private static RadioGroup rg_profile;
        private CompoundButton.OnCheckedChangeListener oCC=new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setP("cbTouchBoost", "" + isChecked);
                if (isChecked)
                    grTouch.setVisibility(View.VISIBLE);
                else
                    grTouch.setVisibility(View.INVISIBLE);
            }
        };

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MainFragment newInstance(int sectionNumber) {
            if ( self == null ) {
                self = new MainFragment();
                Bundle args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, sectionNumber);
                self.setArguments(args);
            }
            return self;
        }

        public MainFragment() {
        }

        private  DialogInterface.OnClickListener di = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( Constants.getFrequencyName(a,which) != null ) {
                    setP("maxfreq",""+which);
                    maxFreq.setText(Constants.getFrequencyName(a,which));
                }
            }
        };

        private  DialogInterface.OnClickListener tdi = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( Constants.getFrequencyName(a,which) != null ) {
                    setP("tbFreq", "" + (which + 1));
                    freq.setText(Constants.getFrequencyName(a,which+1));
                }
            }
        };

        private  DialogInterface.OnClickListener gdi = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( Constants.getGpuFrequencyName(a, which) != null ) {
                    setP("gpufreq", "" + which);
                    gpuFreq.setText(Constants.getGpuFrequencyName(a, which));
                }
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        maxFreq=(Button)getActivity().findViewById(R.id.maxFreq);
        gpuFreq=(Button)getActivity().findViewById(R.id.bLimitGpuFreq);
        freq=(Button)getActivity().findViewById(R.id.tFreq);
        tCores=(EditText)getActivity().findViewById(R.id.tCores);
        tLimitCores=(EditText)getActivity().findViewById(R.id.tMaxCores);
        textFreq=(TextView)getActivity().findViewById(R.id.textFreq);
        textCore=(TextView)getActivity().findViewById(R.id.textCore);
        cbTouchBoost=(CheckBox)getActivity().findViewById(R.id.cbTouchBoost);
        cbLimitCores=(CheckBox)getActivity().findViewById(R.id.cb_limitCore);
        cbLimitGpuFreq=(CheckBox)getActivity().findViewById(R.id.cbLimitGpuFreq);
        grTouch = (GridLayout) getActivity().findViewById(R.id.grTouch);
        rg_profile = (RadioGroup) getActivity().findViewById(R.id.rg_profile);
        if ( rg_profile != null ) {
            rg_profile.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    setP("rg_profile", "" + checkedId);
                }
            });
            maxFreq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Constants.getFrequencyNames(a) != null) {
                        ChoiceDialog md = new ChoiceDialog("Maximum Frequency", Constants.getFrequencyNames(a), di, null, null);
                        md.show(getFragmentManager(), "maxfreq");
                    } else {
                        //TODO
                    }
                }
            });
            gpuFreq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Constants.getGpuFrequencyNames(a) != null) {
                        ChoiceDialog md = new ChoiceDialog("Maximum Frequency", Constants.getGpuFrequencyNames(a), gdi, null, null);
                        md.show(getFragmentManager(), "gpufreq");
                    } else {
                        //TODO
                    }
                }
            });
            freq.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Constants.getFrequencyNames(a) != null) {
                        String tmpNames[] = new String[Constants.getFrequencyNames(a).length - 1];
                        for (int i = 1; i < Constants.getFrequencyNames(a).length; i++) {
                            tmpNames[i - 1] = Constants.getFrequencyNames(a)[i];
                        }
                        ChoiceDialog md = new ChoiceDialog("Touchboost Frequency", tmpNames, tdi, null, null);
                        md.show(getFragmentManager(), "tbfreq");
                    } else {
                        //TODO
                    }
                }
            });
            cbTouchBoost.setOnCheckedChangeListener(oCC);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void beforeSave() {
        super.beforeSave();
        setP("tCores", tCores.getText().toString());
        setP("tLimitCores", tLimitCores.getText().toString());
        setP("cb_limitCores", "" + cbLimitCores.isChecked());
        setP("cbLimitGpuFreq", "" + cbLimitGpuFreq.isChecked());
    }

    public void loadDefaults() {
        setP("maxfreq", "" + Constants.defFRPos);
        maxFreq.setText(Constants.getFrequencyName(getActivity(),Constants.defFRPos));
        try {
            setP("tbFreq", "" + Constants.defTBPos);
        } catch ( Resources.NotFoundException e ) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(a);
                    builder.setTitle(getString(R.string.freq_error))
                            .setMessage("This is probably not a Redmi Note 2 device!")
                            .show();
                    a.finish();
                }
            });
        }
        freq.setText(Constants.getFrequencyName(getActivity(),Constants.defTBPos));
        setP("tCores", "2");
        tCores.setText("2");
        setP("tLimitCores", "5");
        tLimitCores.setText("5");
        setP("cbTouchBoost", "true");
        setP("cb_limitCores", "false");
        cbLimitCores.setChecked(false);
        rg_profile.check(R.id.rb_slow);
        setP("rg_profile", "" + R.id.rb_slow);
        cbTouchBoost.setChecked(true);
        oCC.onCheckedChanged(cbTouchBoost,true);
    }

    private synchronized void setFreqText(final String text) {
        if (getActivity() == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (freq != null) freq.setText(text);
            }
        });
    }
    private synchronized void setCoresText(final String text) {
        if (getActivity() == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tCores != null) tCores.setText(text);
            }
        });
    }

    private synchronized void setLimitCoresText(final String text) {
        if (getActivity() == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tLimitCores != null) tLimitCores.setText(text);
            }
        });
    }
    private synchronized void setReadFreqText(final String text) {
        if (getActivity() == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( textFreq != null ) textFreq.setText(text);
            }
        });
    }
    private synchronized void setReadCoresText(final String text) {
        if (getActivity() == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( textCore!= null ) textCore.setText(text);
            }
        });
    }

    @Override
    public void loadValues() {
        SUCommand.getTouchBoost(new SUCommand.tbCallback() {
            @Override
            public void onGotTB(String freq, String cores) {
                if ( freq!= null && cores != null ) {
                    if (Constants.getFrequencyItems(a) != null) {
                        try {
                            int i = Constants.getItemsPos(a,freq);
                            setReadFreqText(Constants.getFrequencyName(a,i));
                        } catch (Resources.NotFoundException e) {
                            //TODO
                        }
                    }
                    else {
                        //TODO
                    }
                    setReadCoresText(cores);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                            builder.setTitle(getString(R.string.tb_error))
                                    .setMessage(getString(R.string.tb_error_message))
                                    .show();
                        }
                    });
                }
            }
        });
        if ( getP("tbFreq") != null) {
            try {
                setFreqText(Constants.getFrequencyName(a, Integer.valueOf(getP("tbFreq"))));
            } catch (Exception e) {
                setFreqText(Constants.getFrequencyName(getActivity(),Constants.defTBPos));
            }
        }
        else {
            try {
                setP("tbFreq", "" + Constants.defTBPos);
            } catch (Exception e ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(a);
                        builder.setTitle(getString(R.string.freq_error))
                                .setMessage("This is probably not a Redmi Note 2 device!")
                                .show();
                        a.finish();
                    }
                });
            }
        }
        if ( getP("tCores")!=null) {
            setCoresText(getP("tCores"));
        }
        else {
            setCoresText("2");
            setP("tCores", "2");
        }
        if ( getP("tLimitCores") != null) {
            setLimitCoresText(getP("tLimitCores"));
        }
        else {
            setLimitCoresText("5");
            setP("tLimitCores", "5");
        }
        if (getP("maxfreq") != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    try {
                        maxFreq.setText(Constants.getFrequencyName(a, Integer.valueOf(getP("maxfreq"))));
                        cbTouchBoost.setChecked(Boolean.valueOf(getP("cbTouchBoost")));
                        cbLimitCores.setChecked(Boolean.valueOf(getP("cb_limitCores")));
                        oCC.onCheckedChanged(cbTouchBoost, Boolean.valueOf(getP("cbTouchBoost")));
                    } catch (Exception e) {
                        setP("maxfreq", "" + Constants.defFRPos);
                        maxFreq.setText(Constants.getFrequencyName(getActivity(), Constants.defFRPos));
                    }
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    setP("maxfreq", "" + Constants.defFRPos);
                    try {
                        maxFreq.setText(Constants.getFrequencyName(getActivity(), Constants.defFRPos));
                        setP("cbTouchBoost", "false");
                        cbTouchBoost.setChecked(false);
                        setP("cb_limitCores", "false");
                        cbLimitCores.setChecked(false);
                        oCC.onCheckedChanged(cbTouchBoost, false);
                        grTouch.setVisibility(View.INVISIBLE);
                    } catch (IndexOutOfBoundsException e) {
                        //TODO: Better solution
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                            builder.setTitle(getString(R.string.freq_error))
                                    .setMessage("Please restart the application!")
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            a.finish();
                                        }
                                    })
                                    .show();
                        } catch (Exception ee) {
                            a.finish();
                        }
                    }
                }
            });
        }
        if (getP("gpufreq") != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    try {
                        gpuFreq.setText(Constants.getGpuFrequencyName(a, Integer.valueOf(getP("gpufreq"))));
                        cbLimitGpuFreq.setChecked(Boolean.valueOf(getP("cbLimitGpuFreq")));
                    } catch (Exception e) {
                        setP("gpufreq", "" + Constants.defGPUPos);
                        gpuFreq.setText(Constants.getGpuFrequencyName(getActivity(), Constants.defGPUPos));
                    }
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    setP("gpufreq", "" + Constants.defGPUPos);
                    try {
                        gpuFreq.setText(Constants.getGpuFrequencyName(getActivity(), Constants.defGPUPos));
                    } catch (IndexOutOfBoundsException e) {
                        //TODO: Better solution
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                            builder.setTitle(getString(R.string.freq_error))
                                    .setMessage("Please restart the application!")
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            a.finish();
                                        }
                                    })
                                    .show();
                        } catch (Exception ee) {
                            a.finish();
                        }
                    }
                }
            });
        }
        if (getP("rg_profile") != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    rg_profile.check(Integer.valueOf(getP("rg_profile")));
                }
            });
        }
    }
}
