package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    Button add;
    AlertDialog dialog;
    LinearLayout layout;
    EditText searchEditText;

    // New fields for editing dialog
    AlertDialog editDialog;
    EditText editName, editDueDate;
    Spinner editPrioritySpinner;
    View editingCard;

    // Fields for SharedPreferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        add = findViewById(R.id.add);
        layout = findViewById(R.id.container);
        searchEditText = findViewById(R.id.searchEditText);

        sharedPreferences = getSharedPreferences("ToDoList", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        buildDialog();
        buildEditDialog();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveTasks();
    }

    public void buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog, null);

        final EditText name = view.findViewById(R.id.nameEdit);
        final EditText dueDate = view.findViewById(R.id.dueDateEdit);
        final Spinner prioritySpinner = view.findViewById(R.id.prioritySpinner);

        dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(dueDate);
            }
        });

        builder.setView(view);
        builder.setTitle("Enter your Task")
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addCard(name.getText().toString(), dueDate.getText().toString(), prioritySpinner.getSelectedItem().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        dialog = builder.create();
    }

    private void buildEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog, null);
        editName = view.findViewById(R.id.nameEdit);
        editDueDate = view.findViewById(R.id.dueDateEdit);
        editPrioritySpinner = view.findViewById(R.id.prioritySpinner);

        editDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(editDueDate);
            }
        });

        builder.setView(view);
        builder.setTitle("Edit your Task")
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((TextView) editingCard.findViewById(R.id.name)).setText(editName.getText().toString());
                        ((TextView) editingCard.findViewById(R.id.dueDate)).setText(editDueDate.getText().toString());
                        ((TextView) editingCard.findViewById(R.id.priority)).setText(editPrioritySpinner.getSelectedItem().toString());
                    }
                })
                .setNegativeButton("Cancel", null);

        editDialog = builder.create();
    }

    private void saveTasks() {
        editor.clear();
        int count = layout.getChildCount();
        editor.putInt("count", count);
        for (int i = 0; i < count; i++) {
            View card = layout.getChildAt(i);
            TextView nameView = card.findViewById(R.id.name);
            TextView dueDateView = card.findViewById(R.id.dueDate);
            TextView priorityView = card.findViewById(R.id.priority);
            CheckBox checkbox = card.findViewById(R.id.checkbox);
            editor.putString("task_" + i, nameView.getText().toString());
            editor.putString("due_date_" + i, dueDateView.getText().toString());
            editor.putString("priority_" + i, priorityView.getText().toString());
            editor.putBoolean("task_done_" + i, checkbox.isChecked());
        }
        editor.apply();
    }

    private void loadTasks() {
        int count = sharedPreferences.getInt("count", 0);
        for (int i = 0; i < count; i++) {
            String task = sharedPreferences.getString("task_" + i, "");
            String dueDate = sharedPreferences.getString("due_date_" + i, "");
            String priority = sharedPreferences.getString("priority_" + i, "");
            boolean done = sharedPreferences.getBoolean("task_done_" + i, false);
            addCard(task, dueDate, priority, done);
        }
    }

    private void addCard(String name) {
        addCard(name, "", "Low", false);
    }

    private void addCard(String name, String dueDate, String priority) {
        addCard(name, dueDate, priority, false);
    }

    private void addCard(String name, String dueDate, String priority, boolean done) {
        final View view = getLayoutInflater().inflate(R.layout.card, null);

        TextView nameView = view.findViewById(R.id.name);
        TextView dueDateView = view.findViewById(R.id.dueDate);
        TextView priorityView = view.findViewById(R.id.priority);
        Button delete = view.findViewById(R.id.delete);
        Button edit = view.findViewById(R.id.edit);
        CheckBox checkbox = view.findViewById(R.id.checkbox);
        nameView.setText(name);
        dueDateView.setText(dueDate);
        priorityView.setText(priority);
        checkbox.setChecked(done);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.removeView(view);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editingCard = view;
                editName.setText(nameView.getText().toString());
                editDueDate.setText(dueDateView.getText().toString());
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.priority_array, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                editPrioritySpinner.setAdapter(adapter);
                if (priorityView.getText().toString() != null) {
                    int spinnerPosition = adapter.getPosition(priorityView.getText().toString());
                    editPrioritySpinner.setSelection(spinnerPosition);
                }
                editDialog.show();
            }
        });

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    nameView.setPaintFlags(nameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    nameView.setPaintFlags(nameView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }
        });

        layout.addView(view);
    }

    private void showDatePickerDialog(final EditText dateEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                dateEditText.setText(selectedDate);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void filterTasks(String text) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View card = layout.getChildAt(i);
            TextView nameView = card.findViewById(R.id.name);
            if (nameView.getText().toString().toLowerCase().contains(text.toLowerCase())) {
                card.setVisibility(View.VISIBLE);
            } else {
                card.setVisibility(View.GONE);
            }
        }
    }
}
