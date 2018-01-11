package com.ihs.inputmethod.uimodules.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.ihs.inputmethod.uimodules.R;


public final class CustomDialog extends Dialog implements OnClickListener {
    Build build;

    private CustomDialog(Context context, Build build) {
        super(context);
        this.build = build;
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_dialog_delete);
        setCanceledOnTouchOutside(false);
        Button cancel = findViewById(R.id.dialog_cancel_button);
        Button ok = findViewById(R.id.dialog_ok_button);
        cancel.setOnClickListener(this);
        ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (build.listener != null) {
            if (v.getId() == R.id.dialog_cancel_button) {
                build.listener.onNegativeButtonClick();
                dismiss();
            } else if (v.getId() == R.id.dialog_ok_button) {
                build.listener.onPositiveButtonClick();
                dismiss();
            }
        }
    }

    public static class Build {
        DialogClickListener listener;

        public Build() {
        }

        public Build setDialogClickListener(DialogClickListener listener) {
            this.listener = listener;
            return this;
        }

        public CustomDialog create(Context context) {
            return new CustomDialog(context, this);
        }

    }

    public interface DialogClickListener {
        void onNegativeButtonClick();

        void onPositiveButtonClick();
    }

}
