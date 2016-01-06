package hu.xmister.hermestool;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import eu.chainfire.libsuperuser.Shell;


public class MainFragment extends MyFragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static String[] items, names;
        private static MainActivity a;
        private static Button   maxFreq,
                                freq;
        private static EditText tCores;

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
                if ( items != null ) {
                    a.setP("maxfreq",""+which);
                    maxFreq.setText(names[which]);
                }
            }
        };

        private static DialogInterface.OnClickListener tdi = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ( items != null ) {
                    a.setP("tbFreq",""+which);
                    freq.setText(names[which]);
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
        items=new String[]  {"0",           "806",      "1183",     "1326",     "1469",     "1625",     "1781",     "1950"};
        names=new String[]  {"Unlimited",   "806MHz",   "1183MHz",  "1326MHz",  "1469MHz",  "1625MHz",  "1781MHz",  "1950MHz"};
        maxFreq=(Button)getActivity().findViewById(R.id.maxFreq);
        freq=(Button)getActivity().findViewById(R.id.tFreq);
        tCores=(EditText)getActivity().findViewById(R.id.tCores);
        maxFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceDialog md = new ChoiceDialog("Maximum Frequency", names, di, null, null);
                md.show(getFragmentManager(), "maxfreq");
            }
        });
        freq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceDialog md = new ChoiceDialog("Touchboost Frequency", names, tdi, null, null);
                md.show(getFragmentManager(), "tbfreq");
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
        a.setP("maxfreq","0");
        maxFreq.setText("Unlimited");
        a.setP("tbFreq","2");
        freq.setText("806MHz");
        a.setP("tCores","2");
        tCores.setText("2");
    }

    @Override
    public void loadValues() {
        if (a.getP("maxfreq") != null) {
            maxFreq.setText(names[Integer.valueOf(a.getP("maxfreq"))]);
            freq.setText(names[Integer.valueOf(a.getP("tbFreq"))]);
            tCores.setText(a.getP("tCores"));
        } else super.loadValues();
    }
}
