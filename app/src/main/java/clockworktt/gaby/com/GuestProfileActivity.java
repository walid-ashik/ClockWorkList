package clockworktt.gaby.com;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class GuestProfileActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private Toolbar mToolbar;

    //bundle string
    private String event_key;
    private String guest_key;
    private String name;
    private String occupation;
    private String company;
    private String image;

    //xml
    private CircleImageView mProfileImage;
    private TextView mGuestName;
    private TextInputLayout mName;
    private TextInputLayout mSalutation;
    private TextInputLayout mEmail;
    private TextInputLayout mCompany;
    private TextInputLayout mOccupation;
    private TextInputLayout mPhone;
    private TextInputLayout mExpectedNumbers;
    private TextInputLayout mNotes;
    private Button mUpdateButton;

    private ProgressDialog progressDialog;
    private ProgressDialog deleteDialog;


    //firebase
    private DatabaseReference mGuestDataRef;
    private StorageReference mGuestImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_profile);

        //toolbar
        mToolbar = findViewById(R.id.guest_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("  Guest");
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //xml binding
        mProfileImage = findViewById(R.id.guest_profile_image);
        mGuestName = findViewById(R.id.guest_profile_name);
        mName = findViewById(R.id.profile_name_input_layout);
        mSalutation = findViewById(R.id.profile_salutation_input_layout);
        mEmail = findViewById(R.id.profile_email);
        mCompany = findViewById(R.id.profile_company);
        mOccupation = findViewById(R.id.profile_occupation);
        mPhone = findViewById(R.id.profile_phone);
        mExpectedNumbers = findViewById(R.id.profile_expected_numbers);
        mNotes = findViewById(R.id.profile_notes);
        mUpdateButton = findViewById(R.id.profile_update_button);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Info...");

        deleteDialog = new ProgressDialog(this);
        deleteDialog.setMessage("Deleting Guest...");

        //catch the data from previous intent
        Bundle getBundle = getIntent().getExtras();
        event_key = getBundle.getString("event_key");
        guest_key = getBundle.getString("guest_key");
        name = getBundle.getString("name");
        occupation = getBundle.getString("occupation");
        company = getBundle.getString("company");
        image = getBundle.getString("image");

        mGuestDataRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event_key).child("guest_list").child(guest_key);
        mGuestImage = FirebaseStorage.getInstance().getReference().child("guest_images");

        setGuestProfile(name, image);
        setGuestInputLayoutInfo(name, occupation, company);

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.show();
                updateUserProfile();

            }
        });



    }

    private void deleteUser() {

        mGuestDataRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    deleteDialog.dismiss();
                    StyleableToast.makeText(GuestProfileActivity.this, name + " is removed from this event!", R.style.loginErrorToastColor).show();
                    finish();

                } else {
                    deleteDialog.dismiss();
                    StyleableToast.makeText(GuestProfileActivity.this, "An Unwanted Error Occured! try again.", R.style.loginErrorToastColor).show();

                }

            }
        });

    }

    private void updateUserProfile() {

        String updatedName = mName.getEditText().getText().toString();
        String updatedSalutation = mSalutation.getEditText().getText().toString();
        String updatedEmail = mEmail.getEditText().getText().toString();
        String updatedOccupation = mOccupation.getEditText().getText().toString();
        String updatedCompany = mCompany.getEditText().getText().toString();
        String updatedPhone = mPhone.getEditText().getText().toString();
        String updatedExpectedNumbers = mExpectedNumbers.getEditText().getText().toString();
        String updatedNotes = mNotes.getEditText().getText().toString();

        Map map = new HashMap();
        map.put("first_name", updatedName);
        map.put("last_name", "");
        map.put("salutation", updatedSalutation);
        map.put("email", updatedEmail);
        map.put("occupation", updatedOccupation);
        map.put("company", updatedCompany);
        map.put("contact_number", updatedPhone);
        map.put("expected_numbers", updatedExpectedNumbers);
        map.put("notes", updatedNotes);

        mGuestDataRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()) {

                    progressDialog.dismiss();
                    Toast.makeText(GuestProfileActivity.this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    progressDialog.dismiss();
                    StyleableToast.makeText(GuestProfileActivity.this, "An Unwanted Error Occured! try again.", R.style.loginErrorToastColor).show();
                }

            }
        });

    }

    private void setGuestInputLayoutInfo(String name, String occupation, String company) {

        mName.getEditText().setText(name);
        mOccupation.getEditText().setText(occupation);
        mCompany.getEditText().setText(company);

        mGuestDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("salutation")) {
                    String salutation = dataSnapshot.child("salutation").getValue().toString();
                    mSalutation.getEditText().setText(salutation);
                }

                if (dataSnapshot.hasChild("email")) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    mEmail.getEditText().setText(email);
                }

                if (dataSnapshot.hasChild("contact_number")) {
                    String contact_number = dataSnapshot.child("contact_number").getValue().toString();
                    mPhone.getEditText().setText(contact_number);
                }

                if (dataSnapshot.hasChild("expected_numbers")) {
                    String expected_numbers = dataSnapshot.child("expected_numbers").getValue().toString();
                    mExpectedNumbers.getEditText().setText(expected_numbers);
                }

                if (dataSnapshot.hasChild("notes")) {
                    String notes = dataSnapshot.child("notes").getValue().toString();
                    mNotes.getEditText().setText(notes);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setGuestProfile(String name, String image) {

        if (!name.equals("")) {
            mGuestName.setText(name);
        }

        Log.d("IMAGE", "GPA: " + image);

        if (!TextUtils.isEmpty(image)) {
            Picasso.with(getApplicationContext())
                    .load(image)
                    .placeholder(R.drawable.ic_avatar)
                    .into(mProfileImage);
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.guest_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.delete_guest) {

            deleteDialog.show();
            deleteUser();

        } else if (item.getItemId() == R.id.update_guest) {

            updateUserProfile();

        } else if (item.getItemId() == R.id.update_photo) {


            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(galleryIntent, "Choose Your Image"), GALLERY_PICK);
        }

        return true;
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
                StorageReference imagePath = mGuestImage.child(guest_key + ".jpg");
                final StorageReference thumbIconPath = mGuestImage.child("guest_thumb_images").child(guest_key + ".jpg");

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
                                        map.put("image", thumbImageDownloadUri);

                                        mGuestDataRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                if (task.isSuccessful()) {
                                                    Picasso.with(getApplicationContext())
                                                            .load(thumbImageDownloadUri)
                                                            .into(mProfileImage);
                                                    //Icon Uploaded to Firebase
                                                    Toast.makeText(GuestProfileActivity.this, "Image Updated!", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(GuestProfileActivity.this, "Uploading Fail...please try again!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                }
                            });

                        } else {

                            progressDialog.dismiss();
                            Toast.makeText(GuestProfileActivity.this, "Uploading Fail...please try again!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }

        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(this, "CROP IMAGE ACTIVITY GOT AN ERROR!", Toast.LENGTH_SHORT).show();
        }

    }
}