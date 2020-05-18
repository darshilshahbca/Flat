package com.example.flat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.flat.Common.Common;
import com.example.flat.Interface.ItemClickListener;
import com.example.flat.Model.Block;
import com.example.flat.Model.Receipt;
import com.example.flat.ViewHolder.ReceiptViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class Home extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference receipts, receipts_slot;
    DatabaseReference blocks;




    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    List<Block> blockList = new ArrayList<Block>();

    MaterialSpinner spinner, spinnerPayment;

    TextView txtFullName;

    //Receipt Update Layout
    MaterialEditText edtReceiptOwnerName, edtReceiptBlockName, edtReceiptAmt, edtReceiptFlatAmt;
    CheckBox ckbReceiptInUse;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    FirebaseRecyclerAdapter<Receipt, ReceiptViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


//        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("Block");
//        setSupportActionBar(toolbar);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        receipts = database.getReference("Receipt");
        blocks = database.getReference("Block");

        recyclerView = (RecyclerView)findViewById(R.id.recycler_block_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
//        layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);

        //Set-Up Calendar
        /* starts before 1 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -3);

        /* ends after 1 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 3);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .mode(HorizontalCalendar.Mode.MONTHS)
                .configure()
                .formatMiddleText("MMM")
                .formatBottomText("yyyy")
                .showTopText(false)
                .showBottomText(true)
                .textColor(Color.LTGRAY, Color.WHITE)
                .end()
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
//                Toast.makeText(Home.this, ""+date.get(Calendar.MONTH)+""+ date.get(Calendar.YEAR), Toast.LENGTH_SHORT).show();
                int monthSelected = date.get(Calendar.MONTH) + 1;
                loadPaymentStatusOfBlock(String.format(new DecimalFormat("00").format(monthSelected))+new DecimalFormat("0000").format(date.get(Calendar.YEAR)));
            }
        });


        drawerLayout = findViewById(R.id.drawer_layout);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //load Block List
        blockList.clear();
        loadBlockList();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.nav_block)
                {
                    Intent viewBlock = new Intent(Home.this, ViewBlock.class);
                    startActivity(viewBlock);
                } else if (menuItem.getItemId() == R.id.nav_receipt)
                {
//                    Toast.makeText(Home.this, "Generate Receipt Called...", Toast.LENGTH_SHORT).show();
                    showGenerateReceiptDialog();
                }

                return true;
            }
        });


        loadPaymentStatusOfBlock("052020"); //Default month and year Logic pending

    }

    private void loadPaymentStatusOfBlock(String key) {
//        Toast.makeText(this, ""+key, Toast.LENGTH_SHORT).show();
        receipts_slot = database.getReference("Receipt").child(key);

        Query query = database.getReference("Receipt").child(key);
        //Toast.makeText(this, ""+query, Toast.LENGTH_SHORT).show();

        FirebaseRecyclerOptions<Receipt> options =
                new FirebaseRecyclerOptions.Builder<Receipt>()
                        .setQuery(receipts_slot.orderByChild("name"), Receipt.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Receipt, ReceiptViewHolder>(options) {

            Integer totalPaid = 0;
            Integer totalCollected=0;

            @Override
            protected void onBindViewHolder(@NonNull final ReceiptViewHolder viewHolder, final int position, @NonNull final Receipt model) {
                viewHolder.txtBlockSlot.setText(model.getName());
                viewHolder.txtStatus.setText(Common.convertCodeToStatus2(model.getStatus()));

                final Receipt local = model;

                totalPaid = model.getAmount() + totalPaid;
                totalCollected = model.getFlatamount() + totalCollected;

                Toast.makeText(Home.this, "Total: " +totalPaid + " Collected: " + totalCollected, Toast.LENGTH_SHORT).show();


                if(model.getStatus().equals("0")){
                    viewHolder.card_block_slot.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                } else if (model.getStatus().equals("1")){
                    viewHolder.card_block_slot.setCardBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                }

                if(model.isInuse())
                {
                    viewHolder.card_block_slot.setEnabled(true);
                } else{
//                    viewHolder.card_block_slot.setEnabled(false);
                    viewHolder.card_block_slot.setCardBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    viewHolder.txtStatus.setText("Close");
                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Fix Crash
                        showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));
                    }
                });

            }

            @NonNull
            @Override
            public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_block, parent, false);
                return new ReceiptViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

//        receipts.child(key).add

    }

    private void showUpdateDialog(String key, final Receipt item) {
        final AlertDialog.Builder alertDialog =
                new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Payment Received From");
        alertDialog.setMessage(String.format( item.getReceipt_name()) + " (" + String.format("34/" + item.getName()) + ")");


        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.block_update_layout, null);

        spinner = (MaterialSpinner)view.findViewById(R.id.statusSpinner);
        spinner.setItems("Pending", "Paid");

        spinnerPayment = (MaterialSpinner)view.findViewById(R.id.statusPayment);
        spinnerPayment.setItems("Cash", "Online/Paytm");

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                item.setPayment(String.valueOf(spinnerPayment.getSelectedIndex()));

                receipts_slot.child(localKey).setValue(item);
                adapter.notifyDataSetChanged(); //Add to Update Item Size
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
        adapter.notifyDataSetChanged();
    }

    private void loadBlockList() {
        blocks.orderByChild("name")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        blockList.clear();
                        for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
                        {
                            Block item = postSnapshot.getValue(Block.class);
                            blockList.add(item);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showGenerateReceiptDialog() {
        //Close Drawer
        drawerLayout.closeDrawer(Gravity.LEFT);

        //Month-Year Picker
        final int yearSelected;
        int monthSelected;

        //Set default values
        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        monthSelected = calendar.get(Calendar.MONTH);

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected, yearSelected);

        dialogFragment.show(getSupportFragmentManager(), null);

        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                monthOfYear = monthOfYear + 1;
//                Toast.makeText(Home.this, year +""+monthOfYear, Toast.LENGTH_SHORT).show();

                 final String key = String.format(new DecimalFormat("00").format(monthOfYear)+new DecimalFormat("0000").format(year));

                 for(Block block: blockList){
                     Receipt receipt = new Receipt();
                     receipt.setName(block.getName());
                     receipt.setReceipt_name(block.getOwner());
                     receipt.setStatus("0");
                     receipt.setPayment("0");
                     receipt.setAmount(Integer.parseInt(block.getAmount()) - 100);
                     if(Integer.parseInt(block.getAmount()) - 400 > 0)
                         receipt.setFlatamount(Integer.parseInt(block.getAmount()) - 400);
                     else
                         receipt.setFlatamount(100);

                     receipt.setInuse(block.isInuse());

                     receipts.child(key).push().setValue(receipt);
                 }

//                Toast.makeText(Home.this, ""+key, Toast.LENGTH_SHORT).show();


            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE))
        {
//            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
            showUpdateReceiptDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.DELETE))
        {
            deleteReceipt(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void showUpdateReceiptDialog(String key, final Receipt item) {
        final AlertDialog.Builder alertDialog =
                new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Receipt Detail");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.receipt_update_layout, null);

        edtReceiptOwnerName = (MaterialEditText) view.findViewById(R.id.edtReceiptOwnerName);
        edtReceiptBlockName = (MaterialEditText) view.findViewById(R.id.edtReceiptBlockName);
        edtReceiptAmt = (MaterialEditText) view.findViewById(R.id.edtReceiptAmt);
        edtReceiptFlatAmt = (MaterialEditText) view.findViewById(R.id.edtReceiptFlatAmt);
        ckbReceiptInUse = (CheckBox) view.findViewById(R.id.ckbReceiptInUse);

        edtReceiptOwnerName.setText(item.getReceipt_name());
        edtReceiptAmt.setText(String.valueOf(item.getAmount()));
        edtReceiptFlatAmt.setText(String.valueOf(item.getFlatamount()));
        edtReceiptBlockName.setText(item.getName());
        ckbReceiptInUse.setChecked(item.isInuse());

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                item.setReceipt_name(edtReceiptBlockName.getText().toString());
                item.setName(edtReceiptBlockName.getText().toString());
                item.setAmount(Integer.parseInt(edtReceiptAmt.getText().toString()));
                item.setFlatamount(Integer.parseInt(edtReceiptFlatAmt.getText().toString()));
                item.setInuse(ckbReceiptInUse.isChecked());

                receipts_slot.child(localKey).setValue(item);
                adapter.notifyDataSetChanged(); //Add to Update Item Size
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
        adapter.notifyDataSetChanged();
    }

    private void deleteReceipt(String key) {
//        Toast.makeText(this, ""+key, Toast.LENGTH_SHORT).show();
        receipts_slot.child(key).removeValue();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.refresh){
        }

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }



}
