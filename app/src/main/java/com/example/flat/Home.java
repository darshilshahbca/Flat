package com.example.flat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import com.example.flat.Common.Common;
import com.example.flat.Interface.ItemClickListener;
import com.example.flat.Model.Block;
import com.example.flat.Model.Expense;
import com.example.flat.Model.Receipt;
import com.example.flat.ViewHolder.ReceiptViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;

import android.telephony.SmsManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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

    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    FirebaseDatabase database;
    DatabaseReference receipts, receipts_slot, expenses;
    DatabaseReference blocks;
    int finalTotal = 0;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    List<Block> blockList = new ArrayList<Block>();

    MaterialSpinner spinner, spinnerPayment;

    TextView txtFullName, txtTotalAmount, txtFlatAmount;

    Expense expense;

    //String Key
    String key = null;

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

        //Firebase
        database = FirebaseDatabase.getInstance();
        receipts = database.getReference("Receipt");

        //Layout Binding
        txtTotalAmount = findViewById(R.id.txtMaintainaceTotal);
        txtFlatAmount = findViewById(R.id.additionalTotal);
        recyclerView = findViewById(R.id.recycler_block_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        //Set-Up Calendar
        /* starts before 6 month from now */
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -6);

        /* ends after 6 month from now */
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 6);

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
                int monthSelected = date.get(Calendar.MONTH) + 1;
                int yearSelected = date.get(Calendar.YEAR);
                loadPaymentStatusOfBlock(Common.getKeyFormat(monthSelected, yearSelected));
            }
        });

        //Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        //load Block List
        loadBlockList();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.nav_block) {
                    Intent viewBlock = new Intent(Home.this, ViewBlock.class);
                    startActivity(viewBlock);
                } else if (menuItem.getItemId() == R.id.nav_receipt) {
                    showGenerateReceiptDialog();
                } else if (menuItem.getItemId() == R.id.nav_expense_manage) {
                    Intent expenseManage = new Intent(Home.this, ExpenseManage.class);
                    startActivity(expenseManage);
                }

                return true;
            }
        });

        //Get Current Month & Year to Load Default Receipts
        loadPaymentStatusOfBlock(Common.getKeyFormat(Common.getCurrentMonth() + 1, Common.getCurrentYear()));
    }

    private void loadPaymentStatusOfBlock(final String key) {

        //Clear Text Amounts on Holiday Calendar Change
        txtTotalAmount.setText("0");
        txtFlatAmount.setText("0");

        //Load Receipts Slots
        receipts_slot = database.getReference("Receipt").child(key);
        FirebaseRecyclerOptions<Receipt> options =
                new FirebaseRecyclerOptions.Builder<Receipt>()
                        .setQuery(receipts_slot.orderByChild("name"), Receipt.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Receipt, ReceiptViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final ReceiptViewHolder viewHolder, final int position, @NonNull final Receipt model) {
                viewHolder.txtBlockSlot.setText(model.getName());
                viewHolder.txtStatus.setText(Common.convertCodeToStatus2(model.getStatus()));

                //Slot Color Change based on Pending/Received/Confirm
                if (model.getStatus().equals("0")) {
                    viewHolder.card_block_slot.setCardBackgroundColor(getResources().getColor(android.R.color.white));
                } else if (model.getStatus().equals("1")) {
                    viewHolder.card_block_slot.setCardBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
                } else if (model.getStatus().equals("2")) {
                    viewHolder.card_block_slot.setCardBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                }

                //Slot Color Change in case Not in Use
                if (!model.isInuse()) {
                    viewHolder.card_block_slot.setCardBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    viewHolder.txtStatus.setText("Close");
                }

                if (position == adapter.getItemCount() - 1) {
                    int total = 0, flatTotal = 0;
                    for (int i = 0; i < adapter.getItemCount(); i++) {
                        if (adapter.getItem(i).isInuse() && (adapter.getItem(i).getStatus().equals("2"))) {
                            total = total + adapter.getItem(i).getAmount();
                            flatTotal = flatTotal + adapter.getItem(i).getFlatamount();
                        }
                    }

                    txtTotalAmount.setText(String.valueOf(total));
                    txtFlatAmount.setText(String.valueOf(flatTotal));
                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        showReceiptUpdateDialog(adapter.getRef(position).getKey(), adapter.getItem(position), key);
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
        adapter.notifyDataSetChanged();
    }

    private void showReceiptUpdateDialog(final String key, final Receipt item, final String keyMonth) {
        final AlertDialog.Builder alertDialog =
                new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Payment Received From");
        alertDialog.setMessage(String.format(item.getReceipt_name()) + " (" + String.format("34/" + item.getName()) + ")");


        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.block_update_layout, null);

        spinner = view.findViewById(R.id.statusSpinner);
        spinner.setItems("Pending", "Received", "Confirm");

        spinnerPayment = view.findViewById(R.id.statusPayment);
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

                String totalAmount = String.valueOf(item.getAmount() + item.getFlatamount());

                if (item.getStatus().toString().equals("2")) {
                    //Get Current Date
                    Date currentDate = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                    String formattedDate = df.format(currentDate);
//                    SMS Manager
                    if (checkPermission(Manifest.permission.SEND_SMS)) {
                        onSend(item.getReceipt_number(), totalAmount, item.getName(), formattedDate, keyMonth);
                    } else {
                        ActivityCompat.requestPermissions(Home.this,
                                new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
                    }
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
    }

    private void loadBlockList() {

        blocks = database.getReference("Block");

        blocks.orderByChild("name")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        blockList.clear();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
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

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(Common.getCurrentMonth(), Common.getCurrentYear());

        dialogFragment.show(getSupportFragmentManager(), null);

        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {

                final String keyGeneration = Common.getKeyFormat(monthOfYear + 1, year);

                for (Block block : blockList) {
                    Receipt receipt = new Receipt();
                    receipt.setName(block.getName());
                    receipt.setReceipt_name(block.getOwner());
                    receipt.setStatus("0");
                    receipt.setPayment("0");
                    receipt.setAmount(Integer.parseInt(block.getAmount()) - 100);
                    receipt.setReceipt_number(block.getOcontact());
                    if (Integer.parseInt(block.getAmount()) - 400 > 0)
                        receipt.setFlatamount(Integer.parseInt(block.getAmount()) - 400);
                    else
                        receipt.setFlatamount(100);

                    receipt.setInuse(block.isInuse());

                    receipts.child(keyGeneration).push().setValue(receipt);
                }

//                Toast.makeText(Home.this, ""+key, Toast.LENGTH_SHORT).show();


            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {
//            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
            showUpdateReceiptDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
            Log.d("Adapter", String.valueOf(adapter.getRef(item.getOrder()).getKey()));
            deleteReceipt(adapter.getRef(item.getOrder()).getKey());
            loadPaymentStatusOfBlock(key);
        }

        return super.onContextItemSelected(item);
    }

    private void showUpdateReceiptDialog(String key, final Receipt item) {
        final AlertDialog.Builder alertDialog =
                new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Receipt Detail");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.receipt_update_layout, null);

        edtReceiptOwnerName = view.findViewById(R.id.edtReceiptOwnerName);
        edtReceiptBlockName = view.findViewById(R.id.edtReceiptBlockName);
        edtReceiptAmt = view.findViewById(R.id.edtReceiptAmt);
        edtReceiptFlatAmt = view.findViewById(R.id.edtReceiptFlatAmt);
        ckbReceiptInUse = view.findViewById(R.id.ckbReceiptInUse);

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
//        Log.d("Item", String.valueOf(item));

        adapter.notifyDataSetChanged();

//        adapter.notifyItemRemoved(1);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
        }

        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public void onSend(String receipt_number, String totalAmount, String name, String formattedDate, String keyMonth) {
        String phoneNumber = receipt_number;

        String month = keyMonth.substring(0, 2);
        String year = keyMonth.substring(2, 6);

        String smsMessage = "Maintainance amount of Rs."
                + totalAmount
                + " (Block: 34/" + name + ") received on " + formattedDate +
                " for " + Common.convertCodeToMonth(month) + "," + year;

        if (phoneNumber == null || phoneNumber.length() == 0 ||
                smsMessage == null || smsMessage.length() == 0) {
            return;
        }

        if (checkPermission(Manifest.permission.SEND_SMS)) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, smsMessage, null, null);
            Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


}
