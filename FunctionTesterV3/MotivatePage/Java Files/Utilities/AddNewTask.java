import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mixedfeaturesdraft.Models.Task;
import com.example.mixedfeaturesdraft.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";
    EditText editText;
    Button saveBtn, cancelBtn;
    dbHelper db;
    long userId = -1;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_new_task, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editText = view.findViewById(R.id.editText);
        saveBtn = view.findViewById(R.id.saveBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);

        db = new dbHelper(getActivity());

        boolean isUpdate = false;
        Bundle bundle = getArguments();

        if (bundle != null) {
            userId = bundle.getLong("UserId", -1);
            Log.d(TAG, "Received userId: " + userId);

            if (bundle.containsKey("TaskId")) {
                isUpdate = true;
                String task = bundle.getString("TaskName");
                editText.setText(task);
            }
        }

        saveBtn.setEnabled(userId > 0);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                saveBtn.setEnabled(!charSequence.toString().trim().isEmpty());
                saveBtn.setBackgroundColor(
                        saveBtn.isEnabled() ? Color.GREEN : Color.GRAY
                );
            }
        });
        boolean finalIsUpdate = isUpdate;
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(userId <= 0) {
                    Log.e(TAG, "save Blocked: invalid userid");
                    return; }

                String text = editText.getText().toString();
                if (finalIsUpdate) {
                    db.updateTask(bundle.getInt("TaskId"), text);
                } else {
                    db.insertTask(new Task(text, 0, userId));
                }
                dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    @Override
    public void onDismiss (@NotNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener)activity).onDialogClose(dialog);
        }
    }
}
