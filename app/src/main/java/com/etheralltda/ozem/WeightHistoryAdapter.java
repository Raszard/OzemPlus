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

public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.WeightViewHolder> {

    private List<WeightEntry> entries;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public WeightHistoryAdapter(List<WeightEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight, parent, false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        // Mostra do mais recente para o mais antigo
        int index = entries.size() - 1 - position;
        WeightEntry entry = entries.get(index);

        String dateStr = sdf.format(new Date(entry.getTimestamp()));
        String text = context.getString(R.string.weight_item_format, dateStr, entry.getWeight());
        holder.txtWeightItem.setText(text);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder {

        TextView txtWeightItem;

        public WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            txtWeightItem = itemView.findViewById(R.id.txtWeightItem);
        }
    }
}
