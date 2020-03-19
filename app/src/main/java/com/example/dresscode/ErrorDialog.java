package com.example.dresscode;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class ErrorDialog extends Dialog implements View.OnClickListener {
    private Activity c;
    private Context mContext;
    private Button okButton;
    private Boolean isPost;
    private OnMyDialogResult mDialogResult;
    private TextView contentTextView;

    public ErrorDialog(Activity a, Context i_Context, Boolean isPost) {
        super(a);
        c = a;
        mContext = i_Context;
        this.isPost = isPost;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.error_dialog);

        if(!isPost){
            contentTextView = (TextView)findViewById(R.id.contentTextView);
            contentTextView.setText("We weren't able to access the DB.\nSome of the data is not updated.\nPlease try again later.");
        }

        okButton = (Button)findViewById(R.id.okButton);
        okButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        String result = "";
        switch (v.getId()) {
            case R.id.okButton:
                this.dismiss();
                mDialogResult.finish(result); // dialog finished its work
                break;
            default:
                break;
        }
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }
    public interface OnMyDialogResult{
        void finish(String result);
    }
}
