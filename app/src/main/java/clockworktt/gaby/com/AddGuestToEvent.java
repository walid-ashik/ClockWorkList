package clockworktt.gaby.com;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddGuestToEvent extends AppCompatActivity {

    private String event_key;

    private CircleImageView mProfileImage;
    private TextView mGuestName;
    private TextInputLayout mFirstName;
    private TextInputLayout mLastName;
    private TextInputLayout mSalutation;
    private TextInputLayout mEmail;
    private TextInputLayout mCompany;
    private TextInputLayout mOccupation;
    private TextInputLayout mPhone;
    private TextInputLayout mExpectedNumbers;
    private TextInputLayout mNotes;
    private Button mUpdateButton;

    private ProgressDialog pd;

    //firebase
    private DatabaseReference mAddGuestDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guest_to_event);

        event_key = getIntent().getStringExtra("event_key");

        pd = new ProgressDialog(this);
        pd.setMessage("Creating guest...");

        //xml binding
        mProfileImage = findViewById(R.id.guest_profile_image);
        mGuestName = findViewById(R.id.guest_profile_name);
        mFirstName = findViewById(R.id.profile_first_name_input_layout);
        mLastName = findViewById(R.id.profile_last_name_input_layout);
        mSalutation = findViewById(R.id.profile_salutation_input_layout);
        mEmail = findViewById(R.id.profile_email);
        mCompany = findViewById(R.id.profile_company);
        mOccupation = findViewById(R.id.profile_occupation);
        mPhone = findViewById(R.id.profile_phone);
        mExpectedNumbers = findViewById(R.id.profile_expected_numbers);
        mNotes = findViewById(R.id.profile_notes);
        mUpdateButton = findViewById(R.id.profile_update_button);

        mAddGuestDataRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event_key).child("guest_list");


        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pd.show();

                String first_name = mFirstName.getEditText().getText().toString().toLowerCase();
                String last_name = mLastName.getEditText().getText().toString().toLowerCase();
                String salutation = mSalutation.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String company = mCompany.getEditText().getText().toString();
                String occupation = mOccupation.getEditText().getText().toString();
                String contact_number = mPhone.getEditText().getText().toString();
                String expected_numbers= mExpectedNumbers.getEditText().getText().toString();
                String notes = mNotes.getEditText().getText().toString();

                checkInputField(first_name, last_name, salutation, email, company, occupation, contact_number, expected_numbers, notes);

                if(checkInputField(first_name, last_name, salutation, email, company, occupation, contact_number, expected_numbers, notes) == true){

                    final String pushKey = mAddGuestDataRef.push().getKey();
                    String fullName = first_name + " " + last_name;

                    HashMap map = new HashMap();
                    map.put("first_name", first_name);
                    map.put("last_name", last_name);
                    map.put("full_name", fullName.toUpperCase()); //FULL NAME UPPERCASE
                    map.put("salutation", salutation);
                    map.put("email", email);
                    map.put("company", company);
                    map.put("occupation", occupation);
                    map.put("contact_number", contact_number);
                    map.put("expected_numbers", expected_numbers);
                    map.put("notes", notes);

                    mAddGuestDataRef.child(pushKey).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                pd.dismiss();
                                Intent uploadGuestImage = new Intent(AddGuestToEvent.this, UploadGuestImage.class);
                                uploadGuestImage.putExtra("push_key", pushKey);
                                uploadGuestImage.putExtra("event_key", event_key);
                                startActivity(uploadGuestImage);
                                finish();

                            }else {

                            }

                        }
                    });


                }else {
                    pd.dismiss();
                    StyleableToast.makeText(AddGuestToEvent.this, "Please Check above field and input all", R.style.loginErrorToastColor).show();
                }


            }
        });

    }

    private boolean checkInputField(String first_name, String last_name, String salutation, String email, String company, String occupation, String contact_number, String expected_numbers, String notes) {

        if(!first_name.equals("") && !last_name.equals("") && !salutation.equals("") &&!email.equals("") &&
                !company.equals("") && !occupation.equals("") && !contact_number.equals("") && !expected_numbers.equals("") &&
                !notes.equals("") ){

            return  true;

        }else {
            return false;
        }
    }
}
