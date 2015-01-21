package ru.bmstu.evernote.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.bmstu.evernote.R;
import ru.bmstu.evernote.provider.database.ContentProviderHelperService;
import ru.bmstu.evernote.provider.database.IClientAPI;

/**
 * Created by Admin on 21.01.2015.
 */
public class CreateNoteActivity extends Activity implements View.OnClickListener {

    private IClientAPI mService = null;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_notebook_dialog);

        Button yes = (Button) findViewById(R.id.btnYes);
        yes.setOnClickListener(this); // calling onClick() method
        Button no = (Button) findViewById(R.id.btnNo);
        no.setOnClickListener(this);
        TextView simpleNotebook = (TextView) findViewById(R.id.simpleNotebook);
        Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
        Typeface regular = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
        Typeface slabRegular = Typeface.createFromAsset(getAssets(), "fonts/RobotoSlab-Regular.ttf");
        yes.setTypeface(bold);
        no.setTypeface(regular);
        simpleNotebook.setTypeface(slabRegular);

        editText = (EditText)this.findViewById(R.id.edit_text);
        editText.setTypeface(regular);
//        notesId = getArguments().getLong(ItemDetailFragment.ARG_ITEM_ID);
    }
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ContentProviderHelperService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mService != null) {
            this.unbindService(serviceConnection);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btnYes:
                if (editText.getText().toString().trim().length() > 0){
                    mService.insertNotebook(editText.getText().toString());
                    finish();
                } else {
                    Context context = getApplicationContext();
                    CharSequence text = "Sorry, Mario, our princess is in another castle! =(";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                break;
            case R.id.btnNo:
                finish();
                break;
            default:
                break;
        }
    }
}
