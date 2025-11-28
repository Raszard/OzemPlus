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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new MedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) {
        Medication med = medications.get(position);

        holder.txtName.setText(med.getName());
        holder.txtDose.setText("Dose: " + med.getDose());
        holder.txtFrequency.setText("Freq: " + med.getFrequency());
        holder.txtNextDate.setText("Próxima: " + med.getNextDate());

        holder.itemView.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent intent = new Intent(ctx, MedicationDetailsActivity.class);
            intent.putExtra("medName", med.getName());
            intent.putExtra("medDose", med.getDose());
            intent.putExtra("medFreq", med.getFrequency());
            intent.putExtra("medNextDate", med.getNextDate());
            // Passa o dia da semana
            intent.putExtra("medDayOfWeek", med.getDayOfWeek());
            ctx.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            Context ctx = v.getContext();
            new AlertDialog.Builder(ctx)
                    .setTitle("Remover medicamento")
                    .setMessage("Deseja remover \"" + med.getName() + "\"?")
                    .setPositiveButton("Remover", (dialog, which) -> {
                        medications.remove(position);
                        MedicationStorage.saveMedications(ctx, medications);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() { return medications.size(); }

    static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDose, txtFrequency, txtNextDate;
        MedViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtMedName);
            txtDose = itemView.findViewById(R.id.txtMedDose);
            txtFrequency = itemView.findViewById(R.id.txtMedFrequency);
            txtNextDate = itemView.findViewById(R.id.txtMedNextDate);
        }
    }
}