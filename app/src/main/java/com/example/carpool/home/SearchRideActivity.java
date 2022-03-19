package com.example.carpool.home;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carpool.R;
import com.example.carpool.utils.FirebaseMethods;
import com.example.carpool.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


@SuppressWarnings("ALL")
public class SearchRideActivity extends AppCompatActivity {

    private static final String TAG = "MinhMX";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;

    private Context mContext;

    private View view;

    private EditText mDestinationEditText;
    private EditText mFromEditText;
    private EditText mDateOfJourneyEditText;
    private Button mSnippetSeachARideBtn;
    private Switch mSameGenderSearchSwitch;
    private Boolean sameGender;

    //vars
    private User mUserSettings;
    private Calendar mCalendar;
    private DatePickerDialog.OnDateSetListener date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search_ride);

        mContext = SearchRideActivity.this;

        setupWidgets();
        setupFirebaseAuth();
        getActivityData();

        ImageView backArrow = (ImageView) findViewById(R.id.backArrowFindRide);
        backArrow.setOnClickListener(v -> finish());

        mSameGenderSearchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                sameGender = true;
            } else {
                sameGender = false;
            }
        });

        mDateOfJourneyEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 0);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SearchRideActivity.this, date, mCalendar
                    .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.show();
        });


        mSnippetSeachARideBtn.setOnClickListener(v -> {
            if (mDateOfJourneyEditText.getText().length() > 0 &&  mFromEditText.getText().length() > 0 && mDestinationEditText.getText().length() > 0) {
                Intent intent = new Intent(mContext, SearchResultsActivity.class);
                intent.putExtra(getString(R.string.intent_location), mDestinationEditText.getText().toString());
                intent.putExtra(getString(R.string.intent_destination), mFromEditText.getText().toString());
                intent.putExtra(getString(R.string.intent_gender), sameGender);
                intent.putExtra(getString(R.string.intent_date), mDateOfJourneyEditText.getText().toString());
                startActivity(intent);
            } else {
                Toast.makeText(mContext, getString(R.string.all_fields), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getActivityData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mFromEditText.setText(getIntent().getStringExtra(getString(R.string.intent_location)));
            mDestinationEditText.setText(getIntent().getStringExtra(getString(R.string.intent_destination)));
        }
    }

    private void setupWidgets(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        mFirebaseMethods = new FirebaseMethods(this);

        mDestinationEditText = (EditText) findViewById(R.id.destinationEditText);
        mFromEditText = (EditText) findViewById(R.id.locationEditText);
        mDateOfJourneyEditText = (EditText) findViewById(R.id.dateEditText);
        mSameGenderSearchSwitch = (Switch) findViewById(R.id.sameGenderSearchSwitch);
        mSnippetSeachARideBtn = (Button) findViewById(R.id.snippetSearchARideBtn);

        mCalendar = Calendar.getInstance();
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, month);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
    }

    private void updateLabel() {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());

        mDateOfJourneyEditText.setText(simpleDateFormat.format(mCalendar.getTime()));
    }

    private void setupFirebaseAuth(){
        userID = mAuth.getCurrentUser().getUid();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }
}
