package com.etheralltda.ozem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button; // Importante
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class JourneyGlp1Activity extends AppCompatActivity {

    // Views
    private TextView btnBack;
    private TextView txtDaysBadge;
    private Button btnUpdateWeight; // Alterado para Button conforme XML
    private TextView btnCamera;     // TextView clicável conforme XML
    private LineChart chartWeight;
    private LinearLayout cardPhoto, llPhotoGalleryContainer;
    private TextView txtInfoWeight, txtInfoDiff; // Adicionado para preencher dados

    // Launcher Câmera
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        String savedUriStr = PhotoStorage.saveBitmapToFile(this, imageBitmap);
                        if (savedUriStr != null) {
                            PhotoStorage.savePhotoEntry(this, new PhotoStorage.PhotoEntry(savedUriStr, System.currentTimeMillis()));
                            atualizarGaleriaFotos();
                            Toast.makeText(this, "Foto salva!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erro ao salvar foto.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_glp1);

        initViews();
        setupListeners();

        // Carrega dados
        carregarResumoPeso();
        configurarGraficoPeso();
        calcularDiasJornada();
        atualizarGaleriaFotos();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtDaysBadge = findViewById(R.id.txtDaysBadge);
        btnUpdateWeight = findViewById(R.id.btnUpdateWeight);
        chartWeight = findViewById(R.id.chartWeight);
        cardPhoto = findViewById(R.id.cardPhoto);
        btnCamera = findViewById(R.id.btnCamera);
        llPhotoGalleryContainer = findViewById(R.id.llPhotoGalleryContainer);
        txtInfoWeight = findViewById(R.id.txtInfoWeight);
        txtInfoDiff = findViewById(R.id.txtInfoDiff);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // CORREÇÃO: Redireciona para WeightActivity (não WeightTrackerActivity)
        btnUpdateWeight.setOnClickListener(v -> startActivity(new Intent(this, WeightActivity.class)));

        btnCamera.setOnClickListener(v -> abrirCamera());
    }

    private void abrirCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Câmera indisponível", Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarGaleriaFotos() {
        llPhotoGalleryContainer.removeAllViews();
        List<PhotoStorage.PhotoEntry> photos = PhotoStorage.loadPhotos(this);

        if (photos.isEmpty()) {
            TextView placeholder = new TextView(this);
            placeholder.setText("Sem fotos ainda.");
            placeholder.setTextColor(Color.GRAY);
            placeholder.setPadding(16, 16, 16, 16);
            llPhotoGalleryContainer.addView(placeholder);
            return;
        }

        for (PhotoStorage.PhotoEntry photo : photos) {
            ImageView iv = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(250, 250);
            params.setMargins(0, 0, 16, 0);
            iv.setLayoutParams(params);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setClipToOutline(true);
            iv.setBackgroundResource(R.drawable.bg_surface_rounded);

            try {
                iv.setImageURI(Uri.parse(photo.getUriString()));
                llPhotoGalleryContainer.addView(iv, 0); // Adiciona no início (mais recente primeiro)
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void carregarResumoPeso() {
        UserProfile profile = UserStorage.loadUserProfile(this);
        if (profile != null && profile.getCurrentWeight() > 0) {
            float current = profile.getCurrentWeight();
            txtInfoWeight.setText(String.format(Locale.getDefault(), "%.1f kg", current));

            float target = profile.getTargetWeight();
            if (target > 0) {
                float diff = current - target;
                String diffText = String.format(Locale.getDefault(), "%.1f kg", diff);
                txtInfoDiff.setText((diff > 0 ? "+" : "") + diffText + " da meta");
            }
        }
    }

    private void calcularDiasJornada() {
        long oldestTimestamp = Long.MAX_VALUE;
        List<WeightEntry> weights = WeightStorage.loadWeights(this);
        for (WeightEntry w : weights) if (w.getTimestamp() < oldestTimestamp) oldestTimestamp = w.getTimestamp();

        List<InjectionEntry> injections = InjectionStorage.loadInjections(this);
        for (InjectionEntry i : injections) if (i.getTimestamp() < oldestTimestamp) oldestTimestamp = i.getTimestamp();

        if (oldestTimestamp == Long.MAX_VALUE) oldestTimestamp = System.currentTimeMillis();

        long now = System.currentTimeMillis();
        long diffMillis = now - oldestTimestamp;
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis) + 1;

        txtDaysBadge.setText(days + " dias");
    }

    private void configurarGraficoPeso() {
        if (chartWeight == null) return;
        List<WeightEntry> weightList = WeightStorage.loadWeights(this);

        if (weightList.isEmpty()) {
            chartWeight.setNoDataText("Registre seu peso para ver o gráfico.");
            chartWeight.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < weightList.size(); i++) {
            entries.add(new Entry(i, weightList.get(i).getWeight()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Peso (kg)");
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setColor(Color.parseColor("#10B981"));
        dataSet.setCircleColor(Color.parseColor("#10B981"));
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chartWeight.setData(lineData);
        chartWeight.getDescription().setEnabled(false);
        chartWeight.getAxisRight().setEnabled(false);
        chartWeight.getXAxis().setEnabled(false);
        chartWeight.invalidate();
    }
}