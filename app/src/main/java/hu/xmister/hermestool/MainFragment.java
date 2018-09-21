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
        private MainFragment self=null;
        private boolean initDone=false;
        private Button   maxFreq,
                                gpuFreq,
                                freq=null;
        private EditText tCores,
                                tLimitCores;
        private CheckBox cbTouchBoost,
                                cbLimitGpuFreq,
                                cbLimitCores;
        private GridLayout grTouch;
        private TextView textCore,
                                textFreq;
        private RadioGroup rg_profile;
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
            MainFragment self=null;
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

    private void initValues() {
        if (!initDone) {
            maxFreq = (Button) getActivity().findViewById(R.id.maxFreq);
            gpuFreq = (Button) getActivity().findViewById(R.id.bLimitGpuFreq);
            freq = (Button) getActivity().findViewById(R.id.tFreq);
            tCores = (EditText) getActivity().findViewById(R.id.tCores);
            tLimitCores = (EditText) getActivity().findViewById(R.id.tMaxCores);
            textFreq = (TextView) getActivity().findViewById(R.id.textFreq);
            textCore = (TextView) getActivity().findViewById(R.id.textCore);
            cbTouchBoost = (CheckBox) getActivity().findViewById(R.id.cbTouchBoost);
            cbLimitCores = (CheckBox) getActivity().findViewById(R.id.cb_limitCore);
            cbLimitGpuFreq = (CheckBox) getActivity().findViewById(R.id.cbLimitGpuFreq);
            grTouch = (GridLayout) getActivity().findViewById(R.id.grTouch);
            rg_profile = (RadioGroup) getActivity().findViewById(R.id.rg_profile);
            initDone = true;
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        initValues();
        if ( rg_profile != null ) {
            rg_profile.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    setP("rg_profile", "" + checkedId);
                }
            });
            if (maxFreq != null) {
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
            }
            if (gpuFreq != null) {
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
            }
            if (freq != null) {
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
            }
            if (cbTouchBoost != null) {
                cbTouchBoost.setOnCheckedChangeListener(oCC);
            }
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
        initValues();
        try {
            setP("maxfreq", "" + Constants.defFRPos);
            setP("tbFreq", "" + Constants.defTBPos);
            setP("tCores", "2");
            setP("tLimitCores", "5");
            setP("cbTouchBoost", "true");
            setP("cb_limitCores", "false");
            setP("rg_profile", "" + R.id.rb_slow);
            maxFreq.setText(Constants.getFrequencyName(getActivity(), Constants.defFRPos));
            freq.setText(Constants.getFrequencyName(getActivity(),Constants.defTBPos));
            tCores.setText("2");
            tLimitCores.setText("5");
            cbLimitCores.setChecked(false);
            rg_profile.check(R.id.rb_slow);
            cbTouchBoost.setChecked(true);
            oCC.onCheckedChanged(cbTouchBoost,true);
        } catch ( Resources.NotFoundException e ) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(a);
                    builder.setTitle(getString(R.string.freq_error))
                            .setMessage("This is probably not a Redmi Note 2 device!")
                            .show();
                    finish();
                }
            });
        } catch (Exception e) {}
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
    private synchronized void setMaxFreqText(final String text) {
        if (getActivity() == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (maxFreq != null) maxFreq.setText(text);
            }
        });
    }

    private synchronized void setGpuFreqText(final String text) {
        if (getActivity() == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (gpuFreq != null) gpuFreq.setText(text);
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
        initValues();
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
                try {
                    setFreqText(Constants.getFrequencyName(getActivity(),Constants.defTBPos));
                    setP("tbFreq", "" + Constants.defTBPos);
                } catch (Exception ee) {}
            }
        }
        else {
            try {
                setP("tbFreq", "" + Constants.defTBPos);
                setFreqText(Constants.getFrequencyName(getActivity(), Constants.defTBPos));
            } catch (Exception e ) {}
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
                    if (getActivity() == null) return;
                    try {
                        setMaxFreqText(Constants.getFrequencyName(a, Integer.valueOf(getP("maxfreq"))));
                        cbTouchBoost.setChecked(Boolean.valueOf(getP("cbTouchBoost")));
                        cbLimitCores.setChecked(Boolean.valueOf(getP("cb_limitCores")));
                        oCC.onCheckedChanged(cbTouchBoost, Boolean.valueOf(getP("cbTouchBoost")));
                    } catch (Exception e) {
                        setP("maxfreq", "" + Constants.defFRPos);
                        try {
                            setMaxFreqText(Constants.getFrequencyName(getActivity(), Constants.defFRPos));
                        } catch (Exception ee) {
                        }
                    }
        } else {
                    if (getActivity() == null) return;
                    setP("maxfreq", "" + Constants.defFRPos);
                    try {
                        setMaxFreqText(Constants.getFrequencyName(getActivity(), Constants.defFRPos));
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
                                            finish();
                                        }
                                    })
                                    .show();
                        } catch (Exception ee) {
                            try {
                                finish();
                            } catch (Exception eee) {
                            }
                        }
                    } catch (Exception e) {}
        }
        if (getP("gpufreq") != null) {
                    if (getActivity() == null) return;
                    try {
                        setGpuFreqText(Constants.getGpuFrequencyName(a, Integer.valueOf(getP("gpufreq"))));
                        cbLimitGpuFreq.setChecked(Boolean.valueOf(getP("cbLimitGpuFreq")));
                    } catch (Exception e) {
                        setP("gpufreq", "" + Constants.defGPUPos);
                        try {
                            setGpuFreqText(Constants.getGpuFrequencyName(getActivity(), Constants.defGPUPos));
                        } catch (Exception ee) {
                        }
                    }
        } else {
                    if (getActivity() == null) return;
                    setP("gpufreq", "" + Constants.defGPUPos);
                    try {
                        setGpuFreqText(Constants.getGpuFrequencyName(getActivity(), Constants.defGPUPos));
                    } catch (IndexOutOfBoundsException e) {
                        //TODO: Better solution
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                            builder.setTitle(getString(R.string.freq_error))
                                    .setMessage("Please restart the application!")
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            finish();
                                        }
                                    })
                                    .show();
                        } catch (Exception ee) {
                            try {
                                finish();
                            } catch (Exception eee) {
                            }
                        }
                    }
        }
        if (getP("rg_profile") != null) {
                    if (getActivity() == null) return;
                    try {
                        rg_profile.check(Integer.valueOf(getP("rg_profile")));
                    } catch (Exception e) {}
        }
    }
}
