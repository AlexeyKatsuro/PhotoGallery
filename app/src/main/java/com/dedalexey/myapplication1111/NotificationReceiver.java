package com.dedalexey.myapplication1111;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Created by Alexey on 17.02.2018.
 */

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        if (getResultCode() != Activity.RESULT_OK ) {
            return;
        }
        int requestCode = i.getIntExtra(PollService.REQUEST_CODE, 0);
        Notification notification = (Notification)
                i.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(c);
        notificationManager.notify(requestCode, notification);
    }
}
