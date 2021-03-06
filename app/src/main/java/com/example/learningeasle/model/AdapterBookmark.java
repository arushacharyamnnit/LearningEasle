package com.example.learningeasle.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

import com.example.learningeasle.PostDetailActivity;
import com.example.learningeasle.R;
import com.example.learningeasle.UserDetails.UserProfile;
import com.example.learningeasle.ViewAttachement;
import com.example.learningeasle.ViewImage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterBookmark extends RecyclerView.Adapter<AdapterBookmark.MyHolder> {

    Context context;
    List<modelpost> postList;
    DatabaseReference postsref;
    boolean first = true;
    public AdapterBookmark(Context context, List<modelpost> postList) {
        this.context = context;
        this.postList = postList;
        postsref = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public AdapterBookmark.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post_bookmark, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        String uName = postList.get(position).getpName();
        String url = postList.get(position).getuImage();
        final String pTitle = postList.get(position).getpTitle();
        final String pDescription = postList.get(position).getpDesc();
        final String pImage = postList.get(position).getpImage();
        final String pTimeStamp = postList.get(position).getpTime();
        final String pId = postList.get(position).getpId();
        final String pType = postList.get(position).getpType();
        final String videourl = postList.get(position).getVideourl();
         final String pdfurl = postList.get(position).getPdfurl();
         final String audiourl = postList.get(position).getAudiourl();
         //If any of the url is non-empty then make attachen tn visible
        if (!videourl.equals("empty")||!pdfurl.equals("empty")||!audiourl.equals("empty")) {
            holder.attachement.setVisibility(View.VISIBLE);

        }
        //If any of the url is not empty make attached btn visible
        holder.attachement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(first) {
                    if (!videourl.equals("empty")) {
                        holder.video_btn.setVisibility(View.VISIBLE);

                    }
                    if (!pdfurl.equals("empty")) {
                        holder.pdf_btn.setVisibility(View.VISIBLE);
                    }
                    if (!audiourl.equals("empty")) {
                        holder.audio_btn.setVisibility(View.VISIBLE);
                    }
                    first = false;
                }else{
                    if (!videourl.equals("empty")) {
                        holder.video_btn.setVisibility(View.INVISIBLE);

                    }
                    if (!pdfurl.equals("empty")) {
                        holder.pdf_btn.setVisibility(View.INVISIBLE);
                    }
                    if (!audiourl.equals("empty")) {
                        holder.audio_btn.setVisibility(View.INVISIBLE);
                    }
                    first = true;
                }
            }
        });

        //Pass the url of the attached file which user want to view
        holder.video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewAttachement.class);
                intent.putExtra("videourl",videourl);
                intent.putExtra("audiourl","empty");
                intent.putExtra("pdfurl","empty");
                context.startActivity(intent);
            }
        });
        holder.audio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewAttachement.class);
                intent.putExtra("videourl","empty");
                intent.putExtra("audiourl",audiourl);
                intent.putExtra("pdfurl","empty");
                context.startActivity(intent);
            }
        });
        holder.pdf_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewAttachement.class);
                intent.putExtra("videourl","empty");
                intent.putExtra("audiourl","empty");
                intent.putExtra("pdfurl",pdfurl);
                context.startActivity(intent);
            }
        });

        System.out.println(pTitle+" in bookmarks "+pDescription);

        //Set user details on the post
        holder.uName.setText(uName);
        holder.pType.setText(pType);
        //User name is clicked redirect the user to the profile of the user whose name is clicked
        holder.uName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(pId)) {
                    Intent intent = new Intent(context, UserProfile.class);
                    intent.putExtra("Id", pId);
                    context.startActivity(intent);
                } else {
                    /*AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    Fragment myFragment = new ProfileFragment();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.home, myFragment).addToBackStack(null).commit();*/
                    Toast.makeText(context, "Go to Profile to view your profile", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //Set dp of the user who posted this particular post
        if (url.equals("empty"))
            holder.uDp.setImageResource(R.drawable.ic_action_account);
        else
            Picasso.get().load(url).into(holder.uDp);
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));

        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();


        //Set post image
        if (pImage.equals("noImage")) {
            System.out.println(pTitle + "  . " + pDescription);
            holder.pImage.setVisibility(View.GONE);
        } else {
            try {
                holder.pImage.setVisibility(View.VISIBLE);
                Picasso.get().load(pImage).placeholder(R.drawable.ic_default).fit().centerCrop().into(holder.pImage);
            } catch (Exception e) {
            }
        }
        //Setting post details
        holder.pTime.setText(pTime);
        holder.pTitle.setText(pTitle);
        holder.pDesc.setText(pDescription);

        //User want to view the post image
        holder.pImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewImage.class);
                intent.putExtra("image", pImage);
                context.startActivity(intent);

            }
        });
        //When post is unbookmarked remove it from the bookmark
        holder.bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").
                        child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Bookmarks");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //If bookmark section og the user dont have this postId means post is not bookmarked in that case bookmark the post
                        if (snapshot.hasChild(pTimeStamp)) {
                            reference.child(pTimeStamp).removeValue();
                            holder.bookmark.setImageResource(R.drawable.ic_baseline_bookmark_border_24);
                            Toast.makeText(context,"Post removed from Bookmark",Toast.LENGTH_SHORT).show();
                        } else {
                            reference.child(pTimeStamp).setValue(pId);
                            //holder.boookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.bookmarked));
                            holder.bookmark.setImageResource(R.drawable.bookmarked);
                            Toast.makeText(context,"Post Added to Bookmark",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        ImageView uDp,pImage,bookmark;
        TextView uName, pTime, pTitle, pDesc,pType;
        FloatingActionButton attachement,video_btn,audio_btn,pdf_btn;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            uDp = itemView.findViewById(R.id.uDp);
            pImage = itemView.findViewById(R.id.pImage);
            uName = itemView.findViewById(R.id.uname);
            pTime = itemView.findViewById(R.id.time);
            pTitle = itemView.findViewById(R.id.ptitle);
            pDesc = itemView.findViewById(R.id.pdesc);
            pType=itemView.findViewById(R.id.pType);
            attachement = itemView.findViewById(R.id.view_attached);
            video_btn = itemView.findViewById(R.id.video_upload);
            audio_btn = itemView.findViewById(R.id.audio_upload);
            pdf_btn = itemView.findViewById(R.id.pdf_upload);
            bookmark = itemView.findViewById(R.id.bookmarks);

        }
    }
}
