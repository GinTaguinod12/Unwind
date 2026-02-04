package com.taguinod.unwind;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder> {
    Context context;
    ArrayList<Journal> list;
    dbHelper db; // Database helper for deletion

    public JournalAdapter(Context context, ArrayList<Journal> list) {
        this.context = context;
        this.list = list;
        this.db = new dbHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the specific row design we created earlier
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_journal_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Journal journal = list.get(position);

        // 1. Display Date and a preview of the first reflection
        holder.itemDate.setText(journal.getDate());
        holder.itemSummary.setText(journal.getPositiveEvent());

        // 2. SINGLE CLICK: View/Update the full reflection
        holder.itemView.setOnClickListener(v -> {
            // We use MainActivity (or your ViewPage) to show the 5 questions
            Intent i = new Intent(context, AddJournalPage.class);

            // Put all data into the intent BEFORE starting the activity
            i.putExtra("journalId", journal.getJournalId());
            i.putExtra("date", journal.getDate());
            i.putExtra("ans1", journal.getPositiveEvent());
            i.putExtra("ans2", journal.getChallenges());
            i.putExtra("ans3", journal.getMoodInfluence());
            // Since Q4 and Q5 are combined in 'lesson', we pass it to ans4
            i.putExtra("ans4", journal.getLesson());
            i.putExtra("userId", journal.getUserId());

            context.startActivity(i);
        });

        // 3. LONG PRESS: Confirmation Dialog to Delete
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Entry")
                    .setMessage("Do you want to permanently delete this reflection?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.deleteJournal(journal.getJournalId()); // Remove from SQL
                        list.remove(position); // Remove from List
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemDate, itemSummary;

        ViewHolder(View itemView) {
            super(itemView);
            // Matches the IDs in your item_journal_entry.xml
            itemDate = itemView.findViewById(R.id.itemDate);
            itemSummary = itemView.findViewById(R.id.itemSummary);
        }
    }
}
