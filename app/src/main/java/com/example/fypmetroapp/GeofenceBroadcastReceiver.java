package com.example.fypmetroapp;

import android.app.NotificationChannel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.content.ContentValues.TAG;
import static android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
import static android.provider.Settings.System.getString;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            String locId = triggeringGeofences.get(0).getRequestId();
            sendNotification(locId, context);

            Intent serviceIntent = new Intent(context, LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            }else{
                context.startService(serviceIntent);
            }
        } else {
            // Log the error.
            Log.e(TAG, "Error");
        }
    }

    private void sendNotification(String locId, Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.CHANNEL_ID)
                .setSmallIcon(R.drawable.track_location)
                .setContentTitle("Location Reached")
                .setContentText(" you reached " + locId)
                .setContent(null)
                .setSound(DEFAULT_NOTIFICATION_URI)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }
}