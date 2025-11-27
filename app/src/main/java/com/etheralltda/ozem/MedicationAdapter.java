package com.etheralltda.ozem;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedViewHolder> {

    private List<Medication> medications;

    public MedicationAdapter(List<Medication> medications) {
        this.medications = medications;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication, parent, false);
        return new MedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        Medication med = medications.get(position);

        String name = med.getName();
        String dose = med.getDose();
        String freq = med.getFrequency();
        String nextDate = med.getNextDate();

        if (name == null || name.trim().isEmpty()) name = "Medicamento";
        if (dose == null) dose = "";
        if (freq == null) freq = "";
        if (nextDate == null) nextDate = "";

        holder.txtName.setText(name);
        holder.txtDose.setText("Dose: " + dose);
        holder.txtFrequency.setText("Frequência: " + freq);
        holder.txtNextDate.setText("Próxima aplicação: " + nextDate);

        String finalName = name;
        String finalDose = dose;
        String finalFreq = freq;
        String finalNextDate = nextDate;

        // Clique normal: abre detalhes
        holder.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent intent = new Intent(ctx, MedicationDetailsActivity.class);
            intent.putExtra("medName", finalName);
            intent.putExtra("medDose", finalDose);
            intent.putExtra("medFreq", finalFreq);
            intent.putExtra("medNextDate", finalNextDate);
            ctx.startActivity(intent);
        });

        // Clique longo: remover medicamento
        holder.itemView.setOnLongClickListener(v -> {
            Context ctx = v.getContext();

            new AlertDialog.Builder(ctx)
                    .setTitle("Remover medicamento")
                    .setMessage("Deseja remover \"" + finalName + "\"?\nEssa ação não pode ser desfeita.")
                    .setPositiveButton("Remover", (dialog, which) -> {
                        int posAtual = holder.getAdapterPosition();
                        if (posAtual != RecyclerView.NO_POSITION) {
                            medications.remove(posAtual);
                            // salva lista atualizada
                            MedicationStorage.saveMedications(ctx, medications);
                            notifyItemRemoved(posAtual);
                            notifyItemRangeChanged(posAtual, getItemCount() - posAtual);
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true; // consumiu o long click
        });
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    static class MedViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        TextView txtDose;
        TextView txtFrequency;
        TextView txtNextDate;

        MedViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtMedName);
            txtDose = itemView.findViewById(R.id.txtMedDose);
            txtFrequency = itemView.findViewById(R.id.txtMedFrequency);
            txtNextDate = itemView.findViewById(R.id.txtMedNextDate);
        }
    }
}
