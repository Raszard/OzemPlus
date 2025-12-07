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

public class SymptomHistoryAdapter extends RecyclerView.Adapter<SymptomHistoryAdapter.SymptomViewHolder> {

    private List<SymptomEntry> entries;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public SymptomHistoryAdapter(List<SymptomEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_symptom, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        int index = entries.size() - 1 - position;
        SymptomEntry entry = entries.get(index);

        String dateStr = sdf.format(new Date(entry.getTimestamp()));
        String base = context.getString(
                R.string.symptoms_item_format,
                dateStr,
                entry.getNausea(),
                entry.getFatigue(),
                entry.getSatiety()
        );

        String notes = entry.getNotes();
        if (notes != null && !notes.trim().isEmpty()) {
            base = base + " - " + notes.trim();
        }

        holder.txtSymptomItem.setText(base);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class SymptomViewHolder extends RecyclerView.ViewHolder {
        TextView txtSymptomItem;
        public SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSymptomItem = itemView.findViewById(R.id.txtSymptomItem);
        }
    }
}