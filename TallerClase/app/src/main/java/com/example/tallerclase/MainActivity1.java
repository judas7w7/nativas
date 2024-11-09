package com.example.tallerclase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

// Clase MainActivity1 para mostrar un mapa interactivo
public class MainActivity1 extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    // Variables para los campos de texto y el mapa
    EditText txtLatitud, txtLongitud;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        // Asociar campos de texto para latitud y longitud
        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);

        // Inicializar el fragmento de mapa y configurar su callback
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // Método que se ejecuta cuando el mapa está listo
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configurar los eventos de clic y clic largo en el mapa
        this.mMap.setOnMapClickListener(this);
        this.mMap.setOnMapLongClickListener(this);

        // Establecer un marcador en México y mover la cámara a esa posición
        LatLng mexico = new LatLng(19.8077463, -99.4077038);
        mMap.addMarker(new MarkerOptions().position(mexico).title("México"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mexico));
    }

    // Método que se ejecuta al hacer clic en el mapa
    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        // Mostrar latitud y longitud del lugar seleccionado en los campos de texto
        txtLatitud.setText(String.valueOf(latLng.latitude));
        txtLongitud.setText(String.valueOf(latLng.longitude));

        // Limpiar marcadores previos y agregar uno en la nueva posición
        mMap.clear();
        LatLng mexico = new LatLng(latLng.latitude, latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(mexico).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mexico));
    }

    // Método que se ejecuta al hacer clic largo en el mapa
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        try {
            // Intentar convertir los valores de latitud y longitud ingresados en números
            double latitud = Double.parseDouble(txtLatitud.getText().toString());
            double longitud = Double.parseDouble(txtLongitud.getText().toString());

            // Crear una LatLng con las coordenadas especificadas
            LatLng posicion = new LatLng(latitud, longitud);

            // Limpiar el mapa y agregar un marcador en la ubicación indicada
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(posicion).title("Ubicación especificada"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));

        } catch (NumberFormatException e) {
            // Mostrar error si la latitud o longitud ingresada no es válida
            txtLatitud.setError("Latitud no válida");
            txtLongitud.setError("Longitud no válida");
        }
    }

    // Método para volver a la actividad de GPS al hacer clic en un botón
    public void onGps(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
