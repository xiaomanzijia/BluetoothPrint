package cn.com.yyt.mybluetooth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by licheng on 27/4/16.
 */
public class EditPrintDialog extends Dialog implements View.OnClickListener {

    private Button btnEdit,btnPrint;

    private Context mContext;

    private BtnListener listener;

    public void setListener(BtnListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }

    interface BtnListener{
        void onClick(View v);
    }


    public EditPrintDialog(Context context) {
        super(context);
        this.mContext = context;
    }


    public EditPrintDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_edit_print);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnPrint = (Button) findViewById(R.id.btnPrint);
        btnEdit.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
    }
}
