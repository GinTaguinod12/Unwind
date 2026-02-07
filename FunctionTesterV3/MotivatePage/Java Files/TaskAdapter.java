import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mixedfeaturesdraft.Activities.MotivatePage;
import com.example.mixedfeaturesdraft.Models.Task;
import com.example.mixedfeaturesdraft.R;
import com.example.mixedfeaturesdraft.Utilities.AddNewTask;
import com.example.mixedfeaturesdraft.Utilities.dbHelper;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    private List<Task> taskList = new ArrayList<>();
    private MotivatePage activity;
    private dbHelper db;

    public TaskAdapter(dbHelper db, MotivatePage activity) {
        this.activity = activity;
        this.db = db;
    }

    @NonNull
    @Override
    public TaskAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new MyViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.MyViewHolder holder, int position) {

        final Task item = taskList.get(position);
        holder.checkBox.setText(item.getTask());
        holder.checkBox.setChecked(toBoolean(item.getStatus()));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()) {
                    db.updateStatus(item.getTaskId(), 1);
                } else {
                    db.updateStatus(item.getTaskId(), 0);
                }
            }
        });

    }

    private boolean toBoolean(int status) { return status != 0; }

    public Context getContext() { return activity; }

    @Override
    public int getItemCount() { return taskList.size(); }

    public void setTasks(List<Task> taskList) {

        this.taskList = taskList;
        notifyDataSetChanged();

    }

    public void deleteTask(int position) {

        Task item = taskList.get(position);
        db.deleteTask(item.getTaskId());

        taskList.remove(position);
        notifyItemRemoved(position);

    }

    public void editItems(int position) {

        Task item = taskList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("TaskId", item.getTaskId());
        bundle.putString("TaskName", item.getTask());
        bundle.putLong("UserId", item.getUserId());

        AddNewTask task = new AddNewTask();
        task.setArguments(bundle);
        task.show(activity.getSupportFragmentManager(), task.getTag());

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);

        }
    }
}
