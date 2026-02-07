import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mixedfeaturesdraft.Adapters.TaskAdapter;
import com.example.mixedfeaturesdraft.Models.Task;
import com.example.mixedfeaturesdraft.R;
import com.example.mixedfeaturesdraft.Utilities.AddNewTask;
import com.example.mixedfeaturesdraft.Utilities.OnDialogCloseListener;
import com.example.mixedfeaturesdraft.Utilities.RecyclerViewTouchHelper;
import com.example.mixedfeaturesdraft.Utilities.dbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MotivatePage extends AppCompatActivity implements OnDialogCloseListener {

    RecyclerView dailyTaskRV;
    Button addTaskBtn;
    FloatingActionButton returnBtn;
    dbHelper db;
    long userId;
    String name;
    List<Task> taskList = new ArrayList<>();
    TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.motivate_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dailyTaskRV = findViewById(R.id.dailyTaskRV);
        addTaskBtn = findViewById(R.id.addTaskBtn);
        returnBtn = findViewById(R.id.returnBtn);
        db = new dbHelper(this);

        Intent i = getIntent();
        userId = i.getLongExtra("userId", -1);

        adapter = new TaskAdapter(db, MotivatePage.this);

        taskList = db.getAllTasks(userId);
        Collections.reverse(taskList);
        adapter.setTasks(taskList);

        dailyTaskRV.setHasFixedSize(true);
        dailyTaskRV.setLayoutManager(new LinearLayoutManager(this));
        dailyTaskRV.setAdapter(adapter);

        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong("UserId", userId);

                AddNewTask task = AddNewTask.newInstance();
                task.setArguments(bundle);
                task.show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(dailyTaskRV);

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i2 = new Intent(MotivatePage.this, HomePage.class);
                Toast.makeText(MotivatePage.this, "Leaving...", Toast.LENGTH_SHORT).show();
                startActivity(i2);
            }
        });
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        taskList = db.getAllTasks(userId);
        Collections.reverse(taskList);
        adapter.setTasks(taskList);
        adapter.notifyDataSetChanged();
    }
}
