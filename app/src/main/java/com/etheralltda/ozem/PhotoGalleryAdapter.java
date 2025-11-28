package com.etheralltda.ozem;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoGalleryAdapter.PhotoViewHolder> {

    private List<PhotoStorage.PhotoEntry> photos;

    public PhotoGalleryAdapter(List<PhotoStorage.PhotoEntry> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CORREÇÃO: Apontando para o layout correto criado anteriormente
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_gallery, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        PhotoStorage.PhotoEntry photo = photos.get(position);
        try {
            holder.imgPhoto.setImageURI(Uri.parse(photo.getUriString()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.txtDate.setText(sdf.format(new Date(photo.getTimestamp())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPhoto;
        TextView txtDate;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgItemPhoto);
            txtDate = itemView.findViewById(R.id.txtItemDate);
        }
    }
}