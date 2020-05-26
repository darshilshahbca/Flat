package com.example.flat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flat.Interface.ItemClickListener;
import com.example.flat.Model.Expense;
import com.example.flat.Model.Receipt;
import com.example.flat.ViewHolder.ExpenseViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class ExpenseManage extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference receipts,expenses;

    FirebaseRecyclerAdapter<Expense, ExpenseViewHolder> expenseAdapter;

    int collectedAmount = 0, expensetotal = 0, remainedAmount=0;

    TextView txt_collectedAmount, txt_remained_amount;

    RecyclerView recyclerExpense;
    RecyclerView.LayoutManager layoutManagerExpense;

    //Add Expenses
    FloatingActionButton fab;

    //Expense Add Layout
    MaterialEditText edtExpenseName, edtExpenseAmount;

    Expense expense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_manage);

        database = FirebaseDatabase.getInstance();
        expenses = database.getReference("Expenses");
        receipts = database.getReference("Receipt");
//        expenses = database.getReference("Expense");



        recyclerExpense = findViewById(R.id.recycler_expense);
        recyclerExpense.setHasFixedSize(true);
        layoutManagerExpense = new LinearLayoutManager(this);
        recyclerExpense.setLayoutManager(layoutManagerExpense);

        fab = findViewById(R.id.fab_expense);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpenseDialog();
            }
        });

        showCollectedRemainedAmount();

        loadAllExpenses();





    }

    private void showCollectedRemainedAmount() {

        txt_collectedAmount = findViewById(R.id.txt_total_collected);
        txt_remained_amount = findViewById(R.id.txt_remained);

        receipts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                collectedAmount = 0;
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
                txt_remained_amount.setPaintFlags(txt_remained_amount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addExpenseDialog()  {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ExpenseManage.this);
            alertDialog.setTitle("Add New Expense");

            LayoutInflater inflater = this.getLayoutInflater();
            View add_expense_layout = inflater.inflate(R.layout.expense_add_layout, null);

            edtExpenseName = add_expense_layout.findViewById(R.id.edtExpenseName);
            edtExpenseAmount = add_expense_layout.findViewById(R.id.edtExpenseAmount);


            alertDialog.setView(add_expense_layout);
            alertDialog.setIcon(R.drawable.ic_wb_incandescent_black_24dp);

            //SET Button
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    //String edtExpenseName, edtExpenseAmount
                    expense = new Expense(edtExpenseName.getText().toString(), edtExpenseAmount.getText().toString());

                    //Here, Just new Expense
                    if(expense!=null){

                        expenses.push().setValue(expense);

                        Toast.makeText(ExpenseManage.this, "New Expense : " + edtExpenseName.getText().toString() + " was added", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alertDialog.show();
            expenseAdapter.notifyDataSetChanged();
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
            protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, final int position, @NonNull final Expense model) {
                holder.txt_expense_name.setText(model.getName());
                holder.txt_expense_amount.setText(model.getAmount());

                if(position == expenseAdapter.getItemCount()-1) {
                    expensetotal=0;
                    for (int i = 0; i < expenseAdapter.getItemCount(); i++) {
                        expensetotal = expensetotal +  Integer.parseInt(expenseAdapter.getItem(i).getAmount());
                    }

                    remainedAmount = collectedAmount - expensetotal;
                    txt_remained_amount.setText(String.format("Rs.%s", String.valueOf(remainedAmount)));
                    txt_remained_amount.setPaintFlags(txt_remained_amount.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


                }

                holder.img_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteExpense(expenseAdapter.getRef(position).getKey());
                    }
                });

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //To Fix Crash
                    }
                });
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

    private void deleteExpense(String key) {
        expenses.child(key).removeValue();
        expenseAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Expense Deleted Successfully!!", Toast.LENGTH_SHORT).show();
        showCollectedRemainedAmount();
        loadAllExpenses();
    }

}
