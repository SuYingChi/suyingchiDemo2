package com.ihs.inputmethod.uimodules.widget;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.ihs.inputmethod.uimodules.R;


public final class CustomFragmentDialog extends DialogFragment implements OnClickListener{
   
    public interface DSDialogListener{
        void onNegativeButtonClick(String tag);
        void onPositiveButtonClick(String tag);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view=inflater.inflate(R.layout.fragment_dialog_delete,container,false);
        Button cancel=(Button) view.findViewById(R.id.dialog_cancel_button);
        Button ok=(Button)view.findViewById(R.id.dialog_ok_button);
        cancel.setOnClickListener(this);
        ok.setOnClickListener(this);
        getDialog().setCanceledOnTouchOutside(false);
        return view;
    }
    private DSDialogListener mListener;
    public void setDSDialogListener(DSDialogListener listener){
        this.mListener=listener;
    }
    @Override
    public void onClick(View v) {
        if(mListener!=null){
            if (v.getId() ==  R.id.dialog_cancel_button) {
                mListener.onNegativeButtonClick(getTag());
                getDialog().dismiss();
            } else if (v.getId() == R.id.dialog_ok_button) {
                mListener.onPositiveButtonClick(getTag());
                getDialog().dismiss();
            }
        }
    }
}
