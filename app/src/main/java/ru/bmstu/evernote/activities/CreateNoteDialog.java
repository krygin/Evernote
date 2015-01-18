package ru.bmstu.evernote.activities;

import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import ru.bmstu.evernote.EvernoteUtil;
import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.database.ContentProviderHelperService;
import ru.bmstu.evernote.provider.database.IClientAPI;

import static ru.bmstu.evernote.provider.database.EvernoteContract.Notebooks;

/**
 * Created by Sun on 27.12.2014.
 */
public class CreateNoteDialog extends DialogFragment implements View.OnClickListener {
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
    String content = EvernoteUtil.NOTE_PREFIX + "Content of Note" + EvernoteUtil.NOTE_SUFFIX;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Добавить заметку");

        View v = inflater.inflate(R.layout.create_note_dialog, container);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        Cursor cursor = getActivity().getContentResolver().query(
                Notebooks.CONTENT_URI,
                Notebooks.ALL_COLUMNS_PROJECTION,
                Notebooks.NOT_DELETED_SELECTION,
                null, null);
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
        Button buttonYes = (Button)v.findViewById(R.id.btnYes);
        Log.e("","@!(#*$^!&(*@^#$%)(!&@^#$(*&!@#^$(!*&#$^!(@#*&$");
        Log.e("","!@$)(#*%&^!@(*&#^$!*(@#&$^(!@*#&$^(!#@*&$^!(*&");
        Log.e("",content);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editText.getText().toString().trim().length() > 0){
                    mService.insertNote(editText.getText().toString(), content, notebooksId);
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
