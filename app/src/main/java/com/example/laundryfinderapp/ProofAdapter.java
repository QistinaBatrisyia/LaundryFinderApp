package com.example.laundryfinderapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProofAdapter extends RecyclerView.Adapter<ProofAdapter.VH> {

    private final Context context;
    private final ArrayList<ProofModel> list;

    public ProofAdapter(Context context, ArrayList<ProofModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_proof, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProofModel p = list.get(position);

        h.txtType.setText(p.type);
        h.txtNote.setText(p.note == null || p.note.isEmpty() ? "(No note)" : p.note);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        h.txtDate.setText(sdf.format(new Date(p.createdAt)));

        Glide.with(context).load(p.imageUrl).into(h.imgThumb);

        // VIEW
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ProofViewActivity.class);
            i.putExtra("proofId", p.proofId);
            i.putExtra("imageUrl", p.imageUrl);
            i.putExtra("type", p.type);
            i.putExtra("note", p.note);
            context.startActivity(i);
        });

        // EDIT
        h.btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(context, ProofViewActivity.class);
            i.putExtra("proofId", p.proofId);
            i.putExtra("imageUrl", p.imageUrl);
            i.putExtra("type", p.type);
            i.putExtra("note", p.note);
            context.startActivity(i);
        });

        // DELETE
        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Proof")
                    .setMessage("Are you sure you want to delete this proof?")
                    .setPositiveButton("Delete", (d, w) -> deleteProof(p))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteProof(ProofModel p) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("proofs")
                .child(uid)
                .child(p.proofId)
                .removeValue();

        FirebaseStorage.getInstance()
                .getReference("proofs/" + uid + "/" + p.proofId + ".jpg")
                .delete();

        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView txtType, txtNote, txtDate, btnEdit, btnDelete;

        public VH(@NonNull View v) {
            super(v);
            imgThumb = v.findViewById(R.id.imgThumb);
            txtType = v.findViewById(R.id.txtType);
            txtNote = v.findViewById(R.id.txtNote);
            txtDate = v.findViewById(R.id.txtDate);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}

