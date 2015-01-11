package ru.bmstu.evernote.activities;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.database.ContentProviderHelperService;
import ru.bmstu.evernote.provider.database.IClientAPI;

/**
 * Created by Ivan on 15.12.2014.
 */
public class CreateNotebookDialog extends DialogFragment implements View.OnClickListener {

    private IClientAPI mService = null;
    DialogInterface dialog;
    private EditText editText = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ContentProviderHelperService.ContentProviderHelperBinder binder = (ContentProviderHelperService.ContentProviderHelperBinder)iBinder;
            mService = binder.getClientApiService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this.getActivity(), ContentProviderHelperService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mService != null) {
            getActivity().unbindService(serviceConnection);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Добавить блокнот");
        View v = inflater.inflate(R.layout.create_notebook_dialog, container);
        v.findViewById(R.id.btnYes).setOnClickListener(this);
        v.findViewById(R.id.btnNo).setOnClickListener(this);
        editText = (EditText)v.findViewById(R.id.edit_text);
        Button buttonYes = (Button)v.findViewById(R.id.btnYes);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editText.getText().toString().trim().length() > 0){
                    mService.insertNotebook(editText.getText().toString());
                    dismiss();
                } else {
                    Context context = getActivity();
                    CharSequence text = "Sorry, Mario, our princess is in another castle! =(";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
        Button buttonNo = (Button)v.findViewById(R.id.btnNo);
        buttonNo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;
    }

    @Override
    public void onClick(View view) {
    }
}