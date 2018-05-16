package clockworktt.gaby.com;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    private TextInputLayout mEventTitle;
    private TextInputLayout mEventDescription;
    private TextView mEventDate;
    private TextView mEventTime;

    private Spinner mEventDays;
    private String eventDays;

    private Spinner mEventTypes;
    private String eventTypes;

    private Spinner mEventGuestsNumbers;
    private String eventGuestNumbers;

    private Button mCreateEventButton;

    private DatabaseReference mEventDataRef;
    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        mEventTitle = findViewById(R.id.add_event_title);
        mEventDescription = findViewById(R.id.add_event_description);
        mEventDate = findViewById(R.id.add_event_date);
        mEventTime = findViewById(R.id.add_event_time);
        mEventDays = findViewById(R.id.add_event_day_spinner);
        mEventTypes = findViewById(R.id.add_event_type);
        mEventGuestsNumbers = findViewById(R.id.add_event_guests_numbers);
        mCreateEventButton = findViewById(R.id.add_event_button);

        getSupportActionBar().setTitle("Add Your Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating your event...");

        mEventDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("event");


        mEventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerFragment datePickerFragment =  new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(), "Event_Date");

            }
        });

        mEventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimePickerFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.show(getSupportFragmentManager(), "Event_Time");

            }
        });

        mEventDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eventDays = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mEventTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eventTypes = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mEventGuestsNumbers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eventGuestNumbers = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        mCreateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String eventTitle = mEventTitle.getEditText().getText().toString();
                String eventDescription = mEventDescription.getEditText().getText().toString();
                String eventDate = mEventDate.getText().toString();
                String eventTime = mEventTime.getText().toString();

                addEventToDatabase(eventTitle, eventDescription, eventDate, eventTime, eventDays, eventTypes, eventGuestNumbers);

            }
        });


    }//end onCreate()

    private void addEventToDatabase(String eventTitle, String eventDescription, String eventDate, String eventTime, String eventDays, String eventTypes, String eventGuestNumbers) {

        if(!eventTitle.equals("") && !eventDescription.equals("") && !eventDate.equals("Event Date") && !eventTime.equals("Event Time" ) && !eventGuestNumbers.equals("Number Of Guests?")){

            progressDialog.show();

            HashMap<String, String> map = new HashMap<>();
            map.put("title", eventTitle);
            map.put("description", eventDescription);
            map.put("date", eventDate);
            map.put("time", eventTime);
            map.put("days", eventDays);
            map.put("type", eventTypes);
            map.put("total_guests", eventGuestNumbers);

            uploadToDatabase(map);


        }else{

            Toast.makeText(AddEventActivity.this, "Check All Above Field & Input!", Toast.LENGTH_SHORT).show();

        }

    }

    private void uploadToDatabase(HashMap<String, String> map) {

        final String event_push_key = mEventDataRef.push().getKey();


        mEventDataRef.child(event_push_key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    DatabaseReference EventChildDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event_push_key).child("date");
                    EventChildDatabaseRef.setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                //hide progress dialog
                                progressDialog.dismiss();
                                //Show Event Created Toast with tic icon
                                StyleableToast.makeText(AddEventActivity.this, "Event Created Successfully!", R.style.uploadedToDatabase).show();
                                //send user to MainActivity.class

                                //TODO add TWO MORE DATABASE Object
                                //

                                Intent mainIntent = new Intent(AddEventActivity.this, UploadCsvActivity.class);
                                mainIntent.putExtra("event_push_key", event_push_key);
                                startActivity(mainIntent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();

                            }else {

                            }

                        }
                    });

                }else {

                }

            }
        });

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String currentDateString = DateFormat.getDateInstance().format(c.getTime());
        mEventDate.setGravity(Gravity.CENTER);
        mEventDate.setText(currentDateString);

    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);

        String timeSet = "";
        if (hourOfDay > 12) {
            hourOfDay -= 12;
            timeSet = "PM";
        } else if (hourOfDay == 0) {
            hourOfDay += 12;
            timeSet = "AM";
        } else if (hourOfDay == 12){
            timeSet = "PM";
        }else{
            timeSet = "AM";
        }

        String min = "";
        if (minute < 10)
            min = "0" + minute ;
        else
            min = String.valueOf(minute);

        // Append in a StringBuilder
        String aTime = new StringBuilder().append(hourOfDay).append(':')
                .append(min ).append(" ").append(timeSet).toString();


        mEventTime.setGravity(Gravity.CENTER);
        mEventTime.setText(aTime);

    }
}
