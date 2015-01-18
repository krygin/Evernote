package ru.bmstu.evernote.activities;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.database.ContentProviderHelperService;
import ru.bmstu.evernote.provider.database.IClientAPI;

import static ru.bmstu.evernote.provider.database.EvernoteContract.Notebooks;

/**
 * Created by Sun on 27.12.2014.
 */
public class RemoveNoteDialog extends DialogFragment implements View.OnClickListener {
    private IClientAPI mService = null;
    private long notebooksId;

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
        getDialog().setTitle("Удалить заметку");
        View v = inflater.inflate(R.layout.remove_note_dialog, container);
        Cursor cursor = getActivity().getContentResolver().query(
                Notebooks.CONTENT_URI,
                Notebooks.ALL_COLUMNS_PROJECTION,
                null, null, null);
        SimpleCursorAdapter spinnerAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_item, cursor, new String[]{Notebooks.NAME}, new int[]{android.R.id.text1}, 0);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner)v.findViewById(R.id.spinner);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor c = (Cursor)adapterView.getItemAtPosition(i);
                notebooksId = c.getLong(c.getColumnIndex(Notebooks._ID));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Cursor c = (Cursor)adapterView.getItemAtPosition(0);
                notebooksId = c.getLong(c.getColumnIndex(Notebooks._ID));
            }
        });
        v.findViewById(R.id.btnYes).setOnClickListener(this);
        v.findViewById(R.id.btnNo).setOnClickListener(this);
        editText = (EditText)v.findViewById(R.id.edit_text);
        return v;
    }

    @Override
    public void onClick(View view) {
        //mService.deleteNote();
        dismiss();
    }
}
