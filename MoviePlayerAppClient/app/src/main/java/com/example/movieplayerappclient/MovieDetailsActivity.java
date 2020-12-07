package com.example.movieplayerappclient;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.movieplayerappclient.Adapter.MovieShowAdapter;
import com.example.movieplayerappclient.Model.GetVideoDetails;
import com.example.movieplayerappclient.Model.MovieItemClickListenerNew;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailsActivity extends AppCompatActivity implements MovieItemClickListenerNew {
    TextView tv_title, tv_description;
    FloatingActionButton play_fab;
    RecyclerView rvCast, recyclerViewSimilarMovies;
    MovieShowAdapter movieShowAdapter;
    DatabaseReference mDataBaseReference;
    List<GetVideoDetails> uploads, actionMovies, sportsMovies, comedyMovies,
            romanticMovies, adventureMovies;
    String current_video_url;
    String current_video_category;
    private ImageView moviesThumbnail, moviesCoverImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        inView();
        similarMoviesRecycler();
        similarMovies();
    }

    private void similarMovies() {
        if (current_video_category.equals("Action")) {
            movieShowAdapter = new MovieShowAdapter(this, actionMovies, this);
            recyclerViewSimilarMovies.setAdapter(movieShowAdapter);
            recyclerViewSimilarMovies.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            movieShowAdapter.notifyDataSetChanged();
        }

        if (current_video_category.equals("Sports")) {
            movieShowAdapter = new MovieShowAdapter(this, sportsMovies, this);
            recyclerViewSimilarMovies.setAdapter(movieShowAdapter);
            recyclerViewSimilarMovies.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            movieShowAdapter.notifyDataSetChanged();
        }

        if (current_video_category.equals("Adventure")) {
            movieShowAdapter = new MovieShowAdapter(this, adventureMovies, this);
            recyclerViewSimilarMovies.setAdapter(movieShowAdapter);
            recyclerViewSimilarMovies.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            movieShowAdapter.notifyDataSetChanged();
        }

        if (current_video_category.equals("Comedy")) {
            movieShowAdapter = new MovieShowAdapter(this, comedyMovies, this);
            recyclerViewSimilarMovies.setAdapter(movieShowAdapter);
            recyclerViewSimilarMovies.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            movieShowAdapter.notifyDataSetChanged();
        }

        if (current_video_category.equals("Romantic")) {
            movieShowAdapter = new MovieShowAdapter(this, romanticMovies, this);
            recyclerViewSimilarMovies.setAdapter(movieShowAdapter);
            recyclerViewSimilarMovies.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                    LinearLayoutManager.HORIZONTAL, false));
            movieShowAdapter.notifyDataSetChanged();
        }
    }

    private void similarMoviesRecycler() {
        uploads = new ArrayList<>();
        sportsMovies = new ArrayList<>();
        comedyMovies = new ArrayList<>();
        romanticMovies = new ArrayList<>();
        adventureMovies = new ArrayList<>();
        actionMovies = new ArrayList<>();

        mDataBaseReference = FirebaseDatabase.getInstance().getReference("videos");
        mDataBaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    GetVideoDetails upload = postSnapshot.getValue(GetVideoDetails.class);
                    if (upload.getVideo_category().equals("Action")) {
                        actionMovies.add(upload);
                    }

                    if (upload.getVideo_category().equals("Sports")) {
                        sportsMovies.add(upload);
                    }

                    if (upload.getVideo_category().equals("Adventure")) {
                        adventureMovies.add(upload);
                    }

                    if (upload.getVideo_category().equals("Comedy")) {
                        comedyMovies.add(upload);
                    }

                    if (upload.getVideo_category().equals("Sports")) {
                        romanticMovies.add(upload);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inView() {
        play_fab = findViewById(R.id.play_fab);
        tv_title = findViewById(R.id.detail_movie_title);
        tv_description = findViewById(R.id.detail_movie_desc);
        moviesThumbnail = findViewById(R.id.detail_movies_img);
        moviesCoverImg = findViewById(R.id.detail_movies_cover);
        recyclerViewSimilarMovies = findViewById(R.id.recycler_similar_movies);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String moviesTitle = bundle.getString("title");
        String imgRecordsId = bundle.getString("imgURL");
        String imageCover = bundle.getString("imgCover");
        String moviesDetailsText = bundle.getString("movieDetails");
        String moviesUrl = bundle.getString("movieUrl");
        String moviesCategory = bundle.getString("movieCategory");

        current_video_url = moviesUrl;
        current_video_category = moviesCategory;
        Glide.with(this).load(imgRecordsId).into(moviesThumbnail);
        Glide.with(this).load(imageCover).into(moviesCoverImg);
        tv_title.setText(moviesTitle);
        tv_description.setText(moviesDetailsText);
        getSupportActionBar().setTitle(moviesTitle);
    }

    @Override
    public void onMovieClick(GetVideoDetails getVideoDetails, ImageView imageView) {
        tv_title.setText(getVideoDetails.getVideo_name());
        getSupportActionBar().setTitle(getVideoDetails.getVideo_name());
        Glide.with(this).load(getVideoDetails.getVideo_thumb()).into(moviesThumbnail);
        Glide.with(this).load(getVideoDetails.getVideo_thumb()).into(moviesCoverImg);
        tv_description.setText(getVideoDetails.getVideo_description());
        current_video_url = getVideoDetails.getVideo_url();
        current_video_category = getVideoDetails.getVideo_category();
        ActivityOptions options = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            options = ActivityOptions.makeSceneTransitionAnimation(MovieDetailsActivity.this,
                    imageView, "sharedName");
        }
        options.toBundle();
    }
}