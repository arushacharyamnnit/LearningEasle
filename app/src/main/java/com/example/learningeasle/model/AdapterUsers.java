package com.example.learningeasle.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learningeasle.R;
import com.example.learningeasle.UserDetails.UserProfile;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.UserHolder>{
   Context context;
   List<ModelUsers> userList;
   String Url = null;
    public AdapterUsers(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_user,parent,false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserHolder holder, int position) {
          final String userName = userList.get(position).getName();
          final String email = userList.get(position).getEmail();
          final String url = userList.get(position).getUrl();
          final String Uid = userList.get(position).getId();
          final String curruid = FirebaseAuth.getInstance().getCurrentUser().getUid();
          holder.name.setText(userName);
          holder.email.setText(email);


        //Initialise Shimmer
        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(Color.parseColor("#F3F3F3"))
                .setBaseAlpha(1)
                .setHighlightColor(Color.parseColor("#E7E7E7"))
                .setHighlightAlpha(1)
                .setDropoff(50)
                .build();
        //Initialise shimmer Drawable
        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);


//          System.out.println(userName+" jsr "+email);
        //If current user is admin make follow btn invisible
         DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("admin").child("Id");
         databaseReference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if(snapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                     holder.follow.setVisibility(View.GONE);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
         //Set user profile
         if(url.equals("empty"))
           holder.profile.setImageResource(R.drawable.ic_action_account);
         else
           Picasso.get().load(url).placeholder(shimmerDrawable).into(holder.profile);
         //Setting the follower i.e if current user is following the user of holder or not
          setFollower(holder,curruid,Uid);
          //Following and unfollowing the user from the holder view
           holder.follow.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   final DatabaseReference reffollowing = FirebaseDatabase.getInstance().getReference("Users")
                           .child(curruid);
                   final DatabaseReference reffollowers = FirebaseDatabase.getInstance().getReference("Users")
                           .child(Uid);
                   reffollowing.addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                           if(snapshot.child("Following").hasChild(Uid)){
                               reffollowing.child("Following").child(Uid).removeValue();
                               reffollowers.child("Followers").child(curruid).removeValue();
                           }else{
                               reffollowing.child("Following").child(Uid).setValue(Uid);
                               reffollowers.child("Followers").child(curruid).setValue(curruid);
                           }
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError error) {

                       }
                   });
               }
           });

          //When an item of holder is clicked show the profile of the clicked user to current user
          holder.itemView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(context, UserProfile.class);
                  intent.putExtra("Id",Uid);
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  context.startActivity(intent);
              }
          });
    }

    //Set followers of the user
    private void setFollower(final UserHolder holder, final String curruid, final String uid) {
        final DatabaseReference reffollowing = FirebaseDatabase.getInstance().getReference("Users")
                .child(curruid);
        reffollowing.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("Following").hasChild(uid)){
                    holder.follow.setText("Following");
                    holder.follow.setBackgroundResource(R.drawable.button_unfollow);
//                    holder.follow.setBackgroundColor(Color.rgb(133,204,236));
                }else{
                    holder.follow.setText("Follow");
                    holder.follow.setBackgroundResource(R.drawable.button_follow);
//                    holder.follow.setBackgroundColor(Color.rgb(84,104,213));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserHolder extends RecyclerView.ViewHolder {


        ImageView profile;
        TextView name,email;
        Button follow;
        public UserHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            follow = itemView.findViewById(R.id.follow);

        }
    }

}
