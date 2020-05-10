package com.example.flat;

import android.content.DialogInterface;
import android.os.Bundle;

import com.andremion.counterfab.CounterFab;
import com.example.flat.Model.Block;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.CheckBox;

public class Home extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference blocks;

    TextView txtFullName;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    CounterFab btnAddBlock;

    MaterialEditText edtBlockName, edtOwnerName, edtMainAmt, edtOwnerContact, edtRenter, edtRenterContact;
    CheckBox ckbInUse, ckbOnRent;

    Block newBlock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Block");
        setSupportActionBar(toolbar);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        blocks = database.getReference("Block");

        btnAddBlock = (CounterFab)findViewById(R.id.fab);
        btnAddBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBlockDialog();
            }
        });


        drawerLayout = findViewById(R.id.drawer_layout);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.nav_add_block)
                {
                    addBlockDialog();
                }

                return true;
            }
        });


    }

    private void addBlockDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Add new Block");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_block_layout = inflater.inflate(R.layout.add_new_block, null);

        edtBlockName = add_block_layout.findViewById(R.id.edtBlockName);
        edtOwnerName = add_block_layout.findViewById(R.id.edtOwnerName);
        edtMainAmt = add_block_layout.findViewById(R.id.edtMaintainanceAmt);
        edtRenter = add_block_layout.findViewById(R.id.edtRenterName);
        edtOwnerContact = add_block_layout.findViewById(R.id.edtOwnerContact);
        edtRenterContact = add_block_layout.findViewById(R.id.edtRenterContact);

        ckbInUse = add_block_layout.findViewById(R.id.ckbInUse);
        ckbOnRent = add_block_layout.findViewById(R.id.ckbOnRent);

        ckbOnRent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    edtRenter.setVisibility(View.VISIBLE);
                    edtRenterContact.setVisibility(View.VISIBLE);
                } else{
                    edtRenter.setVisibility(View.GONE);
                    edtRenterContact.setVisibility(View.GONE);
                }

            }
        });

//        ckbOnRent


        alertDialog.setView(add_block_layout);
        alertDialog.setIcon(R.drawable.ic_library_add_black_24dp);

        //SET Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //String owner, String amount, String ocontact, String renter, String rcontact, boolean inuse

                newBlock =new Block(edtOwnerName.getText().toString(), edtMainAmt.getText().toString(),
                        edtOwnerContact.getText().toString(),
                        edtRenter.getText().toString(),
                        edtRenterContact.getText().toString(),
                        ckbInUse.isChecked(),
                        ckbOnRent.isChecked());
                //Here, Just new Category
                if(newBlock!=null){

                    blocks.child(edtBlockName.getText().toString()).setValue(newBlock);
//                    blocks.push().setValue(newBlock);
                    Snackbar.make(drawerLayout, "New Block : " + edtBlockName.getText().toString() + " was added", Snackbar.LENGTH_SHORT)
                            .show();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.refresh){
        }

        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }



}
