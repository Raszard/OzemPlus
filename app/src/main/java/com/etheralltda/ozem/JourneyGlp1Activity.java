package com.etheralltda.ozem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class JourneyGlp1Activity extends AppCompatActivity {

    private TextView btnBack;
    private TextView txtDaysBadge;

    // Componentes de imagem
    private ImageView imgLastPhoto;
    private FloatingActionButton btnCamera;

    private LineChart chartWeight;
    private TextView txtInfoWeight, txtInfoDiff, txtInfoBmi, txtInfoDate;

    // Componentes de Sintomas
    private TextView txtNauseaScore, txtFatigueScore, txtSatietyScore;
    private LinearProgressIndicator progressNausea, progressFatigue, progressSatiety;

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
                            atualizarFotoDestaque();
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

        carregarResumoPeso();
        configurarGraficoPeso();
        calcularDiasJornada();
        carregarSintomasRecentes();
        atualizarFotoDestaque();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarResumoPeso();
        configurarGraficoPeso();
        carregarSintomasRecentes();
        atualizarFotoDestaque();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtDaysBadge = findViewById(R.id.txtDaysBadge);
        chartWeight = findViewById(R.id.chartWeight);

        imgLastPhoto = findViewById(R.id.imgLastPhoto);
        btnCamera = findViewById(R.id.btnCamera);

        txtInfoWeight = findViewById(R.id.txtInfoWeight);
        txtInfoDiff = findViewById(R.id.txtInfoDiff);
        txtInfoBmi = findViewById(R.id.txtInfoBmi);
        txtInfoDate = findViewById(R.id.txtInfoDate);

        // Sintomas: Textos e Barras
        txtNauseaScore = findViewById(R.id.txtNauseaScore);
        txtFatigueScore = findViewById(R.id.txtFatigueScore);
        txtSatietyScore = findViewById(R.id.txtSatietyScore);

        progressNausea = findViewById(R.id.progressNausea);
        progressFatigue = findViewById(R.id.progressFatigue);
        progressSatiety = findViewById(R.id.progressSatiety);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCamera.setOnClickListener(v -> abrirCamera());
        imgLastPhoto.setOnClickListener(v -> abrirGaleriaCompleta());
    }

    private void abrirCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Câmera indisponível", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleriaCompleta() {
        Intent intent = new Intent(this, PhotoGalleryActivity.class);
        startActivity(intent);
    }

    private void atualizarFotoDestaque() {
        List<PhotoStorage.PhotoEntry> photos = PhotoStorage.loadPhotos(this);

        if (!photos.isEmpty()) {
            PhotoStorage.PhotoEntry lastPhoto = photos.get(photos.size() - 1);
            try {
                imgLastPhoto.setImageURI(Uri.parse(lastPhoto.getUriString()));
                imgLastPhoto.setPadding(0,0,0,0);
                imgLastPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                e.printStackTrace();
                setPlaceholderImage();
            }
        } else {
            setPlaceholderImage();
        }
    }

    private void setPlaceholderImage() {
        imgLastPhoto.setImageResource(R.drawable.ic_camera);
        int p = (int) (40 * getResources().getDisplayMetrics().density);
        imgLastPhoto.setPadding(p, p, p, p);
        imgLastPhoto.setColorFilter(Color.parseColor("#9CA3AF"));
    }

    private void carregarResumoPeso() {
        UserProfile profile = UserStorage.loadUserProfile(this);
        if (profile != null && profile.getCurrentWeight() > 0) {
            float current = profile.getCurrentWeight();
            txtInfoWeight.setText(String.format(Locale.getDefault(), "%.1fkg", current));

            float target = profile.getTargetWeight();
            if (target > 0) {
                float diff = current - target;
                String diffText = String.format(Locale.getDefault(), "%.1fkg", Math.abs(diff));
                if (diff > 0) {
                    txtInfoDiff.setText("+" + diffText);
                    txtInfoDiff.setTextColor(Color.RED);
                } else {
                    txtInfoDiff.setText("-" + diffText);
                    txtInfoDiff.setTextColor(Color.parseColor("#059669"));
                }
            }

            float height = profile.getHeight();
            if (height > 0) {
                float bmi = current / (height * height);
                txtInfoBmi.setText(String.format(Locale.getDefault(), "%.1f", bmi));
            }
        }
    }

    private void carregarSintomasRecentes() {
        List<SymptomEntry> sintomas = SymptomStorage.loadSymptoms(this);
        if (!sintomas.isEmpty()) {
            SymptomEntry ultimo = sintomas.get(sintomas.size() - 1);

            txtNauseaScore.setText(ultimo.getNausea() + "/5");
            progressNausea.setProgress(ultimo.getNausea());

            txtFatigueScore.setText(ultimo.getFatigue() + "/5");
            progressFatigue.setProgress(ultimo.getFatigue());

            txtSatietyScore.setText(ultimo.getSatiety() + "/5");
            progressSatiety.setProgress(ultimo.getSatiety());
        } else {
            String empty = "0/5";
            txtNauseaScore.setText(empty);
            txtFatigueScore.setText(empty);
            txtSatietyScore.setText(empty);

            progressNausea.setProgress(0);
            progressFatigue.setProgress(0);
            progressSatiety.setProgress(0);
        }
    }

    private void calcularDiasJornada() {
        long oldestTimestamp = Long.MAX_VALUE;
        List<WeightEntry> weights = WeightStorage.loadWeights(this);
        for (WeightEntry w : weights) if (w.getTimestamp() < oldestTimestamp) oldestTimestamp = w.getTimestamp();

        if (oldestTimestamp == Long.MAX_VALUE) oldestTimestamp = System.currentTimeMillis();

        long now = System.currentTimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(now - oldestTimestamp) + 1;
        txtDaysBadge.setText(days + " dias");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        txtInfoDate.setText(sdf.format(new Date(now)));
    }

    private void configurarGraficoPeso() {
        if (chartWeight == null) return;
        List<WeightEntry> weightList = WeightStorage.loadWeights(this);

        if (weightList.isEmpty()) {
            chartWeight.setNoDataText("Sem dados.");
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < weightList.size(); i++) {
            entries.add(new Entry(i, weightList.get(i).getWeight()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setColor(Color.parseColor("#10B981"));
        dataSet.setCircleColor(Color.parseColor("#10B981"));
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chartWeight.setData(lineData);
        chartWeight.getDescription().setEnabled(false);
        chartWeight.getLegend().setEnabled(false);
        chartWeight.getAxisRight().setEnabled(false);
        chartWeight.getXAxis().setEnabled(false);
        chartWeight.invalidate();
    }
}