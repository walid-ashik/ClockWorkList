package clockworktt.gaby.com;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class InvitedFragment extends Fragment {


    public InvitedFragment() {
        // Required empty public constructor
    }

    private RecyclerView mInvitedGuestsRecycler;
    private DatabaseReference mGuestDataRef;
    private ProgressBar mPrgoressBar;

    private View mInvitedView;
    private String event_key;

    private EditText mInvitedSearchEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mInvitedView = inflater.inflate(R.layout.fragment_invited, container, false);

        event_key = getActivity().getIntent().getStringExtra("event_key");

        mInvitedGuestsRecycler = mInvitedView.findViewById(R.id.invited_recycler_view);
        mPrgoressBar = mInvitedView.findViewById(R.id.invited_guest_list_progress);

        mInvitedSearchEditText = mInvitedView.findViewById(R.id.invited_search_edit_text);

        mInvitedGuestsRecycler.setHasFixedSize(true);
        mInvitedGuestsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        mGuestDataRef = FirebaseDatabase.getInstance().getReference().child("Events").child(event_key).child("guest_list");

        mInvitedSearchEditText.setInputType(
                InputType.TYPE_CLASS_TEXT|
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
        );

        mInvitedSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String text = editable.toString();
                searchByName(text, mInvitedGuestsRecycler);
                mInvitedSearchEditText.clearFocus();

            }
        });

        return mInvitedView;
    }



    private void searchByName(String search_name, RecyclerView mInvitedGuestsRecycler) {

        FirebaseRecyclerAdapter<GuestDetails, GuestsViewHolder> searchAdapter = new FirebaseRecyclerAdapter<GuestDetails, GuestsViewHolder>(
                GuestDetails.class,
                R.layout.invited_single_guest_item,
                GuestsViewHolder.class,
                mGuestDataRef.orderByChild("full_name").startAt(search_name).endAt(search_name + "\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(GuestsViewHolder viewHolder, GuestDetails model, int position) {

                String single_guest_key = getRef(position).getKey();

                String name = model.getFirst_name() + " " + model.getLast_name();
                String guest_key = getRef(position).getKey();


                final Bundle invitedBundle = new Bundle();
                invitedBundle.putString("event_key", event_key);
                invitedBundle.putString("guest_key", guest_key);
                invitedBundle.putString("name", name);
                invitedBundle.putString("occupation", model.getOccupation());
                invitedBundle.putString("company", model.getCompany());
                invitedBundle.putString("image", model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent guestProfileIntent = new Intent(getContext(), GuestProfileActivity.class);
                        guestProfileIntent.putExtras(invitedBundle);
                        startActivity(guestProfileIntent);

                    }
                });

                viewHolder.setGuestName(model.getFirst_name(), model.getLast_name());
                viewHolder.setGuestOccupationCompany(model.getOccupation(), model.getCompany());
                viewHolder.setGuestImage(getContext(), model.getImage());
                viewHolder.setArrivedCheckBox(event_key, single_guest_key, model.getArrived());

            }

        };

        mInvitedGuestsRecycler.setAdapter(searchAdapter);
        searchAdapter.notifyDataSetChanged();

    }

    @Override
    public void onStart() {
        super.onStart();


        final FirebaseRecyclerAdapter<GuestDetails, GuestsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<GuestDetails, GuestsViewHolder>(

                GuestDetails.class,
                R.layout.invited_single_guest_item,
                GuestsViewHolder.class,
                mGuestDataRef.orderByChild("full_name")
        ) {
            @Override
            protected void populateViewHolder(GuestsViewHolder viewHolder, GuestDetails model, int position) {

                mPrgoressBar.setVisibility(View.GONE);
                String single_guest_key = getRef(position).getKey();

                String name = model.getFirst_name() + " " + model.getLast_name();
                String guest_key = getRef(position).getKey();


                final Bundle invitedBundle = new Bundle();
                invitedBundle.putString("event_key", event_key);
                invitedBundle.putString("guest_key", guest_key);
                invitedBundle.putString("name", name);
                invitedBundle.putString("occupation", model.getOccupation());
                invitedBundle.putString("company", model.getCompany());
                invitedBundle.putString("image", model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent guestProfileIntent = new Intent(getContext(), GuestProfileActivity.class);
                        guestProfileIntent.putExtras(invitedBundle);
                        startActivity(guestProfileIntent);

                    }
                });

                viewHolder.setGuestName(model.getFirst_name(), model.getLast_name());
                viewHolder.setGuestOccupationCompany(model.getOccupation(), model.getCompany());
                viewHolder.setGuestImage(getContext(), model.getImage());
                viewHolder.setArrivedCheckBox(event_key, single_guest_key, model.getArrived());

            }


        };

        mInvitedGuestsRecycler.setVisibility(View.VISIBLE);
        mInvitedGuestsRecycler.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int pollCount = firebaseRecyclerAdapter.getItemCount();
                Log.d("pollcount", "onItemRangeInserted: "+ pollCount);


                DatabaseReference recyclerdata = FirebaseDatabase.getInstance().getReference().child("arrived_count").child(event_key);
                recyclerdata.child("total_invited").setValue(pollCount);
            }
        });

    }


    public static class GuestsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public GuestsViewHolder(View itemView) {
            super(itemView);


            mView = itemView;
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

        int arrivedCount = 0;

        public void setArrivedCheckBox(final String guest, final String single_guest_key, boolean arrived) {

            final CheckBox arrivedBox = mView.findViewById(R.id.invited_button_check);

            if(arrived == true){
                arrivedCount++;

                if(!arrivedBox.isChecked()){
                    arrivedBox.setChecked(true);
                }

            }

            if(arrived == false){
                if(arrivedBox.isChecked()){
                    arrivedBox.setChecked(false);
                }

            }



            arrivedBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatabaseReference mCheckData = FirebaseDatabase.getInstance().getReference().child("Events").child(guest)
                            .child("guest_list").child(single_guest_key);

                    if(arrivedBox.isChecked()){

                        Map map = new HashMap();
                        map.put("arrived", true);
                        map.put("timestamp", ServerValue.TIMESTAMP);

                        mCheckData.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                Log.d("TimeStamp", "onComplete: " + "true with timestamp");
                            }
                        });

//                        mCheckData.child("arrived").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()){
//                                    Log.d("CHECK", "onComplete: " + "Checked with arrived = true");
//
//                                }
//                            }
//                        });

                    }

                    if(!arrivedBox.isChecked()){


                        Map map = new HashMap();
                        map.put("arrived", false);
                        map.put("timestamp", ServerValue.TIMESTAMP);

                        mCheckData.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                Log.d("TimeStamp", "onComplete: " + "false with timestamp");
                            }
                        });


//                        mCheckData.child("arrived").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()){
//                                    Log.d("CHECK", "onComplete: " + "unchecked with arrived = false");
//
//                                }
//                            }
//                        });
                    }



                }
            });

        }
    }


}
