package com.example.ac2;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MarcarTomadoService extends IntentService {
    public MarcarTomadoService() {
        super("MarcarTomadoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int id = intent.getIntExtra("id", -1);
            if (id != -1) {
                MedicamentoDBHelper dbHelper = new MedicamentoDBHelper(this);
                dbHelper.marcarComoTomado(id, 1);
                dbHelper.close();

                NotificationManagerCompat.from(this).cancel(id);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "medicamentos_channel")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(getString(R.string.medicamento_tomado))
                        .setContentText(getString(R.string.medicamento_marcado_como_tomado))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                NotificationManagerCompat.from(this).notify(id + 1000, builder.build());
            }
        }
    }
}