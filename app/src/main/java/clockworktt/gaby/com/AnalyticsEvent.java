package clockworktt.gaby.com;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AnalyticsEvent extends AppCompatActivity {

    private String event_key;
    private TextView mEventName;
    private TextView mEventDate;
    private TextView mEventTotalInvitedTickets;

    private DatabaseReference mEventDataRef;
    private FirebaseAuth mAuth;
    private String userId;
    private LinearLayout linearLayout;

    private RecyclerView mAnalyticsRecyclerView;

    private DatabaseReference mAnalyticsDataRef;

    //pdf code variable
    private String pdfName = "Event Details";
    Bitmap bitmap;
    boolean boolean_save, boolean_permission;
    private Button mDownloadPdf;

    private ProgressDialog progressDialog;
    public static int REQUEST_PERMISSIONS = 1;
    private RecyclerView mResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_event);

        getSupportActionBar().hide();
        event_key = getIntent().getStringExtra("event_key");

        mEventName = findViewById(R.id.analytics_event_name);
        mEventDate = findViewById(R.id.analytics_event_date);
        mEventTotalInvitedTickets = findViewById(R.id.analytics_event_invited);
        linearLayout = findViewById(R.id.analytics_linear_layout);
        mDownloadPdf = findViewById(R.id.download_pdf);
        mResultList = findViewById(R.id.analytics_recycler_view);

        mAnalyticsRecyclerView = findViewById(R.id.analytics_recycler_view);

        mAnalyticsRecyclerView.setHasFixedSize(true);
        mAnalyticsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mAnalyticsDataRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event_key).child("guest_list");

        mEventDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("event").child(event_key);
        mEventDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("title")){
                    String title = dataSnapshot.child("title").getValue().toString();
                    mEventName.setText("Event Name: " + title);
                    pdfName = title;
                }

                if(dataSnapshot.hasChild("date")){
                    String date = dataSnapshot.child("date").getValue().toString();
                    mEventDate.setText("Event Date: " + date);
                }

                if(dataSnapshot.hasChild("total_guests")){
                    String tickets = dataSnapshot.child("total_guests").getValue().toString();
                    mEventTotalInvitedTickets.setText("Total Guests: " + tickets);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mDownloadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fn_permission();

                if (boolean_save) {
                    Intent intent = new Intent(getApplicationContext(), AnalyticsEvent.class);
                    startActivity(intent);

                } else {
                    if (boolean_permission) {
                        progressDialog = new ProgressDialog(AnalyticsEvent.this);
                        progressDialog.setMessage("Please wait");
                        bitmap = getScreenshotFromRecyclerView(mResultList);
                        createPDF();
//                        saveBitmap(bitmap);
                    } else {

                    }

                }


                createPDF();

            }
        });

    }

    private void createPDF() {

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float hight = displaymetrics.heightPixels ;
        float width = displaymetrics.widthPixels ;

        int convertHighet = (int) hight, convertWidth = (int) width;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, 10000, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        canvas.drawPaint(paint);


        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHighet, true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0 , null);
        document.finishPage(page);

        // write the document content
        String targetPdf = "/sdcard/"+ pdfName + ".pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            boolean_save=true;
            mDownloadPdf.setText("Pdf Downloaded");
            mDownloadPdf.setCompoundDrawables(null,null,null, null);
            mDownloadPdf.setEnabled(false);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();


    }


    public Bitmap getScreenshotFromRecyclerView(RecyclerView view) {
        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {

                    bitmaCache.put(String.valueOf(i), drawingCache);
                }
//                holder.itemView.setDrawingCacheEnabled(false);
//                holder.itemView.destroyDrawingCache();
                height += holder.itemView.getMeasuredHeight();
            }

            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            bigCanvas.drawColor(Color.WHITE);

            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmaCache.get(String.valueOf(i));
                bigCanvas.drawBitmap(bitmap, 0f, iHeight, paint);
                iHeight += bitmap.getHeight();
                bitmap.recycle();
            }

        }
        return bigBitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                boolean_permission = true;


            } else {
                Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

            }
        }
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(AnalyticsEvent.this, android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(AnalyticsEvent.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }

            if ((ActivityCompat.shouldShowRequestPermissionRationale(AnalyticsEvent.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            } else {
                ActivityCompat.requestPermissions(AnalyticsEvent.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;


        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<AnalyticsDetails, AnalyticsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AnalyticsDetails, AnalyticsViewHolder>(

                AnalyticsDetails.class,
                R.layout.analytics_single_item,
                AnalyticsViewHolder.class,
                mAnalyticsDataRef.orderByChild("arrived").equalTo(true)

        ) {
            @Override
            protected void populateViewHolder(AnalyticsViewHolder viewHolder, AnalyticsDetails model, int position) {

                viewHolder.setGuestName(model.getFirst_name(), model.getLast_name());
                viewHolder.setTimeStamp(model.getTimestamp());


            }
        };

        mAnalyticsRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }


    public static class AnalyticsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public AnalyticsViewHolder(View itemView) {
            super(itemView);
            mView  = itemView;
        }

        public void setGuestName(String first_name, String last_name) {

            TextView guestName = mView.findViewById(R.id.analytics_guest_name);
            guestName.setText(first_name + " " + last_name);

        }

        public void setTimeStamp(long timestamp) {

            TextView arrival_time = mView.findViewById(R.id.analytics_arrival_time);

            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            arrival_time.setText("Arrived: " + sfd.format(new Date(timestamp)));

        }
    }
}
