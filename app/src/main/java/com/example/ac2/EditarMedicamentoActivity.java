package com.example.ac2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.Locale;

public class EditarMedicamentoActivity extends AppCompatActivity {
    private MedicamentoDBHelper dbHelper;
    private EditText editNome, editDescricao, editHorario;
    private int medicamentoId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_medicamento);

        dbHelper = new MedicamentoDBHelper(this);

        editNome = findViewById(R.id.editNome);
        editDescricao = findViewById(R.id.editDescricao);
        editHorario = findViewById(R.id.editHorario);


        Intent intent = getIntent();
        if (intent.hasExtra("id")) {
            medicamentoId = intent.getIntExtra("id", -1);
            carregarMedicamento(medicamentoId);
        }


        editHorario.setOnClickListener(v -> mostrarTimePicker());


        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvarMedicamento());
    }

    private void carregarMedicamento(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                MedicamentoDBHelper.TABLE_MEDICAMENTOS,
                null,
                MedicamentoDBHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            editNome.setText(cursor.getString(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_NOME)));
            editDescricao.setText(cursor.getString(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_DESCRICAO)));
            editHorario.setText(cursor.getString(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_HORARIO)));
        }
        cursor.close();
    }

    private void mostrarTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);
        int minuto = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String horario = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    editHorario.setText(horario);
                },
                hora, minuto, true
        );
        timePickerDialog.show();
    }

    private void salvarMedicamento() {
        String nome = editNome.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();
        String horario = editHorario.getText().toString().trim();

        if (nome.isEmpty()) {
            editNome.setError(getString(R.string.campo_obrigatorio));
            return;
        }

        if (horario.isEmpty()) {
            editHorario.setError(getString(R.string.campo_obrigatorio));
            return;
        }

        if (medicamentoId == -1) {

            long id = dbHelper.inserirMedicamento(nome, descricao, horario);
            if (id != -1) {
                Toast.makeText(this, getString(R.string.medicamento_adicionado), Toast.LENGTH_SHORT).show();
                agendarNotificacao((int) id, nome, horario);
            }
        } else {

            int rows = dbHelper.atualizarMedicamento(medicamentoId, nome, descricao, horario, 0);
            if (rows > 0) {
                Toast.makeText(this, getString(R.string.medicamento_atualizado), Toast.LENGTH_SHORT).show();

                cancelarNotificacao(medicamentoId);
                agendarNotificacao(medicamentoId, nome, horario);
            }
        }

        finish();
    }

    private void agendarNotificacao(int id, String nome, String horario) {
        Intent intent = new Intent(this, MedicamentoNotificationReceiver.class);
        intent.putExtra("id", id);
        intent.putExtra("nome", nome);

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
                this,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private void cancelarNotificacao(int id) {
        Intent intent = new Intent(this, MedicamentoNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}