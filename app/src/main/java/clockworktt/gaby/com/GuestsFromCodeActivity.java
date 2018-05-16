package clockworktt.gaby.com;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class GuestsFromCodeActivity extends AppCompatActivity {

    private String event_key;
    private DatabaseReference mScanEventGuestListRef;

    private RecyclerView mScannedGuestListRecyclerView;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guests_from_code);

        event_key = getIntent().getStringExtra("event_key");
        actionBar = getSupportActionBar();
        actionBar.setTitle("  Guests From Code");

        mScannedGuestListRecyclerView = findViewById(R.id.scanned_guest_list_recycler_view);
        mScannedGuestListRecyclerView.setHasFixedSize(true);
        mScannedGuestListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mScanEventGuestListRef = FirebaseDatabase.getInstance().getReference().child("Scan");
        mScanEventGuestListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(event_key)){
                    fetchEventList(mScanEventGuestListRef.child(event_key));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    int scannedGuestArrivedTotal = 0;

    private void fetchEventList(DatabaseReference ScanGuestListRef) {
        ScanGuestListRef = ScanGuestListRef.child("guest_list");


        final FirebaseRecyclerAdapter<ScannedGuestDetails, ScannedGuestsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ScannedGuestDetails, ScannedGuestsViewHolder>(

                ScannedGuestDetails.class,
                R.layout.single_scanned_guest_item,
                ScannedGuestsViewHolder.class,
                ScanGuestListRef

        ) {
            @Override
            protected void populateViewHolder(ScannedGuestsViewHolder viewHolder, final ScannedGuestDetails model, int position) {

                viewHolder.setGuestName(model.getFirst_name(),model.getLast_name());
                viewHolder.setGuestImage(model.getImage(), getApplicationContext(), model.getFb_link(), model.getFb_username());
                viewHolder.setArrivedTimeStamp(model.getTimestamp());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String fb_link = model.getFb_link();

                        if(!fb_link.equals("null")){
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(fb_link));
                                startActivity(intent);
                            } catch (Exception e) {
                                Intent intent =  new Intent(Intent.ACTION_VIEW, Uri.parse(fb_link));
                                startActivity(intent);
                            }
                        }else {
                            Toast.makeText(GuestsFromCodeActivity.this, "User Not Connected With Facebook", Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }
        };

        mScannedGuestListRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                scannedGuestArrivedTotal = firebaseRecyclerAdapter.getItemCount();
                DatabaseReference scannedArrivedTotalDataRef = FirebaseDatabase.getInstance().getReference().child("arrived_count").child(event_key);
                scannedArrivedTotalDataRef.child("scanned_arrived").setValue(scannedGuestArrivedTotal);
                actionBar.setSubtitle("  Total guests from QR code: " + scannedGuestArrivedTotal);

            }
        });


    }



    public static class ScannedGuestsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ScannedGuestsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setGuestName(String first_name, String last_name) {

            TextView name = mView.findViewById(R.id.scanned_guest_name);
            name.setText(first_name + " " + last_name);

        }

        public void setGuestImage(String image, Context applicationContext, String fbLink, final String fb_link) {

            CircleImageView guestImage  = mView.findViewById(R.id.scanned_guest_image);
            Picasso.with(applicationContext)
                    .load(image)
                    .placeholder(R.drawable.ic_avatar)
                    .into(guestImage);


        }

        public void setArrivedTimeStamp(long timestamp) {

            TextView arrivalTime = mView.findViewById(R.id.scanned_guest_arrival_time);
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            arrivalTime.setText("Arrived at " + sfd.format(new Date(timestamp)));

        }
    }
}
