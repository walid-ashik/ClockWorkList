package clockworktt.gaby.com;

import android.*;
import android.Manifest;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.encoder.BarcodeMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;

public class QrCodeActivity extends AppCompatActivity {

    private ImageView mQrCodeView;
    private Button mDownloadCodeButton;

    private Bitmap bitmap;
    private DatabaseReference mEventDataRef;
    private DatabaseReference mAddEventKeyForScanner;
    private String event_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        final String event_key = getIntent().getStringExtra("event_key");

        mQrCodeView = findViewById(R.id.qr_image_view);
        mDownloadCodeButton = findViewById(R.id.qr_code_download_button);

        mAddEventKeyForScanner = FirebaseDatabase.getInstance().getReference().child("Scan").child(event_key).child("date");
        mAddEventKeyForScanner.setValue(ServerValue.TIMESTAMP);

        //generate qr code
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {

            BitMatrix bitMatrix = multiFormatWriter.encode(event_key, BarcodeFormat.QR_CODE, 400,400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            mQrCodeView.setImageBitmap(bitmap);
            
        }catch (WriterException e){
            e.printStackTrace();
        }

        mEventDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("event").child(event_key);

        mEventDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("title")){
                    event_name = dataSnapshot.child("title").getValue().toString();
                }else {
                    event_name = "Clock Work List";
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDownloadCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadQrCode(event_key);
            }
        });

    }

    private void downloadQrCode(String event_key) {

        ActivityCompat.requestPermissions(QrCodeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        //path to sd card
        File path = Environment.getExternalStorageDirectory();

        //create a folder
        File dir = new File(path + "/Clock Work List/");
        dir.mkdirs();

        File file = new File(dir, event_name+".png");

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Toast.makeText(QrCodeActivity.this, "Find Your File TO Storage/Clock Work List/ Folder", Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
