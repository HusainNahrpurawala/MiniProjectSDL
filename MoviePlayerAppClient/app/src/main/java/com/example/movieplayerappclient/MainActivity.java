package com.example.movieplayerappclient;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.movieplayerappclient.Adapter.MovieShowAdapter;
import com.example.movieplayerappclient.Adapter.SliderPagerAdapterNew;
import com.example.movieplayerappclient.Model.GetVideoDetails;
import com.example.movieplayerappclient.Model.MovieItemClickListenerNew;
import com.example.movieplayerappclient.Model.SliderSide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MovieItemClickListenerNew {
    MovieShowAdapter movieShowAdapter;
    DatabaseReference mDatabaseReference;
    ProgressDialog progressDialog;
    private List<GetVideoDetails> uploads, uploadsListLatest, uploadsListPopular;
    private List<GetVideoDetails> actionMovies, sportsMovies, comedyMovies,
            romanticMovies, adventureMovies;
    private ViewPager sliderPager;
    private List<SliderSide> uploadsSlider;
    private TabLayout indicator, tabMoviesActions;
    private RecyclerView moviesRv, moviesRvWeek, tab;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        progressDialog = new ProgressDialog(this);

        inViews();
        addAllMovies();
        iniPopularMovies();
        iniWeekMovies();
        moviesViewTab();
    }

    private void addAllMovies() {
        uploads = new ArrayList<>();
        uploadsListLatest = new ArrayList<>();
        uploadsListPopular = new ArrayList<>();
        actionMovies = new ArrayList<>();
        adventureMovies = new ArrayList<>();
        comedyMovies = new ArrayList<>();
        sportsMovies = new ArrayList<>();
        romanticMovies = new ArrayList<>();
        uploadsSlider = new ArrayList<>();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("videos");
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    GetVideoDetails upload = postSnapshot.getValue(GetVideoDetails.class);
                    SliderSide slide = postSnapshot.getValue(SliderSide.class);
                    if (upload.getVideo_type().toLowerCase().equals("latest movies")) {
                        uploadsListLatest.add(upload);
                    }

                    if (upload.getVideo_type().toLowerCase().equals("best popular movies")) {
                        uploadsListPopular.add(upload);
                    }

                    if (upload.getVideo_category().equals("Action")) {
                        actionMovies.add(upload);
                    }

                    if (upload.getVideo_category().equals("Adventure")) {
                        adventureMovies.add(upload);
                    }

                    if (upload.getVideo_category().equals("Comedy")) {
                        comedyMovies.add(upload);
                    }

                    if (upload.getVideo_category().equals("Romantic")) {
                        romanticMovies.add(upload);
                    }

                    if (upload.getVideo_category().equals("Sports")) {
                        sportsMovies.add(upload);
                    }

                    if (upload.getVideo_slide().equals("Slide movies")) {
                        uploadsSlider.add(slide);
                    }

                    uploads.add(upload);
                }
                inSlider();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inSlider() {
        SliderPagerAdapterNew adapterNew = new SliderPagerAdapterNew(this, uploadsSlider);
        sliderPager.setAdapter(adapterNew);
        adapterNew.notifyDataSetChanged();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 6000);
        indicator.setupWithViewPager(sliderPager, true);
    }

    private void iniWeekMovies() {
        movieShowAdapter = new MovieShowAdapter(this, uploadsListLatest, this);
        moviesRvWeek.setAdapter(movieShowAdapter);
        moviesRvWeek.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void iniPopularMovies() {
        movieShowAdapter = new MovieShowAdapter(this, uploadsListPopular, this);
        moviesRv.setAdapter(movieShowAdapter);
        moviesRv.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void moviesViewTab() {
        getActionMovies();
        tabMoviesActions.addTab(tabMoviesActions.newTab().setText("Action"));
        tabMoviesActions.addTab(tabMoviesActions.newTab().setText("Adventure"));
        tabMoviesActions.addTab(tabMoviesActions.newTab().setText("Comedy"));
        tabMoviesActions.addTab(tabMoviesActions.newTab().setText("Romantic"));
        tabMoviesActions.addTab(tabMoviesActions.newTab().setText("Sports"));
        tabMoviesActions.setTabGravity(TabLayout.GRAVITY_FILL);
        tabMoviesActions.setTabTextColors(ColorStateList.valueOf(Color.WHITE));

        tabMoviesActions.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getActionMovies();
                        break;
                    case 1:
                        getAdventureMovies();
                        break;
                    case 2:
                        getComedyMovies();
                        break;
                    case 3:
                        getRomanticMovies();
                        break;
                    case 4:
                        getSportsMovies();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void inViews() {
        tabMoviesActions = findViewById(R.id.tabActionMovies);
        sliderPager = findViewById(R.id.slider_pager);
        indicator = findViewById(R.id.indicator);
        moviesRvWeek = findViewById(R.id.rv_movies_week);
        moviesRv = findViewById(R.id.rv_movies);
        tab = findViewById(R.id.tabRecycler);
    }

    @Override
    public void onMovieClick(GetVideoDetails getVideoDetails, ImageView imageView) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra("title", getVideoDetails.getVideo_name());
        intent.putExtra("imgURL", getVideoDetails.getVideo_thumb());
        intent.putExtra("movieDetails", getVideoDetails.getVideo_description());
        intent.putExtra("imgCover", getVideoDetails.getVideo_thumb());
        intent.putExtra("movieUrl", getVideoDetails.getVideo_url());
        intent.putExtra("movieCategory", getVideoDetails.getVideo_category());
        ActivityOptions options = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, imageView, "sharedName");
        }
        startActivity(intent, options.toBundle());
    }

    public class SliderTimer extends TimerTask {
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (sliderPager.getCurrentItem() < uploadsSlider.size() - 1) {
                        sliderPager.setCurrentItem(sliderPager.getCurrentItem() - 1);
                    } else {
                        sliderPager.setCurrentItem(0);
                    }
                    iniPopularMovies();
                }
            });
        }
    }

    private void getActionMovies() {
        Log.d("Debugging", "Action movies called");
        movieShowAdapter = new MovieShowAdapter(this, actionMovies, this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void getSportsMovies() {
        movieShowAdapter = new MovieShowAdapter(this, sportsMovies, this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void getRomanticMovies() {
        movieShowAdapter = new MovieShowAdapter(this, romanticMovies, this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void getComedyMovies() {
        movieShowAdapter = new MovieShowAdapter(this, comedyMovies, this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        movieShowAdapter.notifyDataSetChanged();
    }

    private void getAdventureMovies() {
        movieShowAdapter = new MovieShowAdapter(this, adventureMovies, this);
        tab.setAdapter(movieShowAdapter);
        tab.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        movieShowAdapter.notifyDataSetChanged();
    }
}
