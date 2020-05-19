package com.example.flat.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flat.Common.Common;
import com.example.flat.Interface.ItemClickListener;
import com.example.flat.Model.Receipt;
import com.example.flat.R;


public class ReceiptViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener  {

    public TextView txtBlockSlot, txtStatus;
    private ItemClickListener itemClickListener;
    public CardView card_block_slot;
//    public int total = 0;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ReceiptViewHolder(@NonNull View itemView) {
        super(itemView);

        card_block_slot = (CardView)itemView.findViewById(R.id.card_block_slot);
        txtBlockSlot = (TextView)itemView.findViewById(R.id.txt_block_slot);
        txtStatus = (TextView)itemView.findViewById(R.id.txt_block_slot_description);

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
        menu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        menu.add(0, v.getId(), getAdapterPosition(), Common.DELETE);
    }


    public int getTotalAmount(Receipt model, int total) {
        total = total + model.getAmount();
        return total;
    }
}
