package com.romerofernandez.meteoduo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
/**
 * Fragmento que muestra un tutorial interactivo
 * sobre la pantalla principal del usuario.
 *
 * El tutorial guía al usuario paso a paso resaltando distintos
 * elementos de la interfaz mediante un bocadillo informativo y
 * una flecha indicadora.
 *
 * Se muestra únicamente la primera vez que el usuario accede,
 * guardando el estado en SharedPreferences asociado a su UID.
 */
public class TutorialOverlayFragment extends Fragment {


    /**
     * Clase interna que representa un paso del tutorial.
     * Cada paso contiene:
     * - El ID del elemento de la interfaz a destacar.
     * - El mensaje que se mostrará en el bocadillo.
     */
    private static class Step {
        final int targetId;
        final String message;

        Step(int targetId, String message) {
            this.targetId = targetId;
            this.message = message;
        }
    }


    /** Imagen de flecha que apunta al elemento resaltado */
    private ImageView arrow;
    /** Contenedor del bocadillo informativo */
    private LinearLayout bubble;

    /** Texto descriptivo del paso actual */
    private TextView tvMsg;

    /** Botón para avanzar al siguiente paso */
    private Button btnNext;

    /** Lista que almacena todos los pasos del tutorial */
    private final List<Step> steps = new ArrayList<>();

    /** Índice del paso actual */
    private int index = 0;

    /**
     * Método estático para crear una nueva instancia del fragmento.
     *
     * @return instancia de TutorialOverlayFragment
     */
    public static TutorialOverlayFragment newInstance() {
        return new TutorialOverlayFragment();
    }

    /**
     * Infla el layout correspondiente al fragmento.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial, container, false);
    }

    /**
     * Se ejecuta una vez creada la vista.
     * Inicializa componentes, define los pasos y configura el botón.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {

        bubble = v.findViewById(R.id.bubble);
        tvMsg = v.findViewById(R.id.tvMsg);
        btnNext = v.findViewById(R.id.btnNext);
        arrow = v.findViewById(R.id.arrow);

        // Definición de los pasos del tutorial
        steps.add(new Step(R.id.btnMenu, "Aquí podrás acceder a tus ajustes"));
        steps.add(new Step(R.id.imgAvatar, "Este es tu perfil. Podrás personalizar tu avatar y tu nombre."));
        steps.add(new Step(R.id.btnConsultar, "Pulsa aquí para consultar la predicción del tiempo."));
        steps.add(new Step(R.id.btnHistorial, "Aquí verás tu historial de consultas guardadas."));
        steps.add(new Step(R.id.btnclose, "Con este botón puedes salir de la aplicación."));

        // Acción del botón siguiente
        btnNext.setOnClickListener(view -> {
            index++;
            if (index >= steps.size()) {
                finish();
            } else {
                showStep(index);
            }
        });

        // Mostrar primer paso cuando la vista ya esté renderizada
        v.post(() -> showStep(0));
    }

    /**
     * Muestra el paso correspondiente al índice indicado.
     *
     * @param i índice del paso a mostrar
     */
    private void showStep(int i) {

        Step step = steps.get(i);
        View target = requireActivity().findViewById(step.targetId);

        if (target == null) {
            btnNext.performClick();
            return;
        }

        tvMsg.setText(step.message);
        positionBubbleAbove(target);

        btnNext.setText(i == steps.size() - 1 ? "Terminar" : "Siguiente");
    }

    /**
     * Calcula y posiciona dinámicamente el bocadillo y la flecha
     * en relación con el elemento objetivo.
     *
     * @param target vista que se desea destacar
     */
    private void positionBubbleAbove(View target) {

        int[] loc = new int[2];
        target.getLocationOnScreen(loc);

        int targetCenterX = loc[0] + target.getWidth() / 2;
        int targetTop = loc[1];
        int targetBottom = loc[1] + target.getHeight();

        DisplayMetrics dm = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        int screenW = dm.widthPixels;
        int screenH = dm.heightPixels;

        bubble.measure(
                View.MeasureSpec.makeMeasureSpec(screenW, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(screenH, View.MeasureSpec.AT_MOST)
        );

        arrow.measure(
                View.MeasureSpec.makeMeasureSpec(screenW, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(screenH, View.MeasureSpec.AT_MOST)
        );

        int bubbleW = bubble.getMeasuredWidth();
        int bubbleH = bubble.getMeasuredHeight();
        int arrowW = arrow.getMeasuredWidth();
        int arrowH = arrow.getMeasuredHeight();

        // Centrado horizontal
        int x = targetCenterX - bubbleW / 2;

        if (x < 24) x = 24;
        if (x + bubbleW > screenW - 24)
            x = screenW - bubbleW - 24;

        // Intentar colocar arriba
        int y = targetTop - bubbleH - arrowH - 20;
        boolean placedAbove = true;

        if (y < 100) {
            y = targetBottom + arrowH + 20;
            placedAbove = false;
        }

        bubble.setX(x);
        bubble.setY(y);

        int arrowX = targetCenterX - arrowW / 2;

        if (placedAbove) {
            arrow.setRotation(0f);
            arrow.setX(arrowX);
            arrow.setY(y + bubbleH - 8);
        } else {
            arrow.setRotation(180f);
            arrow.setX(arrowX);
            arrow.setY(y - arrowH + 8);
        }
    }

    /**
     * Finaliza el tutorial.
     *
     * Guarda en SharedPreferences que el usuario ya lo ha visto
     * y elimina el fragmento de la pantalla.
     */
    private void finish() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            SharedPreferences sp = requireActivity()
                    .getSharedPreferences("prefs", Context.MODE_PRIVATE);

            sp.edit().putBoolean("tutorial_" + uid, true).apply();
        }

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .remove(this)
                .commitAllowingStateLoss();
    }
}
