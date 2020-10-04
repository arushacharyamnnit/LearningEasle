package com.example.learningeasle.model;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learningeasle.R;
import com.squareup.picasso.Picasso;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.MyHolder>{
    Context context;
    List<modelpost> postList;

    public AdapterPost(Context context, List<modelpost> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_post,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
//        String uId=postList.get(position).getuId();
//        String uEmail=postList.get(position).getuEmail();
//        String uName=postList.get(position).getuName();
//        String uDp=postList.get(position).getuDp();
        String pTitle=postList.get(position).getpTitle();
        String pDescription=postList.get(position).getpDesc();
        String pImage=postList.get(position).getpImage();
        String pTimeStamp=postList.get(position).getpTime();
        String pId=postList.get(position).getpId();

        Calendar calendar=Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));

        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

//        holder.uName.setText(uName);
        holder.pTime.setText(pTime);
        holder.pTitle.setText(pTitle);
        holder.pDesc.setText(pDescription);

//        try {
////            Picasso.get().load(uDp).placeholder(R.drawable.ic_default).into(holder.uDp);
//        }
//        catch (Exception e)
//        {
//
//        }
        if(pImage.equals("noImage")) {
            holder.pImage.setVisibility(View.GONE);
        }
        else {
            try {
                Picasso.get().load(pImage).placeholder(R.drawable.ic_default).into(holder.pImage);
            } catch (Exception e) {

            }
        }
        holder.morebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"More",Toast.LENGTH_SHORT).show();
            }
        });
        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Like",Toast.LENGTH_SHORT).show();
            }
        });
        holder.comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Comment",Toast.LENGTH_SHORT).show();
            }
        });
        holder.share_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context,"Share",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

     class MyHolder extends RecyclerView.ViewHolder{

         ImageView uDp;
         ImageView pImage;
        TextView uName,pTime,pTitle,pDesc,pTotalLikes;
        ImageButton morebtn;
        Button like_btn,share_btn,comment_btn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
//            uDp=itemView.findViewById(R.id.uDp);
            pImage=itemView.findViewById(R.id.pImage);
//            uName=itemView.findViewById(R.id.uname);
            pTime=itemView.findViewById(R.id.time);
            pTitle=itemView.findViewById(R.id.ptitle);
            pDesc=itemView.findViewById(R.id.pdesc);
            pTotalLikes=itemView.findViewById(R.id.totallikes);
            morebtn=(ImageButton) itemView.findViewById(R.id.more);
            like_btn=itemView.findViewById(R.id.like);
            share_btn=itemView.findViewById(R.id.share);
            comment_btn=itemView.findViewById(R.id.comment);
        }
    }
}