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
                                freq=null;
        private static EditText tCores;
        private static CheckBox cbTouchBoost;
        private static GridLayout grTouch;
        private static TextView textCore,
                                textFreq;
        private static RadioGroup rg_profile;
        private CompoundButton.OnCheckedChangeListener oCC=new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                a.setP("cbTouchBoost", "" + isChecked);
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
                    a.setP("maxfreq",""+which);
                    maxFreq.setText(Constants.getFrequencyName(a,which));
                }
            }
        };

        private  DialogInterface.OnClickListener tdi = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( Constants.getFrequencyName(a,which) != null ) {
                    a.setP("tbFreq",""+(which+1));
                    freq.setText(Constants.getFrequencyName(a,which+1));
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
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
            a=(MainActivity) activity;
        }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        maxFreq=(Button)getActivity().findViewById(R.id.maxFreq);
        freq=(Button)getActivity().findViewById(R.id.tFreq);
        tCores=(EditText)getActivity().findViewById(R.id.tCores);
        textFreq=(TextView)getActivity().findViewById(R.id.textFreq);
        textCore=(TextView)getActivity().findViewById(R.id.textCore);
        cbTouchBoost=(CheckBox)getActivity().findViewById(R.id.cbTouchBoost);
        grTouch = (GridLayout) getActivity().findViewById(R.id.grTouch);
        rg_profile = (RadioGroup) getActivity().findViewById(R.id.rg_profile);
        if ( rg_profile != null ) {
            rg_profile.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    a.setP("rg_profile", "" + checkedId);
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
        a.setP("tCores", tCores.getText().toString());
    }

    public void loadDefaults() {
        a.setP("maxfreq", ""+Constants.defFRPos);
        maxFreq.setText(Constants.getFrequencyName(getActivity(),Constants.defFRPos));
        try {
            a.setP("tbFreq", "" + Constants.defTBPos);
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
        a.setP("tCores", "2");
        tCores.setText("2");
        a.setP("cbTouchBoost", "true");
        rg_profile.check(R.id.rb_slow);
        a.setP("rg_profile", "" + R.id.rb_slow);
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
    private synchronized void setReadFreqText(final String text) {
        if (getActivity() == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( freq != null ) textFreq.setText(text);
            }
        });
    }
    private synchronized void setReadCoresText(final String text) {
        if (getActivity() == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( tCores != null ) textCore.setText(text);
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
        if ( a.getP("tbFreq") != null) {
            try {
                setFreqText(Constants.getFrequencyName(a, Integer.valueOf(a.getP("tbFreq"))));
            } catch (Exception e) {
                setFreqText(Constants.getFrequencyName(getActivity(),Constants.defTBPos));
            }
        }
        else {
            try {
                a.setP("tbFreq", "" + Constants.defTBPos);
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
        if ( a.getP("tCores")!=null) {
            setCoresText(a.getP("tCores"));
        }
        else {
            setCoresText("2");
            a.setP("tCores", "2");
        }
        if (a.getP("maxfreq") != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    try {
                        maxFreq.setText(Constants.getFrequencyName(a, Integer.valueOf(a.getP("maxfreq"))));
                        cbTouchBoost.setChecked(Boolean.valueOf(a.getP("cbTouchBoost")));
                        oCC.onCheckedChanged(cbTouchBoost, Boolean.valueOf(a.getP("cbTouchBoost")));
                    } catch (Exception e) {
                        a.setP("maxfreq", "" + Constants.defFRPos);
                        maxFreq.setText(Constants.getFrequencyName(getActivity(), Constants.defFRPos));
                    }
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    a.setP("maxfreq", "" + Constants.defFRPos);
                    try {
                        maxFreq.setText(Constants.getFrequencyName(getActivity(), Constants.defFRPos));
                        a.setP("cbTouchBoost", "false");
                        cbTouchBoost.setChecked(false);
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
        if (a.getP("rg_profile") != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;
                    rg_profile.check(Integer.valueOf(a.getP("rg_profile")));
                }
            });
        }
    }
}
