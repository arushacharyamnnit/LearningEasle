package com.example.learningeasle.MainFragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.learningeasle.MainActivity;
import com.example.learningeasle.R;
import com.example.learningeasle.ViewAttachement;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

//import net.dankito.richtexteditor.android.RichTextEditor;
//import net.dankito.richtexteditor.android.toolbar.AllCommandsEditorToolbar;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class PostFragment extends Fragment implements View.OnClickListener {

    View view;
    UploadTask uploadTask;
     EditText et_title, et_desc;
    FloatingActionButton post_btn,video_btn,view_attached,pdf_btn,audio_btn,view_btn;
    ImageView img_post;
    String pName, url="empty";
    Uri image_rui = null,videouri=null,pdfuri=null,audiouri=null;
    ProgressDialog pd;
    ProgressBar progressBar;
    String edit,id,time,title,des,image,email;
    String pLikes="0",pComments="0";
    Spinner spinner;
    String videourl="empty",pdfUrl="empty",audiourrl="empty";
    ArrayAdapter<String> adapter;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    StorageReference reference;
    String[] cameraPermissions;
    String[] storagePermissions;
    ArrayList<String> interests;
    ProgressDialog progressDialog;
    private int PDF_REQUEST = 1004;
    private int AUDIO_REQUEST=104;
    boolean visible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        edit = getArguments().getString("Edit");
        if(edit.equals("EditPost")) {
            id = getArguments().getString("Id");
            time = getArguments().getString("pTime");
            title = getArguments().getString("Title");
            des = getArguments().getString("Des");
            image = getArguments().getString("Url");
            pLikes = getArguments().getString("Likes");
            pComments = getArguments().getString("Comments");
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        et_title = view.findViewById(R.id.title);
        et_desc = view.findViewById(R.id.desc);
        post_btn = view.findViewById(R.id.post_button);
        img_post = view.findViewById(R.id.img);
        progressBar = view.findViewById(R.id.progressBar_addPost);
        video_btn = view.findViewById(R.id.video_upload);
        view_attached = view.findViewById(R.id.view_attached);
        pdf_btn = view.findViewById(R.id.pdf_upload);
        view_btn = view.findViewById(R.id.view);
        audio_btn = view.findViewById(R.id.audio_upload);

        //When attached button is clicked make visiblity of video n pdf button visible

        view_attached.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If views are visible make them invisible and vice-versa
                if(!visible) {
                    video_btn.setVisibility(View.VISIBLE);
                    pdf_btn.setVisibility(View.VISIBLE);
                    view_btn.setVisibility(View.VISIBLE);
                    audio_btn.setVisibility(View.VISIBLE);
                    visible = true;
                }else{
                    video_btn.setVisibility(View.INVISIBLE);
                    pdf_btn.setVisibility(View.INVISIBLE);
                    view_btn.setVisibility(View.INVISIBLE);
                    audio_btn.setVisibility(View.INVISIBLE);
                    visible = false;
                }
            }
        });
        //pdfbtn is clicked pdf attachment work started
        pdf_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent,PDF_REQUEST);
            }
        });
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.
                WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.
                WRITE_EXTERNAL_STORAGE};
        interests=new ArrayList<>();

        adapter= new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1);

        reference = FirebaseStorage.getInstance().getReference();
        spinner = (Spinner) view.findViewById(R.id.spinner);

        view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent view_page = new Intent(getActivity(), ViewAttachement.class);
                if(videouri==null){
                    view_page.putExtra("videourl","empty");
                }else{
                    view_page.putExtra("videourl",videouri.toString());
                }
                if(pdfuri==null){
                    view_page.putExtra("pdfurl","empty");
                }
                else{
                    view_page.putExtra("pdfurl",pdfuri.toString());
                }

                if(audiouri==null){
                    view_page.putExtra("audiourl","empty");
                }else{
                    view_page.putExtra("audiourl",audiouri.toString());
                }
                startActivity(view_page);
            }
        });

        getUserDetails();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo(v);
            }
        });
        if(edit.equals("EditPost")){
            et_title.setText(title);
            et_desc.setText(des);
            if(!image.equals("noImage"))
                Picasso.get().load(image).into(img_post);
        }
