package com.etheralltda.ozem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import java.util.Collections;
import java.util.List;

public class PhotoGalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView recycler = findViewById(R.id.recyclerPhotos);
        recycler.setLayoutManager(new GridLayoutManager(this, 2)); // 2 colunas

        List<PhotoStorage.PhotoEntry> photos = PhotoStorage.loadPhotos(this);
        Collections.reverse(photos); // Mais recentes primeiro

        // Agora podemos usar o Adapter diretamente, sem gambiarras
        PhotoGalleryAdapter adapter = new PhotoGalleryAdapter(photos);
        recycler.setAdapter(adapter);
    }
}