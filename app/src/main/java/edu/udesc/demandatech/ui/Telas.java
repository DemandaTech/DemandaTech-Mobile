package edu.udesc.demandatech.ui;

import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import edu.udesc.demandatech.R;

public final class Telas {

    private Telas() {
    }

    public static void abrir(AppCompatActivity activity, int layout) {
        Window janela = activity.getWindow();
        WindowCompat.enableEdgeToEdge(janela);
        activity.setContentView(layout);

        View raiz = activity.findViewById(R.id.raiz);
        WindowInsetsControllerCompat barras = WindowCompat.getInsetsController(janela, raiz);
        barras.setAppearanceLightStatusBars(true);
        barras.setAppearanceLightNavigationBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets areas = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(areas.left, areas.top, areas.right, areas.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
