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

/**
 * Main Activity class for Firebase CRUD operations
 * This app allows users to add, view, update, and delete items in Firebase Realtime Database
 */
public class MainActivity extends AppCompatActivity {

    // UI components
    private ListView listView;        // For displaying list of items
    private Button btnAdd;            // Button to add new items

    // Data storage
    private ArrayList<Item> itemList; // Local list to store items from Firebase
    private ArrayAdapter<Item> adapter; // Adapter to connect itemList with ListView

    // Firebase reference
    private DatabaseReference mDatabase; // Reference to the Firebase database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAdd);

        // Initialize data storage
        itemList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(adapter);

        // Initialize Firebase database reference with specific URL
        mDatabase = FirebaseDatabase.getInstance("https://fir-practical-3bcab-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("items");

        // Set up listener to receive real-time updates from Firebase
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear existing list to prevent duplicates
                itemList.clear();

                // Process each item in the database
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Convert database object to Item class
                    Item item = ds.getValue(Item.class);
                    itemList.add(item);
                }

                // Notify adapter to refresh the ListView with new data
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database access errors
                Toast.makeText(MainActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up button click listener to show add item dialog
        btnAdd.setOnClickListener(view -> showAddDialog());

        // Set up long click listener for list items to show update/delete options
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Item item = itemList.get(position);
            showUpdateDeleteDialog(item);
            return true; // Return true to indicate the long click was handled
        });
    }

    /**
     * Shows a dialog for adding a new item
     * User can enter ID (numbers only) and name
     */
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");

        // Inflate custom dialog layout
        View view = getLayoutInflater().inflate(R.layout.dialog_update, null);
        final EditText inputId = view.findViewById(R.id.inputId);
        final EditText inputName = view.findViewById(R.id.inputName);

        // Restrict ID field to numeric input only
        inputId.setInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setView(view);

        // Set up "Add" button action
        builder.setPositiveButton("Add", (dialog, which) -> {
            String id = inputId.getText().toString().trim();
            String name = inputName.getText().toString().trim();

            // Validate input fields are not empty
            if (!id.isEmpty() && !name.isEmpty()) {
                // Additional validation to ensure ID contains only numbers
                if (id.matches("\\d+")) {
                    addItem(id, name);
                } else {
                    Toast.makeText(MainActivity.this, "ID must contain only numbers", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up "Cancel" button
        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.show();
    }

    /**
     * Adds a new item to Firebase database
     *
     * @param id Unique numeric ID for the item
     * @param name Name or description of the item
     */
    private void addItem(String id, String name) {
        // Create new Item object
        Item newItem = new Item(id, name);

        // Save to Firebase under the specified ID node
        mDatabase.child(id).setValue(newItem)
                .addOnSuccessListener(aVoid ->
                        // Show success message when item is added
                        Toast.makeText(MainActivity.this, "Item added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        // Show failure message if operation fails
                        Toast.makeText(MainActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show());
    }

    /**
     * Shows a dialog with options to update or delete an item
     *
     * @param item The Item object to update or delete
     */
    private void showUpdateDeleteDialog(final Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Option");

        // Define available options
        String[] options = {"Update", "Delete"};

        // Set up click listener for options
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Option 0: Update the item
                showUpdateDialog(item);
            } else {
                // Option 1: Delete the item from Firebase
                mDatabase.child(item.getId()).removeValue();
                // Note: The ValueEventListener will automatically update the UI
            }
        });

        // Show the dialog
        builder.show();
    }

    /**
     * Shows a dialog for updating an existing item
     *
     * @param item The Item object to update
     */
    private void showUpdateDialog(final Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Item");

        // Inflate custom dialog layout
        View view = getLayoutInflater().inflate(R.layout.dialog_update, null);
        final EditText inputId = view.findViewById(R.id.inputId);
        final EditText inputName = view.findViewById(R.id.inputName);

        // Restrict ID field to numeric input only
        inputId.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Pre-fill fields with current item data
        inputId.setText(item.getId());
        inputName.setText(item.getName());

        builder.setView(view);

        // Set up "Update" button action
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newId = inputId.getText().toString().trim();
            String newName = inputName.getText().toString().trim();

            // Validate input fields are not empty
            if (!newId.isEmpty() && !newName.isEmpty()) {
                // Additional validation to ensure ID contains only numbers
                if (newId.matches("\\d+")) {
                    // If ID has changed, remove the old entry
                    if (!newId.equals(item.getId())) {
                        mDatabase.child(item.getId()).removeValue();
                    }

                    // Create updated item and save to Firebase
                    Item updatedItem = new Item(newId, newName);
                    mDatabase.child(newId).setValue(updatedItem);
                    // Note: The ValueEventListener will automatically update the UI
                } else {
                    Toast.makeText(MainActivity.this, "ID must contain only numbers", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up "Cancel" button
        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.show();
    }
}