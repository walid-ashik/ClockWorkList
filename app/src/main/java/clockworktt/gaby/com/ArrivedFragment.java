package clockworktt.gaby.com;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.EventLogTags;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.regex.Matcher;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArrivedFragment extends Fragment {


    private RecyclerView mArrivedRecyclerView;
    private DatabaseReference mGuestArrivedDataRef;
    private ProgressBar mArrivedProgressBar;

    private View mArrivedView;
    private String event_key;
    private int guestArrived = 0;

    public ArrivedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        event_key = getActivity().getIntent().getStringExtra("event_key");

        mArrivedView = inflater.inflate(R.layout.fragment_arrived, container, false);

        mArrivedRecyclerView = mArrivedView.findViewById(R.id.arrived_recycler_view);
        mArrivedProgressBar = mArrivedView.findViewById(R.id.arrived_guest_list_progress);

        mArrivedRecyclerView.setHasFixedSize(true);
        mArrivedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mGuestArrivedDataRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event_key).child("guest_list");
        return mArrivedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<GuestDetails,ArrivedGuestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<GuestDetails, ArrivedGuestViewHolder>(
                GuestDetails.class,
                R.layout.invited_single_guest_item,
                ArrivedGuestViewHolder.class,
                mGuestArrivedDataRef.orderByChild("arrived").equalTo(true)
        ) {
            @Override
            protected void populateViewHolder(final ArrivedGuestViewHolder viewHolder, GuestDetails model, int position) {

                mArrivedProgressBar.setVisibility(View.GONE);

                String name = model.getFirst_name() + " " + model.getLast_name();
                String guest_key = getRef(position).getKey();
                String image = model.getImage();

                Log.d("IMAGE", "model.getImage(): " + image);

                if(image != null){
                    image = model.getImage().toString();
                }


                final Bundle arrivedBundle = new Bundle();
                arrivedBundle.putString("event_key", event_key);
                arrivedBundle.putString("guest_key", guest_key);
                arrivedBundle.putString("name", name);
                arrivedBundle.putString("occupation", model.getOccupation());
                arrivedBundle.putString("company", model.getCompany());
                arrivedBundle.putString("image", image);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        Intent guestProfileIntent = new Intent(getContext(), GuestProfileActivity.class);
                        guestProfileIntent.putExtras(arrivedBundle);
                        startActivity(guestProfileIntent);

                    }
                });


                viewHolder.setGuestImage(getContext(), model.getImage());
                viewHolder.setGuestName(model.getFirst_name(), model.getLast_name());
                viewHolder.setGuestOccupationCompany(model.getOccupation(), model.getCompany());
                viewHolder.setArrivedCheckBox(event_key, guest_key, model.getArrived());
            }
        };

        mArrivedRecyclerView.setVisibility(View.VISIBLE);
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mArrivedRecyclerView.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int pollCount = firebaseRecyclerAdapter.getItemCount();
                Log.d("pollcount", "onItemRangeInserted: "+ pollCount);


                DatabaseReference recyclerdata = FirebaseDatabase.getInstance().getReference().child("arrived_count").child(event_key);
                recyclerdata.child("total_arrived").setValue(pollCount);
            }
        });

    }


    public static class ArrivedGuestViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView imageAnim;
        View mView;


        public ArrivedGuestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            CheckBox mArrivedCheckBox = mView.findViewById(R.id.invited_button_check);
            mArrivedCheckBox.setChecked(true);

        }

        public void setGuestName(String first_name, String last_name) {

            TextView guestName = mView.findViewById(R.id.invited_guest_name);
            guestName.setText(first_name + " " + last_name);

        }

        public void setGuestOccupationCompany(String occupation, String company) {

            TextView guestOccupation = mView.findViewById(R.id.invited_guest_occupation);
            guestOccupation.setText(occupation + " at " + company);
        }


        public void setGuestImage(Context context, String image) {

            CircleImageView guestImage = mView.findViewById(R.id.invited_guest_image);
            Picasso.with(context).load(image).placeholder(R.drawable.ic_avatar).into(guestImage);

        }


        public void setArrivedCheckBox(final String guest, final String single_guest_key, boolean arrived) {

            final CheckBox arrivedBox = mView.findViewById(R.id.invited_button_check);

            if(!arrivedBox.isChecked()){
                arrivedBox.setChecked(true);
            }

            arrivedBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatabaseReference mCheckData = FirebaseDatabase.getInstance().getReference().child("Events").child(guest)
                            .child("guest_list").child(single_guest_key);


                    if(!arrivedBox.isChecked()){
                        mCheckData.child("arrived").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.d("CHECK", "onComplete: " + "unchecked with arrived = false");
                                }
                            }
                        });
                    }



                }
            });

        }


    }

}
