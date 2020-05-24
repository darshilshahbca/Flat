package com.example.flat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.flat.Model.Expense;
import com.example.flat.Model.Receipt;
import com.example.flat.ViewHolder.ExpenseViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ExpenseManage extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference receipts,expenses;

    FirebaseRecyclerAdapter<Expense, ExpenseViewHolder> expenseAdapter;

    int collectedAmount = 0, expensetotal = 0, remainedAmount=0;

    TextView txt_collectedAmount, txt_remained_amount;

    RecyclerView recyclerExpense;
    RecyclerView.LayoutManager layoutManagerExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_manage);

        database = FirebaseDatabase.getInstance();
        expenses = database.getReference("Expenses");
        receipts = database.getReference("Receipt");
//        expenses = database.getReference("Expense");

        txt_collectedAmount = (TextView)findViewById(R.id.txt_total_collected);
        txt_remained_amount = (TextView)findViewById(R.id.txt_remained);

        recyclerExpense = (RecyclerView)findViewById(R.id.recycler_expense);
        recyclerExpense.setHasFixedSize(true);
        layoutManagerExpense = new LinearLayoutManager(this);
        recyclerExpense.setLayoutManager(layoutManagerExpense);



        receipts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
//                    Log.d("Snapshot Data : ", String.valueOf(postSnapshot));
                    for(DataSnapshot childSnapshot : postSnapshot.getChildren()){
                        Receipt receipt = childSnapshot.getValue(Receipt.class);
//                        Log.d("Receipt: ", String.valueOf(receipt));
                        if(receipt.getStatus().equals("2"))
                            collectedAmount = collectedAmount + receipt.getFlatamount();

//                        Log.d("Total:", String.valueOf(collectedAmount));
                    }

                }

                txt_collectedAmount.setText(String.format("Rs.%s", String.valueOf(collectedAmount)));
                txt_remained_amount.setText(String.format("Rs.%s", String.valueOf(collectedAmount)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadAllExpenses();





    }

    @Override
    protected void onStart() {
        super.onStart();
        expenseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        expenseAdapter.stopListening();
    }

    private void loadAllExpenses() {

        FirebaseRecyclerOptions<Expense> options =
                new FirebaseRecyclerOptions.Builder<Expense>()
                        .setQuery(expenses.orderByChild("name"), Expense.class)
                        .build();

        Log.d("Options", String.valueOf(options.getSnapshots()));

        expenseAdapter = new FirebaseRecyclerAdapter<Expense, ExpenseViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Expense model) {
                holder.txt_expense_name.setText(model.getName());
                holder.txt_expense_amount.setText(model.getAmount());

                if(position == expenseAdapter.getItemCount()-1) {
                    expensetotal=0;
                    for (int i = 0; i < expenseAdapter.getItemCount(); i++) {
                        expensetotal = expensetotal +  Integer.parseInt(expenseAdapter.getItem(i).getAmount());
                    }

                    remainedAmount = collectedAmount - expensetotal;
                    txt_remained_amount.setText(String.format("Rs.%s", String.valueOf(remainedAmount)));

                }
            }

            @NonNull
            @Override
            public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_layout, parent, false);
                return new ExpenseViewHolder(view);
            }
        };

        expenseAdapter.startListening();
        recyclerExpense.setAdapter(expenseAdapter);
        expenseAdapter.notifyDataSetChanged();
    }

}
