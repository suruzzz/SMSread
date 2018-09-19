package com.example.thasleema.smsread;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.thasleema.smsread.model.Message;

import com.example.thasleema.smsread.database.AsynkResponse;
import com.example.thasleema.smsread.database.Pushdata;

public class MainActivity extends AppCompatActivity {
    private int PHONE_REQUEST = 101;

    Button b1;
    EditText number;
    String num ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = findViewById(R.id.sms);
        number = (EditText) findViewById(R.id.no);
        num = number.getText().toString();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            //    Log.i(TAG, "onCreate: No permission.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PHONE_REQUEST);
        } else {

            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchMessages();
                }
            });
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PHONE_REQUEST) {
            if (grantResults != null && grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchMessages();
                    Intent i1 = new Intent (this,MessageReceiver.class);
                   // SmsMessage.MessageClass messageClass = new SmsMessage.MessageClass(this,MessageReceiver.class);

                } else {
                    //  Log.i(TAG, "onRequestPermissionsResult: Permission denied");
                }
            } else {
                // Log.i(TAG, "onRequestPermissionsResult: Permission denied");
            }

        }
    }

    private void fetchMessages() {



    StringBuilder smsBuilder = new StringBuilder();
    final String SMS_URI_INBOX = "content://sms/inbox";
    final String SMS_URI_ALL = "content://sms/";
            try {



        Cursor cur;

        Uri uri = Uri.parse(SMS_URI_INBOX);
        String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
        cur = getContentResolver().query(uri, projection, "", null, "date desc");
        assert cur != null;
        if (cur.moveToFirst()) {
            int index_Address = cur.getColumnIndex("address");
            int index_Person = cur.getColumnIndex("person");
            int index_Body = cur.getColumnIndex("body");
            int index_Date = cur.getColumnIndex("date");
            int index_Type = cur.getColumnIndex("type");
            do {
                String strAddress = cur.getString(index_Address);
                int intPerson = cur.getInt(index_Person);
                String strbody = cur.getString(index_Body);
                long longDate = cur.getLong(index_Date);
                int int_Type = cur.getInt(index_Type);

                Toast.makeText(this, strbody, Toast.LENGTH_SHORT).show();
                String[] params = {Message.getSender(), Message.getPower(), Message.getPump(), Message.getErr(),
                        Message.getEventHrs(),Message.getAuto()};
                //new Pushdata().execute(params);
                Pushdata asyncTask = new Pushdata(new AsynkResponse() {
                    @Override
                    public void processFinish(Object output) {

                        if ((String)output == "Success")
                        {
                            Toast.makeText(MainActivity.this, "Sheet Updated Successfully", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this,"Error in Sheet Updation", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
                asyncTask.execute(params);


                smsBuilder.append("[ ");
                smsBuilder.append(strAddress + ", ");
                smsBuilder.append(intPerson + ", ");
                smsBuilder.append(strbody + ", ");
                smsBuilder.append(longDate + ", ");
                smsBuilder.append(int_Type);
                smsBuilder.append(" ]\n\n");
            } while (cur.moveToNext());

            if (!cur.isClosed()) {
                cur.close();
                cur= null;
            }
        } else {
            smsBuilder.append("no result!");
        } // end if

    } catch (SQLiteException ex) {
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }
}

}






