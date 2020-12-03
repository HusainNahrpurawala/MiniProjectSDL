package com.example.movieplayerapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class UploadThumbnailActivity extends AppCompatActivity {

    Uri videoThumbUri;
    String thumbnaiUri;
    ImageView thumbnailImage;
    StorageReference mStorageRefThumbnail;
    DatabaseReference referenceVideos;
    TextView textSelected;
    RadioButton radioButtonLatest, radioButtonPopular, radioButtonNoType, radioButtonSlide;
    StorageTask mStorageTask;
    DatabaseReference updateDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_thumbnail);

        textSelected = findViewById(R.id.textNoThumbnailSelected);
        thumbnailImage = findViewById(R.id.imageView);
        radioButtonLatest = findViewById(R.id.radioLatestMovies);
        radioButtonPopular = findViewById(R.id.radioMostPopularMovies);
        radioButtonNoType = findViewById(R.id.radioNotype);
        radioButtonSlide = findViewById(R.id.radioSlideMovies);
        mStorageRefThumbnail = FirebaseStorage.getInstance().getReference().child("VideoThumbnail");
        referenceVideos = FirebaseDatabase.getInstance().getReference().child("videos");
        String currentUid = getIntent().getExtras().getString("currentUid");
        updateDataRef = FirebaseDatabase.getInstance().getReference("videos").child(currentUid);

        radioButtonNoType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String latestMovies = radioButtonLatest.getText().toString();
                updateDataRef.child("video_type").setValue("");
                updateDataRef.child("video_slide").setValue("");
                Toast.makeText(UploadThumbnailActivity.this, "Selected "+"No Type", Toast.LENGTH_SHORT).show();
            }
        });

        radioButtonLatest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String latestMovies = radioButtonLatest.getText().toString();
                updateDataRef.child("video_type").setValue(latestMovies);
                updateDataRef.child("video_slide").setValue("");
                Toast.makeText(UploadThumbnailActivity.this, "Selected "+latestMovies, Toast.LENGTH_SHORT).show();
            }
        });

        radioButtonPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String popularMovies = radioButtonPopular.getText().toString();
                updateDataRef.child("video_type").setValue(popularMovies);
                updateDataRef.child("video_slide").setValue("");
                Toast.makeText(UploadThumbnailActivity.this, "Selected "+popularMovies, Toast.LENGTH_SHORT).show();
            }
        });

        radioButtonSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String slideMovies = radioButtonSlide.getText().toString();
                updateDataRef.child("video_slide").setValue(slideMovies);

                Toast.makeText(UploadThumbnailActivity.this, "Selected "+slideMovies, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showImageChooser(View view){
        Intent in = new Intent(Intent.ACTION_GET_CONTENT);
        in.setType("image/*");
        startActivityForResult(in,102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 102 && resultCode == RESULT_OK && data.getData() != null){

            videoThumbUri = data.getData();

            try{

                String thumbName = getFileName(videoThumbUri);
                textSelected.setText(thumbName);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),videoThumbUri);
                thumbnailImage.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private  String getFileName(Uri uri){
        String result = null;

        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null,null, null);
            try{
                if(cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }finally {
                cursor.close();
            }
        }
        if(result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1){
                result = result.substring(cut + 1);
            }
        }
        return  result;
    }

    private void uploadFiles(){
        if(videoThumbUri != null){
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("wait Uploading Thumbnail ...");
            progressDialog.show();
            String video_title =  getIntent().getExtras().getString("thumbnailName");

            final StorageReference sRef = mStorageRefThumbnail.child(video_title+ "." + getFileExtension(videoThumbUri));

            sRef.putFile(videoThumbUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            thumbnaiUri = uri.toString();
                            updateDataRef.child("video_thumb").setValue(thumbnaiUri);
                            progressDialog.dismiss();
                            Toast.makeText(UploadThumbnailActivity.this, "files Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadThumbnailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded" + ((int)progress) + "%...");
                }
            });
        }
    }

    public void uploadFilesToFirebase(View view){

        if(textSelected.equals("No Thumbnail Selected")){
            Toast.makeText(this, "Select Image First", Toast.LENGTH_SHORT).show();
        }else{

            if(mStorageTask != null && mStorageTask.isInProgress()){
                Toast.makeText(this, "Upload Files is already in Progress", Toast.LENGTH_SHORT).show();
            }else{
                uploadFiles();
            }
        }
    }

    private String getFileExtension(Uri uri) {

        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
}