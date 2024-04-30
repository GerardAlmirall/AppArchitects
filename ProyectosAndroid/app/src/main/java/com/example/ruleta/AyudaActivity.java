package com.example.ruleta;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class AyudaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayuda);

        // Encuentra el WebView en el layout
        WebView webView = findViewById(R.id.webView);

        // Habilita JavaScript (si es necesario)
        webView.getSettings().setJavaScriptEnabled(true);

        // Carga la URL de la página de ayuda
        webView.loadUrl("https://sites.google.com/d/1to59FYNrisfHnvrTNs8h3CwIw32oRi_o/p/1dIK1vKyj-e7R30xIql-TTiOszyFnvulb/edit");

        // Configura un WebViewClient para manejar las interacciones dentro del WebView
        webView.setWebViewClient(new WebViewClient());
    }
}
