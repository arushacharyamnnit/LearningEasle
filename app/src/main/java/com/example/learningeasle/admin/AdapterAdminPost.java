package com.example.learningeasle.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learningeasle.PostDetailActivity;
import com.example.learningeasle.R;
import com.example.learningeasle.ViewAttachement;
import com.example.learningeasle.model.AdapterPost;
import com.example.learningeasle.model.modelpost;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterAdminPost extends RecyclerView.Adapter<AdapterAdminPost.MyHolder> {
    View view;
    Context context;
    List<modelpost> postList;

    public AdapterAdminPost(Context context, List<modelpost> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adminrowpost, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        String uName = postList.get(position).getpName();
        String pComments = postList.get(position).getpComments();
        String url = postList.get(position).getuImage();
        final String pTitle = postList.get(position).getpTitle();
        final String pDescription = postList.get(position).getpDesc();
        final String pImage = postList.get(position).getpImage();
        final String pTimeStamp = postList.get(position).getpTime();
        final String pId = postList.get(position).getpId();
        final String pType = postList.get(position).getpType();
        String pLikes = postList.get(position).getpLikes();
        final String[] viewsCount = new String[1];
        final String videourl = postList.get(position).getVideourl();
        final String audiourl = postList.get(position).getAudiourl();
        final String pdfurl = postList.get(position).getPdfurl();
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

        //If any of the url is not empty make attached btn visible
       if(!videourl.equals("empty")||!(audiourl.equals("empty"))||!(pdfurl.equals("empty"))){
           holder.attachement.setVisibility(View.VISIBLE);
       }

       //When Attached btn is clicked make all thide floating btn visible whose url is not empty
       holder.attachement.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(!videourl.equals("empty")){
                   holder.video_btn.setVisibility(View.VISIBLE);

               }
               if(!pdfurl.equals("empty")){
                   holder.pdf_btn.setVisibility(View.VISIBLE);
               }
               if(!audiourl.equals("empty")){
                   holder.audio_btn.setVisibility(View.VISIBLE);
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


        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Views");
        databaseReference.child(pTimeStamp).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                viewsCount[0] =snapshot.getValue().toString();
                System.out.println(viewsCount[0]+" idhar");
                int viewsCnt = Integer.parseInt(viewsCount[0]);
                System.out.println(viewsCnt + "= views");
                viewsCnt++;

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Views");
                ref.child(pTimeStamp).setValue(Integer.toString(viewsCnt));
                holder.views.setText(viewsCount[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.uName.setText(uName);
        holder.pType.setText(pType);
        holder.views.setText(viewsCount[0]);
        //Set the dp of the user in the holder udp image view
        if (url.equals("empty"))
            holder.uDp.setImageResource(R.drawable.ic_action_account);
        else
            Picasso.get().load(url).placeholder(shimmerDrawable).into(holder.uDp);

        //Getting the time readable format from the timestamp uploaded onto the firebase
        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));

        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        //Setting the comment count onto the comment textview of the holder
        if (pComments == null)
            pComments = "0";
        holder.pTotalComment.setText(pComments + " Comments");


        //If post image url is not "noImage" then show the post image otherwise make visibility of imageview gone
        if (pImage.equals("noImage")) {
            System.out.println(pTitle + "  . " + pDescription);
            holder.pImage.setVisibility(View.GONE);
        } else {
            try {
                holder.pImage.setVisibility(View.VISIBLE);
                Picasso.get().load(pImage).placeholder(shimmerDrawable).fit().centerCrop().into(holder.pImage);
            } catch (Exception e) {
            }
        }
        //Setting the all the values from the realtime database to the post
        holder.pTime.setText(pTime);
        holder.pTitle.setText(pTitle);
        holder.pDesc.setText(pDescription);
        holder.pTotalLikes.setText(pLikes + " Likes");
        //If viewCommnet btn is clicked show all the comments to the admin
        holder.comment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pTimeStamp);
                context.startActivity(intent);
            }
        });
        //Admin want to delete the post
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Before deleting the post delete it from the bookmarks section of the users;

                AlertDialog.Builder delete = new AlertDialog.Builder(context);
                delete.setTitle("Are You Sure?");
                delete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Before deleting the post delete it from the bookmarks section of the users;
                        deletefromBookmarks(pTimeStamp, pImage, pId, videourl, audiourl, pdfurl);
                    }
                });

                delete.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Close Dialog
                    }
                });
                AlertDialog alert = delete.create();
                alert.show();
                //    Customising buttons for dialog


            }
        });

    }

    private void deletefromBookmarks(final String pTimeStamp, String pImage, String pId, String videourl, String audiourl, String pdfurl) {
        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    String path = ds.getKey();
                    if(ds.child("Bookmarks").hasChild(pTimeStamp)){
                        ref.child(path).child("Bookmarks").child(pTimeStamp).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Remove the post views from the view child
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Views");
        databaseReference.child(pTimeStamp).removeValue();
        //If post contains the attached file then firstly delete them
        if(!videourl.equals("empty")){
            StorageReference videoref = FirebaseStorage.getInstance().getReferenceFromUrl(videourl);
            videoref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context,"Attached Video File Deleted Successfully",Toast.LENGTH_SHORT).show();
                }
            });

        }
        if(!audiourl.equals("empty")){

            StorageReference audioref = FirebaseStorage.getInstance().getReferenceFromUrl(audiourl);
            audioref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context,"Attached Audio File Deleted Successfully",Toast.LENGTH_SHORT).show();
                }
            });
        }
        if(!pdfurl.equals("empty")){
            StorageReference pdfref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfurl);
            pdfref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context,"Attached Pdf File Deleted Successfully",Toast.LENGTH_SHORT).show();
                }
            });
        }
        deletePost(pTimeStamp,pImage,pId);
    }

    private void deletePost(final String pTimeStamp, String pImage, final String pId) {
        //If there is no post image then simply delete the post otherwise first delete it from the storage
        if (pImage.equals("noImage")) {
            final ProgressDialog pd = new ProgressDialog(context);
            pd.setMessage("Deleting....");
            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Posts").child(pTimeStamp);
            if (!reference1.child("pComments").equals("0"))
                reference1.child("Comments").removeValue();
            reference1.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Unable to Delete", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }else{
            final ProgressDialog pd = new ProgressDialog(context);
            pd.setMessage("Deleting....");

            StorageReference picref = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
            picref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds:snapshot.getChildren()){
                                HashMap<String,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                                if(hashMap.get("pTime").equals(pTimeStamp))
                                    ds.getRef().removeValue();
                                Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                            }
                            pd.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(context,"Unable to delete Post",Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //Created my Holder for the adapter
    static class MyHolder extends RecyclerView.ViewHolder {

        ImageView uDp,pImage,delete;
        TextView uName, pTime, pTitle, pDesc, pTotalLikes,pTotalComment,pType,views;
        Button comment_btn;
        FloatingActionButton attachement,video_btn,audio_btn,pdf_btn;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            uDp = itemView.findViewById(R.id.uDp);
            pImage = itemView.findViewById(R.id.pImage);
            uName = itemView.findViewById(R.id.uname);
            pTime = itemView.findViewById(R.id.time);
            pTitle = itemView.findViewById(R.id.ptitle);
            pDesc = itemView.findViewById(R.id.pdesc);
            pTotalLikes = itemView.findViewById(R.id.totallikes);
            comment_btn = itemView.findViewById(R.id.comment);
            pTotalComment=itemView.findViewById(R.id.totalcomments);
            pType=itemView.findViewById(R.id.pType);
            views=itemView.findViewById(R.id.viewCount);
            delete = itemView.findViewById(R.id.delete);
            video_btn = itemView.findViewById(R.id.video_upload);
            audio_btn = itemView.findViewById(R.id.audio_upload);
            pdf_btn = itemView.findViewById(R.id.pdf_upload);
            attachement = itemView.findViewById(R.id.view_attached);

        }


    }
}
