package com.example.ac2;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 1001;
    private MedicamentoDBHelper dbHelper;
    private ListView listView;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new MedicamentoDBHelper(this);
        listView = findViewById(R.id.listView);

        createNotificationChannel();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditarMedicamentoActivity.class);
            startActivity(intent);
        });

        carregarMedicamentos();

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.excluir_medicamento))
                    .setMessage(getString(R.string.confirmar_exclusao))
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        dbHelper.excluirMedicamento((int) id);
                        carregarMedicamentos();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, EditarMedicamentoActivity.class);
            intent.putExtra("id", (int) id);
            startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION_PERMISSION
                );
            }
        }

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("medicamentos_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void carregarMedicamentos() {
        Cursor cursor = dbHelper.listarMedicamentos();


        String[] from = new String[]{
                MedicamentoDBHelper.COLUMN_NOME,
                MedicamentoDBHelper.COLUMN_HORARIO

        };


        int[] to = new int[]{
                R.id.textNome,
                R.id.textHorario

        };

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.item_medicamento,
                cursor,
                from,
                to,
                0
        ) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);


                ImageView imageStatus = view.findViewById(R.id.imageStatus);
                int tomado = cursor.getInt(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_TOMADO));

                if (tomado == 1) {
                    imageStatus.setImageResource(R.drawable.ic_launcher_background);
                } else {
                    imageStatus.setImageResource(R.drawable.ic_launcher_foreground);
                }
                imageStatus.setColorFilter(ContextCompat.getColor(context, R.color.white));

                view.findViewById(R.id.btnTomado).setOnClickListener(v -> {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_ID));
                    dbHelper.marcarComoTomado(id, tomado == 0 ? 1 : 0);
                    carregarMedicamentos();
                });

                view.findViewById(R.id.btnEditar).setOnClickListener(v -> {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_ID));
                    Intent intent = new Intent(context, EditarMedicamentoActivity.class);
                    intent.putExtra("id", id);
                    context.startActivity(intent);
                });

                view.findViewById(R.id.btnExcluir).setOnClickListener(v -> {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MedicamentoDBHelper.COLUMN_ID));
                    new AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.excluir_medicamento))
                            .setMessage(context.getString(R.string.confirmar_exclusao))
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                dbHelper.excluirMedicamento(id);
                                ((MainActivity)context).carregarMedicamentos();
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                });
            }
        };

        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarMedicamentos();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}