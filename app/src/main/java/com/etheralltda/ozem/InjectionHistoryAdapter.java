package com.etheralltda.ozem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InjectionHistoryAdapter extends RecyclerView.Adapter<InjectionHistoryAdapter.InjectionViewHolder> {

    private List<InjectionEntry> entries;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public InjectionHistoryAdapter(List<InjectionEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public InjectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_injection, parent, false);
        return new InjectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InjectionViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        // Mostra do mais recente para o mais antigo
        int index = entries.size() - 1 - position;
        InjectionEntry entry = entries.get(index);

        String dateStr = sdf.format(new Date(entry.getTimestamp()));
        String medName = entry.getMedicationName();
        if (medName == null || medName.trim().isEmpty()) {
            medName = "Medicamento";
        }
        String locLabel = getLocationLabel(context, entry.getLocationCode());

        String text = context.getString(R.string.injection_item_format, dateStr, medName, locLabel);
        holder.txtInjectionItem.setText(text);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class InjectionViewHolder extends RecyclerView.ViewHolder {

        TextView txtInjectionItem;

        public InjectionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtInjectionItem = itemView.findViewById(R.id.txtInjectionItem);
        }
    }

    private String getLocationLabel(Context context, String code) {
        if ("abdomen".equals(code)) {
            return context.getString(R.string.injection_location_abdomen);
        } else if ("thigh".equals(code)) {
            return context.getString(R.string.injection_location_thigh);
        } else if ("arm".equals(code)) {
            return context.getString(R.string.injection_location_arm);
        }
        return code != null ? code : "";
    }
}