//        imgclick(view);
//        postclick(view);
        //audio_btn is clicked upload audio file
        audio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,AUDIO_REQUEST);
            }
        });
        trigger();

        // Inflate the layout for this fragment


    }

    private void chooseVideo(View v) {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,108);

    }

    public void getUserDetails() {
        final String pId = FirebaseAuth.getInstance().getCurrentUser().getUid();



        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot db:snapshot.getChildren()) {
                    HashMap<Object, String> hashMap = (HashMap<Object, String>) db.getValue();
                    if (hashMap.get("Id").equals(pId)) {
                        pName = hashMap.get("Name");
                        url = hashMap.get("Url");
                        email=hashMap.get("email");
                        String folder=email.substring(0,email.length()-4);
                        interests.clear();
                        DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("Users").child(db.getKey()).child(folder);
                        ref1.addValueEventListener(new ValueEventListener() {
                            @Override

                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                                    System.out.println(dataSnapshot.getKey()+"///////////////");
                                    if(dataSnapshot.getValue().equals("1"))
                                        interests.add(dataSnapshot.getKey());
                                }

                                adapter.addAll(interests);
                                spinner.setAdapter(adapter);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                }, 3000);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                }, 3000);

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void trigger() {
        post_btn.setOnClickListener(this);
        img_post.setOnClickListener(this);
    }

    private void Post_Data(final String title, final String description, final String uri) {

//        pd=new ProgressDialog(this);
//        pd.setMessage("Publishing Post..");
//        pd.show();
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        // String UserId = firebaseUser.getUid();
        final String timeStamp;
        //If post is to be edited then timestamp is going to be the time stamp of the post to be edited not the current timestamp
        if(edit.equals("EditPost"))
            timeStamp = time;
        else
            timeStamp= String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + timeStamp;
        final String type=spinner.getSelectedItem().toString();





        //If post contains the image
        if (!uri.equals("noImage")) {
            // with image
            //If imageuri is null which means that the post image is not changed
            if(image_rui==null){
                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Views");
                databaseReference.child(timeStamp).setValue("0");
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(timeStamp);
                ref.child("pTitle").setValue(title);
                ref.child("pDesc").setValue(description);
                ref.child("type").setValue(type);
//                ref.child("views").setValue("0");
                Toast.makeText(getActivity(), "Post Edited!", Toast.LENGTH_SHORT)
                        .show();
                et_desc.setText("");
                et_title.setText("");
                img_post.setImageURI(null);
                image_rui = null;
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }else {
                //otherwise we have added a image either from the gallery or from the camera
                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();


                        if (uriTask.isSuccessful()) {
                            //If edit post then in any scenerio we can either change image type title and desc nothing else than that
                            if (edit.equals("EditPost")) {
                                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Views");
                                databaseReference.child(timeStamp).setValue("0");
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(timeStamp);
                                ref.child("pTitle").setValue(title);
                                ref.child("pDesc").setValue(description);
                                ref.child("type").setValue(type);
                                ref.child("pImage").setValue(downloadUri);
//                                ref.child("views").setValue("0");
                                Toast.makeText(getActivity(), "Post Edited!", Toast.LENGTH_SHORT)
                                        .show();
                                et_desc.setText("");
                                et_title.setText("");
                                img_post.setImageURI(null);
                                image_rui = null;
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                //Otherwise we are publishing the post
                                HashMap<Object, Object> hashMap = new HashMap<>();
                                hashMap.put("pId", firebaseUser.getUid());
                                hashMap.put("pImage", downloadUri);
                                hashMap.put("pTitle", title);
                                hashMap.put("pDesc", description);
                                hashMap.put("pTime", timeStamp);
                                hashMap.put("pName", pName);
                                hashMap.put("url", url);
                                hashMap.put("pLikes", "0");
                                hashMap.put("pComments", "0");
                                hashMap.put("type", type);
                                hashMap.put("videourl",videourl);
                                hashMap.put("pdfurl",pdfUrl);
                                hashMap.put("audiourl",audiourrl);
                                hashMap.put("order",-System.currentTimeMillis());
//                                hashMap.put("views","0");

                                //Reference of the admin pending post when user post for the first time then it should pass through the admin
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("admin").child("pendingpost");
                                reference.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
//                                pd.dismiss();
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(getActivity(), "Post Passed to Admin!!", Toast.LENGTH_SHORT)
                                                        .show();
                                                et_desc.setText("");
                                                et_title.setText("");
                                                img_post.setImageURI(null);
                                                image_rui = null;

                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                startActivity(intent);


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
//                                pd.dismiss();
                                        progressBar.setVisibility(View.GONE);
//                                Toast.makeText(getApplicationContext(),"Whyyy",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                            pd.dismiss();
                            }
                        });

            }
        } else {
            //If its edit post then we need to set the values of only title content type and image
            if (edit.equals("EditPost")) {
                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Views");
                databaseReference.child(timeStamp).setValue("0");
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(timeStamp);
                ref.child("pTitle").setValue(title);
                ref.child("pDesc").setValue(description);
                ref.child("type").setValue(type);
                ref.child("pImage").setValue("noImage");
//                ref.child("views").setValue("0");
                    Toast.makeText(getActivity(), "Post Edited!", Toast.LENGTH_SHORT)
                            .show();
                et_desc.setText("");
                et_title.setText("");
                img_post.setImageURI(null);
                image_rui = null;
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            } else {
                //otherwise publish the new post
                HashMap<Object, Object> hashMap = new HashMap<>();
                hashMap.put("pId", firebaseUser.getUid());
                hashMap.put("pImage", "noImage");
                hashMap.put("pTitle", title);
                hashMap.put("pDesc", description);
                hashMap.put("pTime", timeStamp);
                hashMap.put("pName", pName);
                hashMap.put("url", url);
                hashMap.put("pLikes", "0");
                hashMap.put("pComments", "0");
                hashMap.put("type", type);
                hashMap.put("videourl",videourl);
                hashMap.put("pdfurl",pdfUrl);
                hashMap.put("audiourl",audiourrl);
                hashMap.put("order",-System.currentTimeMillis());
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("admin").child("pendingpost");
                ref.child(timeStamp).setValue(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
//                            pd.dismiss();
                                progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "Post Passed to Admin!", Toast.LENGTH_SHORT)
                                            .show();
                                et_desc.setText("");
                                et_title.setText("");
                                img_post.setImageURI(null);
                                image_rui = null;
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                    pd.dismiss();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Whyyy", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }

    }

    private void Image_dialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    if (!checkCameraPermission())
                        requestCameraPermission();
                    else
                        pickFromCamera();
                }

                if (i == 1) {
                    if (!checkStoragePermission())
                        requestStoragePermission();
                    else
                        pickFromGallery();
                }
            }
        });
        builder.create().show();
       DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users");
       db.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               for(DataSnapshot ds:snapshot.getChildren()){
                   HashMap<String ,Object> hashMap = (HashMap<String, Object>) ds.getValue();
                   if(hashMap.get("Id").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                       pName = (String) hashMap.get("Name");
                       url = (String) hashMap.get("Url");
                   }
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp Desc");
        image_rui = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission() {
        boolean reuslt = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.
                WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return reuslt;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), storagePermissions,
                STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.
                CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.
                WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), cameraPermissions,
                CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted)
                        pickFromCamera();
                    else
                        Toast.makeText(getActivity(), "Camera and Storage permissions are required...",
                                Toast.LENGTH_SHORT).show();
                } else {


                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted)
                        pickFromGallery();
                    else
                        Toast.makeText(getActivity(), "Storage permission is required...",
                                Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == -1) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_rui = data.getData();
                img_post.setImageURI(image_rui);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                img_post.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==108){

                videouri = data.getData();
                uploadVideo(videouri);
            }
        }
        if(resultCode==RESULT_OK){
            if(requestCode==PDF_REQUEST){
                pdfuri = data.getData();
                uploadPdf(pdfuri);
            }
        }
        if(resultCode==RESULT_OK){
            if(requestCode==AUDIO_REQUEST){
                audiouri = data.getData();
                uploadAudio(audiouri);
            }
        }

    }

    private void uploadAudio(Uri audiouri) {
        if(audiouri!=null){
            final String timeStamp;
            //If post is to be edited then timestamp is going to be the time stamp of the post to be edited not the current timestamp
            if (edit.equals("EditPost"))
                timeStamp = time;
            else
                timeStamp = String.valueOf(System.currentTimeMillis());

            final StorageReference des = reference.child("Audio/"+ timeStamp + "/" + "audio."+getExt(audiouri));
            uploadTask = des.putFile(audiouri);
            des.putFile(audiouri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    des.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                             audiourrl= uri.toString();
                            Toast.makeText(getActivity(),"Audio File Attached",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void uploadPdf(Uri pdfuri) {
        if(pdfuri!=null) {
            final String timeStamp;
            //If post is to be edited then timestamp is going to be the time stamp of the post to be edited not the current timestamp
            if (edit.equals("EditPost"))
                timeStamp = time;
            else
                timeStamp = String.valueOf(System.currentTimeMillis());

            final StorageReference des = reference.child("Pdf/"+ timeStamp + "/" + "Pdf."+getExt(pdfuri));
            uploadTask = des.putFile(pdfuri);
            des.putFile(pdfuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    des.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            pdfUrl = uri.toString();
                            Toast.makeText(getActivity(),"Pdf File Attached",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    //defineType;
    private  String getExt(Uri uri){
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    private void uploadVideo(Uri videouri) {
        if(videouri!=null){
            final String timeStamp;
            //If post is to be edited then timestamp is going to be the time stamp of the post to be edited not the current timestamp
            if(edit.equals("EditPost"))
                timeStamp = time;
            else
                timeStamp= String.valueOf(System.currentTimeMillis());
            final StorageReference des = reference.child("Video/"+ timeStamp + "/" + "video."+getExt(videouri));
            uploadTask = des.putFile(videouri);
            des.putFile(videouri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    des.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            videourl = uri.toString();
                            Toast.makeText(getActivity(),"Video File Attached",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img:
                Image_dialog();
                break;
            case R.id.post_button:
                String title = et_title.getText().toString().trim();
                String description = et_desc.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getActivity(), "Enter Title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(getActivity(), "Enter Description...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (image_rui != null) {
                    // post with image
                    Post_Data(title, description, String.valueOf(image_rui));
                } else {
//                     post without image
                    if(edit.equals("EditPost")&&!image.equals("noImage"))
                        Post_Data(title,description,image);
                    else
                    Post_Data(title, description, "noImage");
                }
                break;

        }
    }
}
