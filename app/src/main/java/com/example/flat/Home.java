package com.example.flat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.example.flat.Model.Block;
import com.example.flat.Model.Receipt;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;

import android.view.Gravity;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

public class Home extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference receipts;
    DatabaseReference blocks;

    List<Block> blockList = new ArrayList<Block>();

    TextView txtFullName;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;


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
                    Toast.makeText(Home.this, "Generate Receipt Called...", Toast.LENGTH_SHORT).show();
                    showGenerateReceiptDialog();
                }

                return true;
            }
        });


    }

    private void loadPaymentStatusOfBlock(String key) {
//        Toast.makeText(this, ""+key, Toast.LENGTH_SHORT).show();
        receipts = database.getReference("Receipt");
//        receipts.child(key).add

    }

    private void loadBlockList() {
        blocks.orderByChild("name")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                     receipt.setStatus("Pending");
                     receipt.setInuse(block.isInuse());

                     receipts.child(key).push().setValue(receipt);
                 }

//                Toast.makeText(Home.this, ""+key, Toast.LENGTH_SHORT).show();


            }
        });
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
