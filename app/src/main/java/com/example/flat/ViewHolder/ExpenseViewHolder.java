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

public class ExpenseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

//
    public TextView txt_expense_name, txt_expense_amount;
    public ImageView img_delete;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public ExpenseViewHolder(@NonNull View itemView) {
        super(itemView);

        txt_expense_name = itemView.findViewById(R.id.txt_expense_name);
        txt_expense_amount = itemView.findViewById(R.id.txt_expense_amount);
        img_delete = itemView.findViewById(R.id.btnDeleteExpense);

//        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(),false);
    }

}
