package ru.bmstu.evernote.activities;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ru.bmstu.evernote.EvernoteUtil;
import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.database.ContentProviderHelperService;
import ru.bmstu.evernote.provider.database.IClientAPI;

/**
 * Created by Sun on 27.12.2014.
 */
public class RemoveNotebookDialog extends DialogFragment implements View.OnClickListener {
    private IClientAPI mService = null;
    private long notebookId;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ContentProviderHelperService.ContentProviderHelperBinder binder = (ContentProviderHelperService.ContentProviderHelperBinder) iBinder;
            mService = binder.getClientApiService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notebookId = getArguments().getLong(ItemDetailFragment.ARG_ITEM_ID);
    }

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
        getDialog().setTitle("Удалить блокнот?");
        View v = inflater.inflate(R.layout.edit_notebook_dialog, container);
        v.findViewById(R.id.edit_text).setVisibility(View.GONE);
        v.findViewById(R.id.btnYes).setOnClickListener(this);
        v.findViewById(R.id.btnNo).setOnClickListener(this);

        Button buttonYes = (Button)v.findViewById(R.id.btnYes);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mService.deleteNotebook(notebookId);
                dismiss();
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