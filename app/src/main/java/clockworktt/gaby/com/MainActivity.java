package clockworktt.gaby.com;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mEventRecyclerView;
    private DatabaseReference mEventDatabaseRef;

    private ProgressBar mProgressBar;
    private DrawerLayout drawer;

    private FirebaseAuth mAuth;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goAddEventActivity();

            }
        });


        mEventRecyclerView = findViewById(R.id.event_list_recycler_view);


        mEventRecyclerView.setHasFixedSize(true);
        mEventRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressBar = findViewById(R.id.progress_bar);


        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        final TextView mName = headerView.findViewById(R.id.nav_header_name);
        TextView mEmail = headerView.findViewById(R.id.nav_header_email);
        LinearLayout editProfile = headerView.findViewById(R.id.nav_header_edit_linearlayout);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                drawer.closeDrawer(GravityCompat.START);
                showAlertDialogToChangeName();

            }
        });


        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String email = user.getEmail();
            mEmail.setText(email);

            userId = user.getUid();
            mEventDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("event");
            addHeaderNameifHave(mName, mEmail, userId);
        }


    }//end of onCreate()

    private void showAlertDialogToChangeName() {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View changeNameView = getLayoutInflater().inflate(R.layout.edit_name_layout, null);
        final EditText changeNameEditText = changeNameView.findViewById(R.id.set_your_name_edit_text);
        Button updateYourNameButton = changeNameView.findViewById(R.id.update_name_button);

        mBuilder.setView(changeNameView);
        final AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();

        updateYourNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = changeNameEditText.getText().toString();

                if(!TextUtils.isEmpty(name)){

                    DatabaseReference userNameDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                    userNameDataRef.child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            alertDialog.dismiss();

                        }
                    });

                }else {
                    Toast.makeText(MainActivity.this, "Please Enter Your Name", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void addHeaderNameifHave(final TextView mName, TextView mEmail, String userId) {

        DatabaseReference mUserProfileRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mUserProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("name")){
                    mName.setText(dataSnapshot.child("name").getValue().toString());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout) {

            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                FirebaseAuth.getInstance().signOut();
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_new_event) {

            goAddEventActivity();

        } else if (id == R.id.nav_your_events) {

            drawer.closeDrawer(GravityCompat.START);

        }  else if (id == R.id.nav_logout) {

            if(FirebaseAuth.getInstance().getCurrentUser() != null){
                FirebaseAuth.getInstance().signOut();
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void goAddEventActivity() {

        Intent addNewEventActivity = new Intent(MainActivity.this, AddEventActivity.class);
        startActivity(addNewEventActivity);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Checking If USER not logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent registerUser = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(registerUser);
            finish();
        }else{

            FirebaseRecyclerAdapter<EventDetails, EventsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<EventDetails, EventsViewHolder>(

                    EventDetails.class,
                    R.layout.single_event_item,
                    EventsViewHolder.class,
                    mEventDatabaseRef.orderByChild("title")

            ) {
                @Override
                protected void populateViewHolder(EventsViewHolder viewHolder, EventDetails model, int position) {

                    viewHolder.setEventDate(model.getDate());
                    viewHolder.setEventIcon(getApplicationContext(),model.getIcon());
                    viewHolder.setEventTitle(model.getTitle());
                    viewHolder.setEventDescription(model.getDescription());
                    viewHolder.setEventTotalGuest(model.getTotal_guests());

                    mProgressBar.setVisibility(View.GONE);

                    final String event_key = getRef(position).getKey();

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent guestListIntent = new Intent(MainActivity.this, GuestListActivity.class);
                            guestListIntent.putExtra("event_key",event_key);
                            startActivity(guestListIntent);

                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                        }
                    });

                }
            };

            mEventRecyclerView.setVisibility(View.VISIBLE);
            mEventRecyclerView.setAdapter(firebaseRecyclerAdapter);
        }


    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public EventsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setEventDate(String date) {

            TextView mEventDate = mView.findViewById(R.id.single_event_date);
            mEventDate.setText(date);

        }

        public void setEventIcon(Context context, String icon) {

            CircleImageView mEventIcon = mView.findViewById(R.id.single_event_icon);
            Picasso.with(context)
                    .load(icon)
                    .placeholder(R.drawable.app_icon_circle)
                    .into(mEventIcon);

        }

        public void setEventTitle(String title) {

            TextView mEventTitle = mView.findViewById(R.id.single_event_title);
            mEventTitle.setText(title);

        }

        public void setEventDescription(String description) {

            TextView mEventDescription = mView.findViewById(R.id.single_event_description);
            mEventDescription.setText(description);
        }

        public void setEventTotalGuest(String total_guests) {

            TextView mEventTotal = mView.findViewById(R.id.single_total);
            mEventTotal.setText("Total: "+total_guests);
        }
    }
}
