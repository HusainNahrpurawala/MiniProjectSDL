package com.example.movieplayerapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.movieplayerapp.Model.VideoUploadDetails;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Uri videoUri;
    TextView text_video_selected;
    String videoCategory;
    String videoTitle;
    String currentUid;
    StorageReference mStorageRef;
    StorageTask mUploadsTask;
    DatabaseReference referenceVideos;
    EditText videoDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_video_selected = findViewById(R.id.textVideoSelected);
        videoDescription = findViewById(R.id.moviesDescription);
        referenceVideos = FirebaseDatabase.getInstance().getReference().child("videos");
        mStorageRef = FirebaseStorage.getInstance().getReference().child("videos");

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        List<String> Categories = new ArrayList<>();
        Categories.add("Action");
        Categories.add("Adventure");
        Categories.add("Sports");
        Categories.add("Romantic");
        Categories.add("Comedy");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        videoCategory = parent.getItemAtPosition(position).toString();
        Toast.makeText(this, "Selected: " + videoCategory, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void openVideoFiles(View view) {
        Intent In = new Intent(Intent.ACTION_GET_CONTENT);
        In.setType("video/*");
        startActivityForResult(In, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101 && resultCode == RESULT_OK && data.getData() != null) {

            videoUri = data.getData();

            String path = null;
            Cursor cursor;
            int columnIndexData;
            String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Video.Media._ID, MediaStore.Video.Thumbnails.DATA};
            final String orderby = MediaStore.Video.Media.DEFAULT_SORT_ORDER;
            cursor = MainActivity.this.getContentResolver().query(videoUri, projection, null, null, orderby);
            columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

            while (cursor.moveToNext()) {
                path = cursor.getString(columnIndexData);
                videoTitle = FilenameUtils.getBaseName(path);
            }
            text_video_selected.setText(videoTitle);
        }
    }

    public void uploadFileToFirebase(View view) {

        if(text_video_selected.equals("No Video Selected")){
            Toast.makeText(this, "Please Select a Video", Toast.LENGTH_SHORT).show();
        }
        else{
            if(mUploadsTask != null && mUploadsTask.isInProgress()){
                Toast.makeText(this, "video Upload is Already in Progress", Toast.LENGTH_SHORT).show();
            }else{
                uploadFiles();
            }
        }
    }

    private void uploadFiles(){

        if(videoUri!=null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("video Uploading...");
            progressDialog.show();
            final StorageReference storageReference = mStorageRef.child(videoTitle);
            mUploadsTask = storageReference.putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String videoURL = uri.toString();

                            VideoUploadDetails videoUploadDetails = new VideoUploadDetails("","","",videoURL, videoTitle,videoDescription.getText().toString(), videoCategory);
                            String uploadID = referenceVideos.push().getKey();
                            referenceVideos.child(uploadID).setValue(videoUploadDetails);
                            currentUid = uploadID;
                            progressDialog.dismiss();

                            if(currentUid.equals(uploadID)){
                                startThumbnailActivity();
                            }
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    progressDialog.setMessage("uploaded "+ ((int)progress) + "%...");
                }
            });
        }else {
            Toast.makeText(this, "No Video Selected to Upload", Toast.LENGTH_SHORT).show();
        }
    }

    public void startThumbnailActivity(){
        Intent in = new Intent(MainActivity.this, UploadThumbnailActivity.class);
        in.putExtra("currentUid", currentUid);
        in.putExtra("thumbnailName", videoTitle);
        startActivity(in);

        Toast.makeText(this, "Video uploaded Successfully... Upload video Thumbnail!!!", Toast.LENGTH_LONG).show();
    }

}