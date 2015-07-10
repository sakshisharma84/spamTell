package edu.gatech.sakshi.spamtell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by ketanbj on 11/28/2014.
 */
public class SpamReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SPAMTELL_ACT", "Broadcast reciever");

        Bundle extras = intent.getExtras();
        if (extras != null) {
            // OFFHOOK
            String state = extras.getString(TelephonyManager.EXTRA_STATE);
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                //FIXME: Some issue with stopping the activity
                Intent i = new Intent(context, edu.gatech.sakshi.spamtell.SpamTellActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //i.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                context.startActivity(i);
                Log.d("SPAMTELL_ACT", "Starting spamTellActivity");
            }
        }

    }
}
