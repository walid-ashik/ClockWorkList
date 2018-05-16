package clockworktt.gaby.com;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditEventActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    private static final int GALLERY_PICK = 1;
    private String mEvent_key;

    private CircleImageView mEventImage;

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

    private Button mUpdateEventButton;

    private DatabaseReference mEventDataRef;
    private DatabaseReference mEventIconRef;
    private StorageReference mGuestImage;
    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        mEvent_key = getIntent().getStringExtra("event_key");

        mEventImage = findViewById(R.id.edit_event_icon);
        mEventTitle = findViewById(R.id.edit_event_title);
        mEventDescription = findViewById(R.id.edit_event_description);
        mEventDate = findViewById(R.id.edit_event_date);
        mEventTime = findViewById(R.id.edit_event_time);
        mEventDays = findViewById(R.id.edit_event_day_spinner);
        mEventTypes = findViewById(R.id.edit_event_type);
        mEventGuestsNumbers = findViewById(R.id.edit_event_guests_numbers);
        mUpdateEventButton = findViewById(R.id.update_event_button);

        mGuestImage = FirebaseStorage.getInstance().getReference().child("event_icons");


        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating your event...");

        mEventDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("event").child(mEvent_key);
        mEventIconRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("event").child(mEvent_key).child("thumb_image");

        setEventIcon(mEventDataRef);

        mEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Choose Your Image"), GALLERY_PICK);


            }
        });

        setEventInfo(mEventDataRef);

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

        mUpdateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String eventTitle = mEventTitle.getEditText().getText().toString();
                String eventDescription = mEventDescription.getEditText().getText().toString();
                String eventDate = mEventDate.getText().toString();
                String eventTime = mEventTime.getText().toString();

                updateEvent(eventTitle, eventDescription, eventDate, eventTime, eventDays, eventTypes, eventGuestNumbers);


            }
        });

    }

    private void setEventInfo(DatabaseReference mEventDataRef) {

        mEventDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("title")){
                  String title = dataSnapshot.child("title").getValue().toString();
                  mEventTitle.getEditText().setText(title);
                }

                if(dataSnapshot.hasChild("description")){
                  String description = dataSnapshot.child("description").getValue().toString();
                  mEventDescription.getEditText().setText(description);
                }

                if(dataSnapshot.hasChild("date")){
                  String date = dataSnapshot.child("date").getValue().toString();
                  mEventDate.setGravity(Gravity.CENTER);
                  mEventDate.setText(date);
                }

                if(dataSnapshot.hasChild("time")){
                   String time = dataSnapshot.child("time").getValue().toString();
                   mEventTime.setGravity(Gravity.CENTER);
                   mEventTime.setText(time);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setEventIcon(DatabaseReference mEventDataRef) {

        mEventDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("thumb_icon")){
                    String eventIcon = dataSnapshot.child("thumb_icon").getValue().toString();

                    Picasso.with(EditEventActivity.this)
                            .load(eventIcon)
                            .placeholder(R.drawable.app_icon_circle)
                            .into(mEventImage);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateEvent(String eventTitle, String eventDescription, String eventDate, String eventTime, String eventDays, String eventTypes, String eventGuestNumbers) {
        if(!eventTitle.equals("") && !eventDescription.equals("") && !eventDate.equals("Event Date") && !eventTime.equals("Event Time" ) && !eventGuestNumbers.equals("Number Of Guests?")){

            progressDialog.show();

            Map map = new HashMap();
            map.put("title", eventTitle);
            map.put("description", eventDescription);
            map.put("date", eventDate);
            map.put("time", eventTime);
            map.put("days", eventDays);
            map.put("type", eventTypes);
            map.put("total_guests", eventGuestNumbers);

            updateToDatabase(map);


        }else{

            Toast.makeText(EditEventActivity.this, "Check All Above Field & Input!", Toast.LENGTH_SHORT).show();

        }
    }

    private void updateToDatabase(Map map) {

        mEventDataRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if(task.isSuccessful()){

                    progressDialog.dismiss();
                    Intent eventListIntent = new Intent(EditEventActivity.this, GuestListActivity.class);
                    eventListIntent.putExtra("event_key",mEvent_key);
                    Log.d("EVENT", "onComplete: " + "Event Updated!");
                    startActivity(eventListIntent);

                }else {
                    progressDialog.dismiss();
                    Toast.makeText(EditEventActivity.this, "Updating failed...please try again!", Toast.LENGTH_SHORT).show();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        Bitmap thumb_icon;
        byte[] thumb_icon_data = new byte[0];

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                File thumb_icon_file = new File(resultUri.getPath());


                try {
                    thumb_icon = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_icon_file);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_icon.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_icon_data = baos.toByteArray();


                } catch (IOException e) {
                    e.printStackTrace();
                }

                //TODO replace puskh key event
                StorageReference imagePath = mGuestImage.child(mEvent_key + ".jpg");
                final StorageReference thumbIconPath = mGuestImage.child("thumb_icon").child(mEvent_key + ".jpg");

                final UploadTask uploadTask = thumbIconPath.putBytes(thumb_icon_data);

                imagePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {

                                        final String thumbImageDownloadUri = task.getResult().getDownloadUrl().toString();

                                        Map map = new HashMap();
                                        map.put("thumb_icon", thumbImageDownloadUri);
                                        map.put("icon", thumbImageDownloadUri);

                                        mEventDataRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                if (task.isSuccessful()) {
                                                    Picasso.with(getApplicationContext())
                                                            .load(thumbImageDownloadUri)
                                                            .into(mEventImage);
                                                    //Icon Uploaded to Firebase
                                                    Toast.makeText(EditEventActivity.this, "Image Updated!", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(EditEventActivity.this, "Uploading Fail...please try again!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                }
                            });

                        } else {

                            progressDialog.dismiss();
                            Toast.makeText(EditEventActivity.this, "Uploading Fail...please try again!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }

        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            progressDialog.dismiss();
            Toast.makeText(this, "CROP IMAGE ACTIVITY GOT AN ERROR!", Toast.LENGTH_SHORT).show();
        }

    }
}
