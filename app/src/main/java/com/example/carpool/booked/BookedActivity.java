package com.example.carpool.booked;

import static com.example.carpool.utils.Utils.AVAILABLE_RIDE;
import static com.example.carpool.utils.Utils.INFO;
import static com.example.carpool.utils.Utils.REQUEST_RIDE;
import static com.example.carpool.utils.Utils.checkNotifications;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carpool.adapter.BookingAdapter;
import com.example.carpool.R;
import com.example.carpool.interfaces.ResponseBooked;
import com.example.carpool.models.Info;
import com.example.carpool.utils.BottomNavigationViewHelper;
import com.example.carpool.utils.FirebaseMethods;
import com.example.carpool.models.BookingResults;
import com.example.carpool.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookedActivity extends AppCompatActivity implements ResponseBooked {

    private static final String TAG = "BookedActivity";
    private static final int ACTIVITY_NUMBER = 3;

    //View variables
    private RelativeLayout mNoResultsFoundLayout;
    private BottomNavigationView bottomNavigationView;

    //Recycle View variables
    private final Context mContext = BookedActivity.this;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecycleAdapter;
    private BookingAdapter myAdapter;
    private ArrayList<BookingResults> rides;

    //Firebase variables
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private FirebaseMethods mFirebaseMethods;
    private ImageView notFoundIcon;
    private TextView notFoundBooked;

    private String user_id;
    private Boolean isDriver = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked);
        setupBottomNavigationView();


        notFoundBooked = findViewById(R.id.notFoundBooked);
        notFoundIcon = findViewById(R.id.notFoundIcon);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mRecycleAdapter);
        rides = new ArrayList<BookingResults>();

        mNoResultsFoundLayout = findViewById(R.id.noResultsFoundLayout);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        if (mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();
            mRef.child(INFO).child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Info info = snapshot.getValue(Info.class);
                    isDriver = info.getCarOwner();
                    if (isDriver) {
                        mRef.child(REQUEST_RIDE).child(user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                        BookingResults booking = dataSnapshot1.getValue(BookingResults.class);
                                        if (!booking.getAccepted()) {
                                            rides.add(booking);
                                            Log.d(TAG, "onDataChange: ride = " + rides);
                                        }
                                        notFoundBooked.setVisibility(View.INVISIBLE);
                                        notFoundIcon.setVisibility(View.INVISIBLE);
                                    }
                                }
                                Log.d(TAG, "onDataChange: " + rides.size());

                                myAdapter = new BookingAdapter(BookedActivity.this, rides, isDriver, BookedActivity.this, mRef);
                                mRecyclerView.setAdapter(myAdapter);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        mRef.child(REQUEST_RIDE).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                        for (DataSnapshot dataSnapshot2: dataSnapshot1.getChildren()) {
                                            BookingResults r = dataSnapshot2.getValue(BookingResults.class);

                                            if (r.getPassengerID().equals(user_id)) {
                                                rides.add(r);
                                            }
                                            Log.d(TAG, "onDataChange: " + r);
                                            notFoundBooked.setVisibility(View.INVISIBLE);
                                            notFoundIcon.setVisibility(View.INVISIBLE);
                                            /*mNoResultsFoundLayout.setVisibility(View.INVISIBLE);*/
                                        }
                                    }
                                }
                                Log.d(TAG, "onDataChange: " + rides.size());

                                myAdapter = new BookingAdapter(BookedActivity.this, rides, isDriver, BookedActivity.this, mRef);
                                mRecyclerView.setAdapter(myAdapter);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            Log.i(TAG, "onCreate: "+ user_id);
        }

        checkNotifications(mRef, user_id, mContext, bottomNavigationView);

    }


    private void setupBottomNavigationView(){
        bottomNavigationView = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUMBER);
        menuItem.setChecked(true);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    int seatsAvailable = 0;

    @Override
    public void responseBooked(Boolean isAccept, String rideId, int pos, String passengerId, int seat) {
        if (isAccept) {
            mRef.child(REQUEST_RIDE).child(user_id).child(rideId).child("accepted").setValue(true);
            mRef.child(AVAILABLE_RIDE).child(rideId).child("seatsAvailable").setValue(seat - 1);
            String user = "passenger_" + (4-seat);
            mRef.child("participant").child(rideId).child(user).setValue(passengerId);
        } else {
            mRef.child(REQUEST_RIDE).child(user_id).child(rideId).removeValue();
        }
        rides.remove(pos);
        myAdapter.notifyDataSetChanged();
        checkNotifications(mRef, user_id, mContext, bottomNavigationView);
    }
}
