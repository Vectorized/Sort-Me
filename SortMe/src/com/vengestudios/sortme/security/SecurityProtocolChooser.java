package com.vengestudios.sortme.security;

import java.lang.reflect.Method;
import com.vengestudios.sortme.R;
import com.vengestudios.sortme.helpers.ui.ScreenDimensions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;
import android.widget.RelativeLayout;

/**
 * This class is used to generate the pop-up dialog to choose between the protocols.
 *
 * It contains the GUI formatting code needed to show the pop-up dialog.
 *
 */
public class SecurityProtocolChooser implements OnClickListener {

	// UI constants
    private static final int DIALOG_BUTTON_WIDTH  = 68;
    private static final int DIALOG_BUTTON_HEIGHT = 86;

    // Application context used for creation of UI elements
    private Context        context;

    // UI Elements
    private Button         dialogButton;

    private Dialog         dialog;
    private NumberPicker   protocolPicker;

    // Used for setting up the NumberPicker to choose between the protocols
    private SparseArray<String>               securityProtocolNames;
    private SparseArray<SecurityProtocolType> securityProtocolTypes;
    private SparseIntArray                    securityProtocolValues;

    // A reference to the SecurityMessageLayer
    private SecurityMessageLayer securityMessageLayer;

    /**
     * Constructor
     *
     * Sets up the UI Elements required to choose between the different protocols
     *
     * @param context                     The context of the application (usually MainActivity)
     * @param securityMessageLayer        The SecurityMessageLayer
     * @param securityProtocolButtonAdder The object that has implemented the
     *                                    SecurityProtocolButtonAdder interface
     */
    public SecurityProtocolChooser(Context context,
            SecurityMessageLayer securityMessageLayer,
            SecurityProtocolButtonAdder securityProtocolButtonAdder) {

        this.context = context;
        this.securityMessageLayer = securityMessageLayer;

        float screenDensity = ScreenDimensions.getDensity(context);
        int dialogButtonFinalWidth  = (int)(screenDensity*DIALOG_BUTTON_WIDTH);
        int dialogButtonFinalHeight = (int)(screenDensity*DIALOG_BUTTON_HEIGHT);
        dialogButton = new Button(context);
        dialogButton.setBackgroundResource(R.drawable.security_protocol_button);
        dialogButton.setLayoutParams(
                new RelativeLayout.LayoutParams(
                        dialogButtonFinalWidth,
                        dialogButtonFinalHeight));
        dialogButton.setOnClickListener(this);
        securityProtocolButtonAdder.addProtocolChooserButton(dialogButton);

        protocolPicker = new NumberPicker(context);

        securityProtocolNames  = new SparseArray<String>(8);
        securityProtocolTypes  = new SparseArray<SecurityProtocolType>(8);
        securityProtocolValues = new SparseIntArray(8);

        int startValue = 100;
        int value = startValue;
        for (SecurityProtocolType securityProtocolType:SecurityProtocolType.values()) {
            securityProtocolNames.put(value, securityProtocolType.name());
            securityProtocolTypes.put(value, securityProtocolType);
            securityProtocolValues.put(securityProtocolType.ordinal(), value);
            ++value;
        }
        protocolPicker.setMinValue(startValue);
        protocolPicker.setMaxValue(value-1);
        protocolPicker.setFormatter(new ProtocolPickerFormatter());
        enableNumberPickerManualEditing(protocolPicker, false);
        forceNumberPickerFormatting(protocolPicker);
    }

    // Below is mostly UI based code that is not involved in the
    // security protocols and messaging

    private class ProtocolPickerFormatter implements Formatter {
        @Override
        public String format(int value) {
            return securityProtocolNames.get(value);
        }
    }

    public static void enableNumberPickerManualEditing(NumberPicker numPicker,
            boolean isEnabled) {
        int childCount = numPicker.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View childView = numPicker.getChildAt(i);

            if (childView instanceof EditText) {
                EditText et = (EditText) childView;
                et.setFocusable(isEnabled);
                return;
            }
        }
    }

    public static void forceNumberPickerFormatting(NumberPicker numberPicker) {
        try {
            Method method = numberPicker.getClass()
                    .getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(numberPicker, true);
        } catch (Exception e) {}
    }

    private void showDialog() {
        protocolPicker.setValue(securityProtocolValues.get(
                securityMessageLayer.getSecurityProtocolType().ordinal()));

        if (dialog==null){
            dialog = new AlertDialog.Builder(context)
            .setTitle("Choose a security protocol.")
            .setPositiveButton("Ok", new DialogPositiveButtonListener())
            .setNegativeButton("Cancel", new DialogCancelButtonListener())
            .setView(protocolPicker).show();
        } else {
            dialog.show();
        }
    }

    private class DialogPositiveButtonListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            securityMessageLayer.setSecurityProtocolType(
                    securityProtocolTypes.get(
                            protocolPicker.getValue()));
        }
    }

    private class DialogCancelButtonListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            protocolPicker.setValue(securityProtocolValues.get(
                    securityMessageLayer.getSecurityProtocolType().ordinal()));
            dialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v==dialogButton) {
            showDialog();
        }
    }
}
