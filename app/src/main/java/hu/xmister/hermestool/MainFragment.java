package hu.xmister.hermestool;

import android.app.Activity;
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


public class MainFragment extends MyFragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static MainActivity a;
        private static Button   maxFreq,
                                freq;
        private static EditText tCores;
        private static CheckBox cbTouchBoost;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MainFragment newInstance(int sectionNumber) {
            MainFragment fragment = new MainFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public MainFragment() {
        }

        private static DialogInterface.OnClickListener di = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( Constants.frequencyItems != null ) {
                    a.setP("maxfreq",""+which);
                    maxFreq.setText(Constants.frequencyNames[which]);
                }
            }
        };

        private static DialogInterface.OnClickListener tdi = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( Constants.frequencyItems != null ) {
                    a.setP("tbFreq",""+which);
                    freq.setText(Constants.frequencyNames[which]);
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
        maxFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceDialog md = new ChoiceDialog("Maximum Frequency", Constants.frequencyNames, di, null, null);
                md.show(getFragmentManager(), "maxfreq");
            }
        });
        freq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceDialog md = new ChoiceDialog("Touchboost Frequency", Constants.frequencyNames, tdi, null, null);
                md.show(getFragmentManager(), "tbfreq");
            }
        });
        cbTouchBoost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                a.setP("cbTouchBoost",""+isChecked);
                GridLayout grTouch= (GridLayout)getActivity().findViewById(R.id.grTouch);
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
        a.setP("tCores",tCores.getText().toString());
    }

    public void loadDefaults() {
        a.setP("maxfreq", "0");
        maxFreq.setText("Unlimited");
        a.setP("tbFreq", "2");
        freq.setText("806MHz");
        a.setP("tCores", "2");
        tCores.setText("2");
        a.setP("cbTouchBoost","true");
        cbTouchBoost.setChecked(true);
    }

    @Override
    public void loadValues() {
        if (a.getP("maxfreq") != null) {
            maxFreq.setText(Constants.frequencyNames[Integer.valueOf(a.getP("maxfreq"))]);
            freq.setText(Constants.frequencyNames[Integer.valueOf(a.getP("tbFreq"))]);
            tCores.setText(a.getP("tCores"));
            cbTouchBoost.setChecked(Boolean.valueOf(a.getP("cbTouchBoost")));
        } else super.loadValues();
    }
}
