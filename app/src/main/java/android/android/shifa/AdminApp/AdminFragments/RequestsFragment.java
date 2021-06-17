package android.android.shifa.AdminApp.AdminFragments;

import android.android.shifa.AdminApp.AdminPatientsDetailsActivity;
import android.android.shifa.Models.LocationModel;
import android.android.shifa.R;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.victor.loading.rotate.RotateLoading;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {
    public final static String ADMIN_EXTRA_REQUEST_KEY = "request_key";
    public static String uri;
    View view;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    FirebaseRecyclerAdapter<LocationModel, AdminRequestsViewHolder> firebaseRecyclerAdapter;
    RotateLoading rotateLoading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.admin_requests_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = view.findViewById(R.id.doctors_recyclerview);
        rotateLoading = view.findViewById(R.id.rotateloading);

        rotateLoading.start();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        DisplayallDoctors();
    }

    private void DisplayallDoctors() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("AllRequests")
                .limitToLast(50);

        FirebaseRecyclerOptions<LocationModel> options =
                new FirebaseRecyclerOptions.Builder<LocationModel>()
                        .setQuery(query, LocationModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<LocationModel, AdminRequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AdminRequestsViewHolder holder, int position, @NonNull final LocationModel model) {
                rotateLoading.stop();

                final String key2 = model.getNfc_id();
                final String key = getRef(position).getKey();

                holder.BindPlaces(model);

                holder.doctor_mobile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialContactPhone(model.getEmergency());
                    }
                });


                holder.doctor_details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AdminPatientsDetailsActivity.class);
                        intent.putExtra(ADMIN_EXTRA_REQUEST_KEY, key2);
                        startActivity(intent);
                    }
                });


                holder.view_profile_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AdminPatientsDetailsActivity.class);
                        intent.putExtra(ADMIN_EXTRA_REQUEST_KEY, key2);
                        startActivity(intent);
                    }
                });


                //holder.view_profile_btn.setVisibility(View.GONE);
                holder.doctor_mobile.setVisibility(View.GONE);
                //   holder.doctor_details.setVisibility(View.GONE);

                holder.remove_request.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteDialog(key);
                    }
                });

                holder.doctor_picture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String latitude = model.getLatitude();
                        String longitude = model.getLongitude();
                        String name = model.getName();

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + latitude + "," + longitude + "(" + name + ")"));
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public AdminRequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.request_item, parent, false);
                return new AdminRequestsViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        rotateLoading.stop();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }
    }

    private void dialContactPhone(final String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }

    private void showDeleteDialog(final String keyy) {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.delete_user_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView title = dialog.findViewById(R.id.title_delete);
        TextView info = dialog.findViewById(R.id.info_delete);

        Button yes_btn = dialog.findViewById(R.id.yes_btn);
        Button cancel_btn = dialog.findViewById(R.id.cancel_btn);

        title.setText("Delete Request");
        info.setText("Are You Sure to Delete This Request");

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("AllRequests").child(keyy).removeValue();

                Toast.makeText(getContext(), "Request Removed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public static class AdminRequestsViewHolder extends RecyclerView.ViewHolder {
        ImageView doctor_mobile, remove_request;
        CircleImageView doctor_picture;
        TextView doctor_name, doctor_specailty, disease_txt, notes_txt;
        MaterialRippleLayout doctor_details;
        Button view_profile_btn;

        AdminRequestsViewHolder(View itemView) {
            super(itemView);

            doctor_picture = itemView.findViewById(R.id.doctor_profile_picture);
            doctor_mobile = itemView.findViewById(R.id.phonenumber_btn);
            remove_request = itemView.findViewById(R.id.delete_request);
            doctor_name = itemView.findViewById(R.id.doctor_fullname);
            doctor_specailty = itemView.findViewById(R.id.doctor_specialty);
            doctor_details = itemView.findViewById(R.id.details_btn);
            view_profile_btn = itemView.findViewById(R.id.view_profile_btn);
            disease_txt = itemView.findViewById(R.id.disease_txt);
            notes_txt = itemView.findViewById(R.id.note_txt);
        }

        void BindPlaces(final LocationModel locationModel) {
            doctor_name.setText(locationModel.getName());
            doctor_specailty.setText(locationModel.getNfc_id());
            disease_txt.setText(locationModel.getText());
            notes_txt.setText(locationModel.getNotes());
        }
    }
}
