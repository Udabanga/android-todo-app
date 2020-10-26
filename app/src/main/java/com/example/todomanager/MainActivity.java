package com.example.todomanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button addButton;
    private DatabaseReference databaseReference;
    RecyclerAdapter adapter;
    ArrayList<Item> todoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = (Button) findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(v);
                adapter.notifyDataSetChanged();

                populateList();
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });


        todoList = new ArrayList<>();
        RecyclerView todoRecyclerView = (RecyclerView) findViewById(R.id.itemList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        todoRecyclerView.setLayoutManager(llm);
        adapter = new RecyclerAdapter();
        todoRecyclerView.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                String itemID = todoList.get(position).getId();
                removeItem(itemID);
                todoList.remove(position);
                adapter.notifyDataSetChanged();
            }
        });


        helper.attachToRecyclerView(todoRecyclerView);
    }

    public void populateList(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("TODO").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        todoList.clear();

                        Log.w("TodoApp", "getUser:onCancelled " + dataSnapshot.toString());
                        Log.w("TodoApp", "count = " + String.valueOf(dataSnapshot.getChildrenCount()) + " values " + dataSnapshot.getKey());
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Item todo = data.getValue(Item.class);
                            todoList.add(todo);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("TodoApp", "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateList();
    }

    private void addItem(View v){
        EditText text = (EditText)findViewById(R.id.inputTaskText);
        String value = text.getText().toString();
        if("".equals(value)){
            Toast.makeText(MainActivity.this,"Enter Task",Toast.LENGTH_LONG).show();
        }
        else {
            databaseReference = FirebaseDatabase.getInstance().getReference("TODO");
            String key = "ITM" + uniqueIDgen();

            Item item = new Item();
            item.setId(key);
            item.setName_TODO(value);
            item.setStatus("Incomplete");

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(key, item.toFirebaseObject());

            databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        Toast.makeText(MainActivity.this,"Task Entered",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        text.getText().clear();
    }

    private void removeItem(String itemID){
        databaseReference= FirebaseDatabase.getInstance().getReference("TODO");
        databaseReference.child(itemID).removeValue();
        Toast.makeText(MainActivity.this,"Task Deleted",Toast.LENGTH_LONG).show();
    }



    public String uniqueIDgen(){
        Calendar calendar= Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMDDyyyy");
        SimpleDateFormat currentTime=new SimpleDateFormat("HHmmss");

        return currentDate.format(calendar.getTime())+currentTime.format(calendar.getTime());
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        if(checked){

        }
        else{

        }
    }



    private class RecyclerAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item, parent, false);
            SimpleItemViewHolder pvh = new SimpleItemViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            SimpleItemViewHolder viewHolder = (SimpleItemViewHolder) holder;
            position = position;
            Item todo = todoList.get(position);
            ((SimpleItemViewHolder) holder).title.setText(todo.getName_TODO());
            viewHolder.id=todo.getId();
            if("Complete".equals(todo.getStatus())){
                ((SimpleItemViewHolder) holder).title.setChecked(true);
            }


        }

        public final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CheckBox title;
            public int position;
            String id;


            public SimpleItemViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                title = (CheckBox) itemView.findViewById(R.id.itemTextView);
            }

            @Override
            public void onClick(View view) {
                if(title.isChecked()){
                    title.setChecked(false);
                    databaseReference= FirebaseDatabase.getInstance().getReference("TODO").child(id);
                    Map<String, Object> update = new HashMap<>();
                    update.put("status", "Incomplete");
                    databaseReference.updateChildren(update);
                }
                else{
                    title.setChecked(true);
                    databaseReference= FirebaseDatabase.getInstance().getReference("TODO").child(id);
                    Map<String, Object> update = new HashMap<>();
                    update.put("status", "Complete");
                    databaseReference.updateChildren(update);
                }
            }
        }

        @Override
        public int getItemCount() {
            return todoList.size();
        }


    }
}


