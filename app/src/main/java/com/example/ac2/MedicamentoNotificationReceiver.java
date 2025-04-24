package com.example.ac2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MedicamentoNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        String nome = intent.getStringExtra("nome");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medicamentos_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.hora_do_medicamento))
                .setContentText(context.getString(R.string.hora_de_tomar, nome))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);


        Intent tomarIntent = new Intent(context, MarcarTomadoService.class);
        tomarIntent.putExtra("id", id);
        PendingIntent tomarPendingIntent = PendingIntent.getService(
                context,
                id,
                tomarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        builder.addAction(
                R.drawable.ic_launcher_background,
                context.getString(R.string.marcar_como_tomado),
                tomarPendingIntent
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(id, builder.build());
            }
        } else {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(id, builder.build());
        }
    }
}