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
        Context ctx = holder.itemView.getContext();

        holder.txtName.setText(med.getName());
        holder.txtDose.setText(med.getDose());

        // --- CORREÇÃO: Usando Utils na lista ---
        String locFreq = MedicationUtils.getLocalizedFrequency(ctx, med.getFrequency());
        holder.txtFrequency.setText(locFreq);

        String locNext = MedicationUtils.getLocalizedNextDate(ctx, med);
        // Exibe "Prox: Segunda, 08:00" formatado
        holder.txtNextDate.setText("Prox: " + locNext);
        // ---------------------------------------

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, MedicationDetailsActivity.class);
            intent.putExtra("medName", med.getName());
            ctx.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(ctx)
                    .setTitle(R.string.dialog_remove_med_title)
                    .setMessage(ctx.getString(R.string.dialog_remove_med_message))
                    .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                        medications.remove(position);
                        MedicationStorage.saveMedications(ctx, medications);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                    })
                    .setNegativeButton(R.string.dialog_no, null)
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