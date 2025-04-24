package com.example.ac2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

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

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(id, builder.build());

                Log.d("MedicamentoApp", "Notificação exibida para medicamento ID: " + id);
            } else {
                Log.e("MedicamentoApp", "Permissão de notificação não concedida");
            }
        } else {
            notificationManager.notify(id, builder.build());

            Log.d("MedicamentoApp", "Notificação exibida para medicamento ID: " + id);
        }


        agendarProximaNotificacao(context, id, nome, intent.getStringExtra("horario"));
    }

    private void agendarProximaNotificacao(Context context, int id, String nome, String horario) {

    }
}