package clockworktt.gaby.com;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.channels.FileLock;
import java.util.ArrayList;

public class GuestListActivity extends AppCompatActivity {

    private static final String TAG = "GUESTCOUNT";
    private Toolbar mToolbar;
    private ViewPager mViewPager;

    private SectionPagerAdapter sectionPagerAdapter;
    private TabLayout mTabLayout;
    private String event_key;

    int arrivedGuest = 0;
    int invitedGuest = 0;

    String arrived;
    String invited;

    private DatabaseReference mEventDetailsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_list);

        event_key = getIntent().getStringExtra("event_key");

        mToolbar = findViewById(R.id.guest_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("  Guest Lists");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager = findViewById(R.id.guest_pager);

        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(sectionPagerAdapter);

        mTabLayout = findViewById(R.id.guest_page_tablayout);
        mTabLayout.setupWithViewPager(mViewPager);

        mEventDetailsRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event_key).child("guest_list");

        FetchSummeryContent(mEventDetailsRef);

        DatabaseReference eventSummeryData = FirebaseDatabase.getInstance().getReference().child("arrived_count").child(event_key);
        eventSummeryData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("total_arrived")) {
                    arrived = dataSnapshot.child("total_arrived").getValue().toString();
                    Log.d("PIECHART", "onDataChange: arrived: " + arrived);
                }else
                    arrived = "";

                if(dataSnapshot.hasChild("total_invited")){
                    invited = dataSnapshot.child("total_invited").getValue().toString();
                    Log.d("PIECHART", "onDataChange: invited: " + invited);
                }else
                    invited = "";

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void FetchSummeryContent(DatabaseReference mEventDetailsRef) {
        mEventDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    String guest_key = snap.getKey().toString();
                    countArrived(guest_key);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void countArrived(String guest_key) {

        mEventDetailsRef.child(guest_key).orderByChild("arrived").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getChildren().equals(true)){
                    arrivedGuest++;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.d(TAG, "onCreate: arrived: " + arrivedGuest);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.guest_page_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.add_guest){

            Intent addGuestIntent = new Intent(GuestListActivity.this, AddGuestToEvent.class);
            addGuestIntent.putExtra("event_key", event_key);
            startActivity(addGuestIntent);
            finish();

        }else if(item.getItemId() == R.id.summery_of_event){

            showSummery();

        }else if(item.getItemId() == R.id.action_qr_generator){

            Intent qrIntent = new Intent(GuestListActivity.this, QrCodeActivity.class);
            qrIntent.putExtra("event_key", event_key);
            startActivity(qrIntent);

        }else if(item.getItemId() == R.id.action_edit_event){

            Intent editEventIntent = new Intent(GuestListActivity.this, EditEventActivity.class);
            editEventIntent.putExtra("event_key", event_key);
            startActivity(editEventIntent);

        }else if(item.getItemId() == R.id.action_analytics){

            Intent analyticsEventIntent = new Intent(GuestListActivity.this, AnalyticsEvent.class);
            analyticsEventIntent.putExtra("event_key", event_key);
            startActivity(analyticsEventIntent);

        }else if(item.getItemId() == R.id.action_delete_event){

            deleteThisEvent();

        }else if(item.getItemId() == R.id.action_guests_from_code){

            Intent guestFromQrCodeActivity = new Intent(GuestListActivity.this, GuestsFromCodeActivity.class);
            guestFromQrCodeActivity.putExtra("event_key", event_key);
            startActivity(guestFromQrCodeActivity);

        }

        return true;
    }

    DatabaseReference mDeleteEventUsersChildRef;
    DatabaseReference mEventsDataRef;
    DatabaseReference mScanEventDataRef;
    DatabaseReference mArrivedCountEventRef;

    private void deleteThisEvent() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDeleteEventUsersChildRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userId).child("event").child(event_key);

        mEventsDataRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event_key);

        //check first if has this event child in SCAN
        mScanEventDataRef = FirebaseDatabase.getInstance().getReference().child("Scan");

        mArrivedCountEventRef = FirebaseDatabase.getInstance().getReference().child("arrived_count");

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Delete this event?")
                .setMessage("are you sure want to delete this event?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        deleteAllEventDetails(mDeleteEventUsersChildRef, mEventsDataRef, mScanEventDataRef);
                        deleteArrivedCountDataRef(mArrivedCountEventRef);
                        dialogInterface.dismiss();
                        sendMainActivity();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

    }

    private void deleteArrivedCountDataRef(DatabaseReference mArrivedCountEventRef) {

        mArrivedCountEventRef.child(event_key).removeValue();

    }

    private void sendMainActivity() {

        finish();

    }

    private void deleteAllEventDetails(DatabaseReference mDeleteEventUsersChildRef, final DatabaseReference mEventsDataRef, final DatabaseReference mScanEventDataRef) {

        //will delete mDeleteEventUsersChildRef then after successful will delete
        //mEventsDataRef and then will check if mScanEventDataRef has event_key child
        //if has then will delete the child event_key otherwise nothing will happen

        mDeleteEventUsersChildRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        mEventsDataRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    mScanEventDataRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.hasChild(event_key)) {
                                                mScanEventDataRef.child(event_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            Log.d(TAG, "onComplete: event deleted successfully");


                                                        }

                                                    }
                                                });
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });

                    }
            }
        });

    }


    private void showSummery() {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(GuestListActivity.this);
        View summeryView = getLayoutInflater().inflate(R.layout.summery_layout, null);

        mBuilder.setView(summeryView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();

        TextView mSummeryInvited = summeryView.findViewById(R.id.pie_chart_total_invited_text_view);
        TextView mSummeryArrived = summeryView.findViewById(R.id.pie_chart_total_arrived_text_view);

        PieChart pieChart = summeryView.findViewById(R.id.pie_chart);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(0,0,0,0);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        if(!arrived.equals("") && !invited.equals("")){
            Float arrivedFloat = Float.valueOf(arrived);
            Float notArrivedFloat = Float.valueOf(invited) - Float.valueOf(arrived);

            mSummeryArrived.setText("Arrived: " + arrived);
            mSummeryInvited.setText("Invited: " + invited);

            ArrayList<PieEntry> yValues = new ArrayList<>();
            yValues.add(new PieEntry(arrivedFloat, "Arrived"));
            yValues.add(new PieEntry(notArrivedFloat, "Absent"));

            pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic);

            PieDataSet dataSet = new PieDataSet(yValues, "");
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(2f);
            dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

            PieData data = new PieData(dataSet);
            data.setValueTextSize(17f);
            data.setValueTextColor(Color.WHITE);

            pieChart.setData(data);

        }


    }

    @Override
    protected void onStart() {
        super.onStart();

    }


}
