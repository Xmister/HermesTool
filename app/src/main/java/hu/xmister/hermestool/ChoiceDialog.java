package hu.xmister.hermestool;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;

/**
 * A helper class to easily create dialogs.
 */
@SuppressLint("ValidFragment")
public class ChoiceDialog extends DialogFragment {
    private DialogInterface.OnClickListener olistener;
    private DialogInterface.OnCancelListener clistener;
    private DialogInterface.OnDismissListener dlistener;
    private String label;
    private String[] options;

    public ChoiceDialog(String label, String[] options, DialogInterface.OnClickListener ol, DialogInterface.OnCancelListener cl, DialogInterface.OnDismissListener cd) {
        olistener = ol;
        clistener = cl;
        dlistener = cd;
        this.label = label;
        this.options = options;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(label)
                .setItems(options, olistener);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dlistener != null)
            dlistener.onDismiss(dialog);
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (clistener != null)
            clistener.onCancel(dialog);
        super.onCancel(dialog);
    }
}
