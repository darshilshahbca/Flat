package com.example.flat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.flat.Common.Common;
import com.example.flat.Interface.ItemClickListener;
import com.example.flat.Model.Block;
import com.example.flat.ViewHolder.BlockViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
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

       if(item.getTitle().equals(Common.DELETE))
        {
            deleteBlock(adapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void deleteBlock(String key) {
        blocks.child(key).removeValue();
        Toast.makeText(this, "Block Deleted Successfully!!", Toast.LENGTH_SHORT).show();
    }
}
