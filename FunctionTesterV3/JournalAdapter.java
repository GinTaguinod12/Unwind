package com.example.functiontesterfpv3;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder>{
    Context context;
    ArrayList<Journal> list;
    public JournalAdapter(Context context, ArrayList<Journal> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public JournalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.journal_note_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalAdapter.ViewHolder holder, int position) {
        Journal journal = list.get(position);
        holder.txtTitle.setText("Journal: " + journal.getDate());

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ViewJournalPage.class);
            i.putExtra("journalId", journal.getJournalId());
            i.putExtra("date", journal.getDate());
            i.putExtra("content1", journal.getPositiveEvent());
            i.putExtra("content2", journal.getChallenges());
            i.putExtra("content3", journal.getMoodInfluence());
            i.putExtra("content4", journal.getLesson());
            i.putExtra("userId", journal.getUserId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;

        ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }
    }
}
