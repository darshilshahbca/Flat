package com.example.flat.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flat.Common.Common;
import com.example.flat.Interface.ItemClickListener;
import com.example.flat.R;

public class BlockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        , View.OnCreateContextMenuListener  {

//    public TextView food_name;
//    public ImageView food_image, fav_image, share_image, quick_cart;
    public TextView txt_block_name, txt_owner_contact, txt_inUse, txt_onRent, txt_renter_name, txt_renter_contact,amount;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public BlockViewHolder(@NonNull View itemView) {
        super(itemView);

        txt_block_name = (TextView)itemView.findViewById(R.id.txt_block_name);
        txt_owner_contact = (TextView)itemView.findViewById(R.id.txt_owner_contact);
        txt_inUse = (TextView)itemView.findViewById(R.id.txt_inUse);
        txt_onRent = (TextView)itemView.findViewById(R.id.txt_onRent);
        txt_renter_name = (TextView)itemView.findViewById(R.id.txt_renter_name);
        txt_renter_contact = (TextView)itemView.findViewById(R.id.txt_renter_contact);
        amount = (TextView)itemView.findViewById(R.id.txt_maintenance) ;

        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select the action");
        menu.add(0, 0, getAdapterPosition(), Common.DELETE);
    }
}
