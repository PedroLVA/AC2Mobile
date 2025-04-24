package com.example.ac2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            MedicamentoDBHelper dbHelper = new MedicamentoDBHelper(context);
            Cursor cursor = dbHelper.listarMedicamentos();

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_ID));
                    String nome = cursor.getString(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_NOME));
                    String horario = cursor.getString(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_HORARIO));


                    Intent notificationIntent = new Intent(context, MedicamentoNotificationReceiver.class);
                    notificationIntent.putExtra("id", id);
                    notificationIntent.putExtra("nome", nome);

                    String[] partes = horario.split(":");
                    int hora = Integer.parseInt(partes[0]);
                    int minuto = Integer.parseInt(partes[1]);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hora);
                    calendar.set(Calendar.MINUTE, minuto);
                    calendar.set(Calendar.SECOND, 0);

                    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            context,
                            id,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent
                    );
                } while (cursor.moveToNext());
            }
            cursor.close();
            dbHelper.close();
        }
    }
}
