package hu.xmister.hermestool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;

import java.util.List;


public class MainFragment extends MyFragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static MainFragment self=null;
        private static MainActivity a;
        private static Button   maxFreq,
                                freq=null;
        private static EditText tCores;
        private static CheckBox cbTouchBoost;
        private static GridLayout grTouch;

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

        private static DialogInterface.OnClickListener di = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( Constants.getFrequencyName(which) != null ) {
                    a.setP("maxfreq",""+which);
                    maxFreq.setText(Constants.getFrequencyName(which));
                }
            }
        };

        private static DialogInterface.OnClickListener tdi = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( Constants.getFrequencyName(which) != null ) {
                    a.setP("tbFreq",""+(which+1));
                    freq.setText(Constants.getFrequencyName(which+1));
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
        cbTouchBoost=(CheckBox)getActivity().findViewById(R.id.cbTouchBoost);
        grTouch = (GridLayout) getActivity().findViewById(R.id.grTouch);
        maxFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.getFrequencyNames() != null) {
                    ChoiceDialog md = new ChoiceDialog("Maximum Frequency", Constants.getFrequencyNames(), di, null, null);
                    md.show(getFragmentManager(), "maxfreq");
                }
                else {
                    //TODO
                }
            }
        });
        freq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constants.getFrequencyNames() != null) {
                    String tmpNames[] = new String[Constants.getFrequencyNames().length - 1];
                    for (int i = 1; i < Constants.getFrequencyNames().length; i++) {
                        tmpNames[i - 1] = Constants.getFrequencyNames()[i];
                    }
                    ChoiceDialog md = new ChoiceDialog("Touchboost Frequency", tmpNames, tdi, null, null);
                    md.show(getFragmentManager(), "tbfreq");
                }
                else {
                    //TODO
                }
            }
        });
        cbTouchBoost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                a.setP("cbTouchBoost", "" + isChecked);
                if (isChecked)
                    grTouch.setVisibility(View.VISIBLE);
                else
                    grTouch.setVisibility(View.INVISIBLE);
            }
        });
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void beforeSave() {
        super.beforeSave();
        a.setP("tCores", tCores.getText().toString());
    }

    public void loadDefaults() {
        a.setP("maxfreq", "0");
        maxFreq.setText("Unlimited");
        if ( Constants.getNamesPos("806MHz") > -1 )
            a.setP("tbFreq", ""+Constants.getNamesPos("806MHz"));
        freq.setText("806MHz");
        a.setP("tCores", "2");
        tCores.setText("2");
        a.setP("cbTouchBoost", "true");
        cbTouchBoost.setChecked(true);
    }

    private synchronized void setFreqText(final String text) {
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( freq != null ) freq.setText(text);
            }
        });
    }
    private synchronized void setCoresText(final String text) {
        a.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ( tCores != null ) tCores.setText(text);
            }
        });
    }

    @Override
    public void loadValues() {
        SUCommand.getTouchBoost(new SUCommand.tbCallback() {
            @Override
            public void onGotTB(String freq, String cores) {
                if ( freq!= null && cores != null ) {
                    if (Constants.getFrequencyItems() != null) {
                        int i = Constants.getItemsPos(freq);
                        if (i > -1 && i < Constants.getFrequencyItems().length) {
                            setFreqText(Constants.getFrequencyName(i));
                        } else i = 0;
                        a.setP("tbFreq", "" + i);
                        setCoresText(cores);
                    }
                    else {
                        //TODO
                    }
                } else {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(a);
                            builder.setTitle(getString(R.string.tb_error))
                                    .setMessage(getString(R.string.tb_error_message))
                                    .show();
                        }
                    });
                    if ( a.getP("tbFreq") != null) {
                        setFreqText(Constants.getFrequencyName(Integer.valueOf(a.getP("tbFreq"))));
                    }
                    else {
                        setFreqText("806MHz");
                        if ( Constants.getNamesPos("806MHz") > -1)
                            a.setP("tbFreq", ""+Constants.getNamesPos("806MHz"));
                    }
                    if ( a.getP("tCores")!=null) {
                        setCoresText(a.getP("tCores"));
                    }
                    else {
                        setCoresText("2");
                        a.setP("tCores","2");
                    }
                }
            }
        });
        if (a.getP("maxfreq") != null) {
            maxFreq.setText(Constants.getFrequencyName(Integer.valueOf(a.getP("maxfreq"))));
            cbTouchBoost.setChecked(Boolean.valueOf(a.getP("cbTouchBoost")));
        } else {
            a.setP("maxfreq", "0");
            maxFreq.setText("Unlimited");
            a.setP("cbTouchBoost", "false");
            cbTouchBoost.setChecked(false);
            grTouch.setVisibility(View.INVISIBLE);
        }
    }
}
