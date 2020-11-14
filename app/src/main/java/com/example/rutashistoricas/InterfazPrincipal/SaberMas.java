package com.example.rutashistoricas.InterfazPrincipal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.rutashistoricas.R;

public class SaberMas extends AppCompatActivity {
    private static int index_pnj = 0;
    private static String  nombre = "",
            biografia = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saber_mas);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        if (b != null) {
            index_pnj = b.getInt("idPnj");
            switch (index_pnj) {
                case 1:
                    nombre = getString(R.string.nombre_federico);
                    biografia = getString(R.string.biografia_federico);
            }
        }

        setTitle(nombre);
        TextView textView = findViewById(R.id.biografia);
        textView.setText(biografia);


        Button botonLink = findViewById(R.id.botonLink);

        String url = getString(R.string.url_federico);

        botonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }
}