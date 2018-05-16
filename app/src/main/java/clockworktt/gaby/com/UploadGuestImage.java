package clockworktt.gaby.com;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class UploadGuestImage extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private String guest_push_key;
    private String event_push_key;

    private CircleImageView mUploadImageButton;
    private ProgressDialog progressDialog;

    private StorageReference mGuestImage;
    private DatabaseReference mGuestDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_guest_image);

        guest_push_key = getIntent().getStringExtra("push_key");
        event_push_key = getIntent().getStringExtra("event_key");

        Log.d("MENU", "onCreate: key: " +guest_push_key.toString());

        mUploadImageButton = findViewById(R.id.single_guest_profile_image);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        mGuestImage = FirebaseStorage.getInstance().getReference().child("guest_images");
        mGuestDataRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event_push_key).child("guest_list").child(guest_push_key);

        mUploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Choose Your Image"), GALLERY_PICK);

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
                StorageReference imagePath = mGuestImage.child(guest_push_key + ".jpg");
                final StorageReference thumbIconPath = mGuestImage.child("guest_thumb_images").child(guest_push_key + ".jpg");

                final UploadTask uploadTask = thumbIconPath.putBytes(thumb_icon_data);

                imagePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        final String imageDownloadUri = task.getResult().getDownloadUrl().toString();

                        if(task.isSuccessful()){

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()){

                                        String thumbImageDownloadUri = task.getResult().getDownloadUrl().toString();

                                        Map map = new HashMap();
                                        map.put("image", thumbImageDownloadUri);

                                        mGuestDataRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    //Icon Uploaded to FirebaseStorage
                                                    Intent guestListIntent = new Intent(UploadGuestImage.this, GuestListActivity.class);
                                                    guestListIntent.putExtra("event_key", event_push_key);
                                                    startActivity(guestListIntent);
                                                    finish();

                                                }else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(UploadGuestImage.this, "Uploading Fail...please try again!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                }
                            });

                        }else {

                            progressDialog.dismiss();
                            Toast.makeText(UploadGuestImage.this, "Uploading Fail...please try again!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }

        }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
            Toast.makeText(this, "CROP IMAGE ACTIVITY GOT AN ERROR!", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

}
