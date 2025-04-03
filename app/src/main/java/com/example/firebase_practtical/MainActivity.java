package com.example.firebase_practtical;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnAdd;
    private ArrayList<Item> itemList;
    private ArrayAdapter<Item> adapter;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAdd);
        itemList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance("https://fir-practical-3bcab-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("items");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Item item = ds.getValue(Item.class);
                    itemList.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });

        btnAdd.setOnClickListener(view -> showAddDialog());
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Item item = itemList.get(position);
            showUpdateDeleteDialog(item);
            return true;
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");
        View view = getLayoutInflater().inflate(R.layout.dialog_update, null);
        final EditText inputId = view.findViewById(R.id.inputId);
        final EditText inputName = view.findViewById(R.id.inputName);

        // Set input type to number for ID field
        inputId.setInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String id = inputId.getText().toString().trim();
            String name = inputName.getText().toString().trim();
            if (!id.isEmpty() && !name.isEmpty()) {
                // Additional check to ensure ID contains only numbers
                if (id.matches("\\d+")) {
                    addItem(id, name);
                } else {
                    Toast.makeText(MainActivity.this, "ID must contain only numbers", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addItem(String id, String name) {
        Item newItem = new Item(id, name);
        mDatabase.child(id).setValue(newItem)
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Item added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show());
    }

    private void showUpdateDeleteDialog(final Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Option");
        String[] options = {"Update", "Delete"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showUpdateDialog(item);
            } else {
                mDatabase.child(item.getId()).removeValue();
            }
        });
        builder.show();
    }

    private void showUpdateDialog(final Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Item");
        View view = getLayoutInflater().inflate(R.layout.dialog_update, null);
        final EditText inputId = view.findViewById(R.id.inputId);
        final EditText inputName = view.findViewById(R.id.inputName);

        // Set input type to number for ID field
        inputId.setInputType(InputType.TYPE_CLASS_NUMBER);

        inputId.setText(item.getId());
        inputName.setText(item.getName());
        builder.setView(view);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newId = inputId.getText().toString().trim();
            String newName = inputName.getText().toString().trim();
            if (!newId.isEmpty() && !newName.isEmpty()) {
                if (newId.matches("\\d+")) {
                    if (!newId.equals(item.getId())) {
                        mDatabase.child(item.getId()).removeValue();
                    }
                    Item updatedItem = new Item(newId, newName);
                    mDatabase.child(newId).setValue(updatedItem);
                } else {
                    Toast.makeText(MainActivity.this, "ID must contain only numbers", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}