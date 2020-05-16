package com.example.flat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.flat.Common.Common;
import com.example.flat.Interface.ItemClickListener;
import com.example.flat.Model.Block;
import com.example.flat.ViewHolder.BlockViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.CheckBox;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ViewBlock extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference blocks;

    //Search Functionality
    FirebaseRecyclerAdapter<Block, BlockViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    FirebaseRecyclerAdapter<Block, BlockViewHolder> adapter;

    //Add/Edit Block
    MaterialEditText edtBlockName, edtOwnerName, edtMainAmt, edtOwnerContact, edtRenter, edtRenterContact;
    CheckBox ckbInUse, ckbOnRent;

    //Add Block
    FloatingActionButton fab;
    Block newBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_block);

        //`Firebase
        database = FirebaseDatabase.getInstance();
        blocks = database.getReference("Block");

        recyclerView = (RecyclerView)findViewById(R.id.recycler_block);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        fab = (FloatingActionButton) findViewById(R.id.fab_block);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBlockDialog();
            }
        });

        loadAllBlocks();

        //Search
        materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Search Block");
        loadSuggest(); //Write function to load suggest from Firebase
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //When User Type their Text, We will change your Suggest List
                List<String> suggest = new ArrayList<>();
                for(String search: suggestList)
                {
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                    {
                        suggest.add(search);
                    }
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When SearchBar is Close
                //Restore Original Suggest Adapter
                if(!enabled){
                    adapter.startListening();
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When Search Finished
                //Show Result of Search Adapter
                   startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });


    }

    private void loadSuggest() {
        blocks.orderByChild("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Block item = postSnapshot.getValue(Block.class);
                    suggestList.add(item.getName()); //Add name of food to suggested list
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadAllBlocks() {
        FirebaseRecyclerOptions<Block> options =
                new FirebaseRecyclerOptions.Builder<Block>()
                        .setQuery(blocks.orderByValue(), Block.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Block, BlockViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BlockViewHolder holder, int position, @NonNull Block model) {
                holder.txt_block_name.setText(String.format("#34/%s (%s)",adapter.getRef(position).getKey(), model.getOwner()));
                holder.txt_owner_contact.setText(String.format("+91-%s", model.getOcontact()));
                holder.txt_inUse.setText(Common.convertCodeToStatus(model.isInuse()));
                holder.txt_onRent.setText(Common.convertCodeToStatus(model.isOnrent()));
                holder.amount.setText(String.format("Rs.%s", model.getAmount()));

                if(model.isOnrent())
                {
                    holder.txt_renter_name.setText(model.getRenter());
                    holder.txt_renter_contact.setText(String.format("+91-%s",model.getRcontact()));
                }else{
                    holder.txt_renter_name.setText("Not Applicable.");
                    holder.txt_renter_contact.setText("Not Applicable.");
                }

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Fix Crash
                    }
                });


            }

            @NonNull
            @Override
            public BlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_layout, parent, false);
                return new BlockViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void startSearch(final CharSequence text) {

        FirebaseRecyclerOptions<Block> options =
                new FirebaseRecyclerOptions.Builder<Block>()
                        .setQuery(blocks.orderByChild("name").equalTo(text.toString()), Block.class) //Compare Name
                        .build();


        searchAdapter = new FirebaseRecyclerAdapter<Block, BlockViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BlockViewHolder holder, int position, @NonNull Block model) {

                holder.txt_block_name.setText(String.format("#34/%s (%s)",model.getName(), model.getOwner()));
                holder.txt_owner_contact.setText(String.format("+91-%s", model.getOcontact()));
                holder.txt_inUse.setText(Common.convertCodeToStatus(model.isInuse()));
                holder.txt_onRent.setText(Common.convertCodeToStatus(model.isOnrent()));
                holder.amount.setText(String.format("Rs.%s", model.getAmount()));

                if(model.isOnrent())
                {
                    holder.txt_renter_name.setText(model.getRenter());
                    holder.txt_renter_contact.setText(String.format("+91-%s",model.getRcontact()));
                }else{
                    holder.txt_renter_name.setText("Not Applicable.");
                    holder.txt_renter_contact.setText("Not Applicable.");
                }

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //To Fix Crash
                    }
                });

            }

            @NonNull
            @Override
            public BlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_layout, parent, false);
                return new BlockViewHolder(view);
            }
        };
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter); //Set Adapter for Recycler View is Search Result
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }
        
       else if(item.getTitle().equals(Common.DELETE))
        {
            deleteBlock(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(final String key, final Block item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ViewBlock.this);
        alertDialog.setTitle("Edit Block");

        LayoutInflater inflater = this.getLayoutInflater();
        View edit_block_layout = inflater.inflate(R.layout.add_new_block, null);

        edtBlockName = edit_block_layout.findViewById(R.id.edtBlockName);
        edtOwnerName = edit_block_layout.findViewById(R.id.edtOwnerName);
        edtMainAmt = edit_block_layout.findViewById(R.id.edtMaintainanceAmt);
        edtRenter = edit_block_layout.findViewById(R.id.edtRenterName);
        edtOwnerContact = edit_block_layout.findViewById(R.id.edtOwnerContact);
        edtRenterContact = edit_block_layout.findViewById(R.id.edtRenterContact);

        ckbInUse = edit_block_layout.findViewById(R.id.ckbInUse);
        ckbOnRent = edit_block_layout.findViewById(R.id.ckbOnRent);

        //Set Default Value for View
        edtOwnerName.setText(item.getOwner());
        edtOwnerContact.setText(item.getOcontact());
        edtMainAmt.setText(item.getAmount());
        edtBlockName.setText(key);
        edtBlockName.setEnabled(false);
        ckbInUse.setChecked(item.isInuse());
        ckbOnRent.setChecked(item.isOnrent());

        if(ckbOnRent.isChecked())
        {
            edtRenter.setVisibility(View.VISIBLE);
            edtRenterContact.setVisibility(View.VISIBLE);
            edtRenter.setText(item.getRenter());
            edtRenterContact.setText(item.getRcontact());
        }

        ckbOnRent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    edtRenter.setVisibility(View.VISIBLE);
                    edtRenterContact.setVisibility(View.VISIBLE);
                    edtRenter.setText(item.getRenter());
                    edtRenterContact.setText(item.getRcontact());
                } else{
                    edtRenter.setVisibility(View.GONE);
                    edtRenterContact.setVisibility(View.GONE);
                    edtRenter.setText("");
                    edtRenterContact.setText("");
                }

            }
        });

        alertDialog.setView(edit_block_layout);
        alertDialog.setIcon(R.drawable.ic_library_add_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                    item.setOwner(edtOwnerName.getText().toString());
                    item.setOcontact(edtOwnerContact.getText().toString());
                    item.setName(edtBlockName.getText().toString());
                    item.setAmount(edtMainAmt.getText().toString());
                    item.setInuse(ckbInUse.isChecked());
                    item.setOnrent(ckbOnRent.isChecked());
                    item.setRenter(edtRenter.getText().toString());
                    item.setRcontact(edtRenterContact.getText().toString());

                    blocks.child(key).setValue(item);
                Toast.makeText(ViewBlock.this, "Block Edited Successfully!", Toast.LENGTH_SHORT).show();

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

    private void deleteBlock(String key) {
        blocks.child(key).removeValue();
        Toast.makeText(this, "Block Deleted Successfully!!", Toast.LENGTH_SHORT).show();
    }

    private void addBlockDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ViewBlock.this);
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

                newBlock =new Block(edtBlockName.getText().toString(), edtOwnerName.getText().toString(), edtMainAmt.getText().toString(),
                        edtOwnerContact.getText().toString(),
                        edtRenter.getText().toString(),
                        edtRenterContact.getText().toString(),
                        ckbInUse.isChecked(),
                        ckbOnRent.isChecked());
                //Here, Just new Category
                if(newBlock!=null){

                    blocks.child(edtBlockName.getText().toString()).setValue(newBlock);

                    Toast.makeText(ViewBlock.this, "New Block : " + edtBlockName.getText().toString() + " was added", Toast.LENGTH_SHORT).show();
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
}
