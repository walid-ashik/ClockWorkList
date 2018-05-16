package clockworktt.gaby.com;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class UploadCsvActivity extends AppCompatActivity {

    private CircleImageView mEventIcon;
    private Button mSetEventIconButton;
    private Button mUploadCsvButton;

    private DatabaseReference mEventDatabaseRef;
    private StorageReference mEventIconStorageRef;

    private static final int  GALLERY_PICK = 0;
    public static final int PERMISSIONS_REQUEST_CODE = 0;

    private ProgressDialog progressDialog;
    private ProgressDialog guestListUploadingDialog;

    //TODO
    //Replace this PUSKHKEVENT from getting Intent.putExtras from AddEventActivity class
    private String pushKeyEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_csv);

        getSupportActionBar().hide();

        mEventIcon = findViewById(R.id.event_image_icon);
        mSetEventIconButton = findViewById(R.id.event_image_button);
        mUploadCsvButton = findViewById(R.id.upload_csv_guest_list);

        Intent intent = getIntent();
        pushKeyEvent = intent.getStringExtra("event_push_key");

        progressDialog = new ProgressDialog(this);
        guestListUploadingDialog = new ProgressDialog(this);

        progressDialog.setMessage("Please Wait...");

        guestListUploadingDialog.setTitle("Please Wait...");
        guestListUploadingDialog.setMessage("Don't turn your internet connection Off while uploading Guest list to database!");
        guestListUploadingDialog.setCanceledOnTouchOutside(false);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mEventDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("event").child(pushKeyEvent);
        mEventIconStorageRef = FirebaseStorage.getInstance().getReference().child("event_icons");

       //TODO get EVENT name from Previous Intent by INTENT Extra

        mSetEventIconButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Choose Your Image"), GALLERY_PICK);

            }
        });

        setEventIcon();

        //TODO upload CSV to ScanChild
        mUploadCsvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermissionsAndOpenFilePicker();

            }
        });

    }


    private void checkPermissionsAndOpenFilePicker() {
        String permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showError();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            chooseFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseFile();
                } else {
                    showError();
                }
            }
        }
    }

    private void chooseFile() {

        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(1)
                // .withFilter(Pattern.compile("image/")) // Filtering files and directories by file name using regexp
                .withFilterDirectories(false) // Set directories filterable (false by default)
                .withHiddenFiles(false) // Show hidden files and folders
                .start();


    }


    private List<CsvSample> csvSamplesList = new ArrayList<>();
    private void readCSV(String filePath) {
        try {
            InputStream inputStream1 = new FileInputStream(filePath);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream1, Charset.forName("UTF-8"))
            );

            String line = "";
            try {

                //Step OVer Headers
                bufferedReader.readLine();

                HashMap<String, String> map = new HashMap<>();

                while ( (line = bufferedReader.readLine()) != null){

                    Log.d("UploadCsvActivity", "Line: " + line);

                    //Split by ','
                    String[] tokens = line.split(",");

                    //Read Data
                    CsvSample csvSample = new CsvSample();

                    String firstName = "", lastName = "", fullName = "";

                    //SALUTATION
                    if( tokens.length >= 1 && tokens[0].length() > 0){
                        csvSample.setSalutation(tokens[0]);
                        Log.d("CSV", "salutation: " + csvSample.getSalutation().toString() + " ");
                        map.put("salutation", csvSample.getSalutation().toString());
                    }else {
                        csvSample.setSalutation(null);
                      //  map.put("name", "no info");
                        map.put("salutation", "none");
                    }

                    //FIRST NAME
                    if(tokens.length >= 2 && tokens[1].length() > 0){
                        csvSample.setFirst_name(tokens[1]);
                        Log.d("CSV", "first_name: " + csvSample.getFirst_name().toString() + " ");
                        map.put("first_name", csvSample.getFirst_name().toString().toLowerCase());
                        firstName = csvSample.getFirst_name().toUpperCase();
                    }else {
                        csvSample.setFirst_name(null);
                        map.put("first_name", " ");
                    }

                    //LAST NAME
                    if(tokens.length >=3  && tokens[2].length() > 0){
                        csvSample.setLast_name(tokens[2]);
                        Log.d("CSV", ":last_name: " + csvSample.getLast_name().toString() + " ");
                        map.put("last_name", csvSample.getLast_name().toString().toLowerCase());
                        lastName = csvSample.getLast_name().toUpperCase();
                    }else {
                        csvSample.setLast_name(null);
                        map.put("last_name", " ");
                    }

                    //EMAIL
                    if(tokens.length >= 4 && tokens[3].length() > 0){
                        csvSample.setEmail(tokens[3]);
                        Log.d("CSV", ":email: " + csvSample.getEmail().toString() + " ");
                        map.put("email", csvSample.getEmail().toString());
                    }else {
                        csvSample.setEmail(null);
                        map.put("email", "none");
                    }

                    //CONTACT NUMBER
                    if(tokens.length >= 5 && tokens[4].length() > 0){
                        csvSample.setContact_number(tokens[4]);
                        Log.d("CSV", "contact_number: " + csvSample.getContact_number().toString() + " ");
                        map.put("contact_number", csvSample.getContact_number().toString());
                    }else {
                        csvSample.setContact_number(null);
                        map.put("contact_number", "none");
                    }

                    //COMPANY
                    if(tokens.length >= 6 && tokens[5].length() > 0){
                        csvSample.setCompany(tokens[5]);
                        Log.d("CSV", "company: " + csvSample.getCompany().toString() + " ");
                        map.put("company", csvSample.getCompany().toString());
                    }else {
                        csvSample.setCompany(null);
                        map.put("company", "none");
                    }

                    //OCCUPATION
                    if(tokens.length >= 7 && tokens[6].length() > 0){
                        csvSample.setOccupation(tokens[6]);
                        Log.d("CSV", ":occupation: " + csvSample.getOccupation().toString() + " ");
                        map.put("occupation", csvSample.getOccupation().toString());
                    }else {
                        csvSample.setOccupation(null);
                        map.put("occupation", "none");
                    }

                    //EXPECTED NUMBERS
                    if(tokens.length >= 8 && tokens[7].length() > 0){
                        csvSample.setExpected_numbers(tokens[7]);
                        Log.d("CSV", "expected_numbers: " + csvSample.getExpected_numbers().toString() + " ");
                        map.put("expected_numbers", csvSample.getExpected_numbers().toString());
                    }else {
                        map.put("expected_numbers","none");
                    }

                    //NOTES
                    if(tokens.length >= 9 && tokens[8].length() > 0){
                        csvSample.setNotes(tokens[8]);
                        Log.d("CSV", "notes: " + csvSample.getNotes().toString() + " ");
                        map.put("notes", csvSample.getNotes().toString());
                    }else {
                        csvSample.setNotes(null);
                        map.put("notes","none");
                    }

                    map.put("full_name", firstName + " " + lastName);

                    uploadToEventDatabase(map);
                    csvSamplesList.add(csvSample);
                    Log.d("AccountActivity", "Just Created" + csvSample);

                }
            } catch (IOException e) {
                Log.wtf("UploadCsvActivity", "Error reading data file on line" + line, e);
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }


    private void uploadToEventDatabase(HashMap<String, String> map) {
        guestListUploadingDialog.show();

         final DatabaseReference mEventGuestListRef = FirebaseDatabase.getInstance().getReference().child("Events").child(pushKeyEvent).child("guest_list");
         String guest_push_key = mEventGuestListRef.push().getKey();


            mEventGuestListRef.child(guest_push_key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){

                        guestListUploadingDialog.dismiss();

                        Intent mainIntent = new Intent(UploadCsvActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();

                    }else {
                        //Due to any data upload error...REMOVE ALL half uploaded guest lists
                        //Need to upload all list or remove existing lists!
                        mEventGuestListRef.removeValue();
                        guestListUploadingDialog.dismiss();
                    }


                }
            });




    }

    private void showError() {
        Toast.makeText(this, "Allow external storage reading", Toast.LENGTH_SHORT).show();
    }

    private void setEventIcon() {

        mEventDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("icon")){
                    String eventIcon = dataSnapshot.child("icon").getValue().toString();

                    Picasso.with(UploadCsvActivity.this)
                            .load(eventIcon)
                            .placeholder(R.drawable.app_icon_circle)
                            .into(mEventIcon);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file

            if(filePath != null){
                readCSV(filePath);
            }
        }

        Bitmap thumb_icon;
        byte[] thumb_icon_data = new byte[0];

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            progressDialog.show();
            progressDialog.setCanceledOnTouchOutside(false);

            if(resultCode == RESULT_OK){
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
                StorageReference iconPath = mEventIconStorageRef.child(pushKeyEvent + ".jpg");
                final StorageReference thumbIconPath = mEventIconStorageRef.child("thumb_icon").child(pushKeyEvent + ".jpg");

                final UploadTask uploadTask = thumbIconPath.putBytes(thumb_icon_data);

                iconPath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        final String iconDownloadUrl = task.getResult().getDownloadUrl().toString();

                        if(task.isSuccessful()){

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()){

                                        String thumbIconDownlaodUrl = task.getResult().getDownloadUrl().toString();

                                        Map map = new HashMap();
                                        map.put("icon", iconDownloadUrl);
                                        map.put("thumb_icon", thumbIconDownlaodUrl);

                                        mEventDatabaseRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    //Icon Uploaded to FirebaseStorage
                                                    Toast.makeText(UploadCsvActivity.this, "Icon Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                                    StyleableToast.makeText(UploadCsvActivity.this, "Now Upload Your Guest Lists!",Toast.LENGTH_LONG, R.style.loginErrorToastColor).show();

                                                }else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(UploadCsvActivity.this, "Uploading Fail...please try again!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                }
                            });

                        }else {

                            progressDialog.dismiss();
                            Toast.makeText(UploadCsvActivity.this, "Uploading Fail...please try again!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }

        }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
            Toast.makeText(this, "CROP IMAGE ACTIVITY GOT AN ERROR!", Toast.LENGTH_SHORT).show();
        }

    }
}
