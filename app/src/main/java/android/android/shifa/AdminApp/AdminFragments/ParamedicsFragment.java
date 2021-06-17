package android.android.shifa.AdminApp.AdminFragments;

import android.android.shifa.ActivitiesAndFragments.DoctorsFragment;
import android.android.shifa.AdminApp.AdminDoctorsAndParamedicsDetailsActivity;
import android.android.shifa.Models.ParamedicModel;
import android.android.shifa.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParamedicsFragment extends Fragment {
    public final static String ADMIN_EXTRA_PARAMEDIC_KEY = "paramedic_key";
    View view;
    ImageView imageView;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<ParamedicModel, AdminParamedicsViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerAdapter<ParamedicModel, AdminParamedicsViewHolder> firebaseRecyclerAdapterSpecialty;
    Spinner specialization_spinner;
    Button filter_search_btn, clear_filter_btn;
    TextView filter_txt;
    String specialization_txt;
    RotateLoading rotateLoading;
    private NestedScrollView nested_scroll_view;
    private LinearLayout linearLayout;
    private LinearLayout lyt_expand_text;

    public static void nestedScrollTo(final NestedScrollView nested, final View targetView) {
        nested.post(new Runnable() {
            @Override
            public void run() {
                nested.scrollTo(500, targetView.getBottom());
            }
        });
    }

    public static void expand(final View v, final DoctorsFragment.AnimListener animListener) {
        Animation a = expandAction(v);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animListener.onFinish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private static Animation expandAction(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targtetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targtetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
        return a;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.admin_paramedics_fragment, container, false);

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

        initComponent();

        specialization_spinner = view.findViewById(R.id.specialization_spinner);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(),
                R.array.hospitals, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        specialization_spinner.setAdapter(adapter1);

        specialization_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                specialization_txt = String.valueOf(parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void DisplayallDoctors() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("AllUsers")
                .child("Paramedics")
                .limitToLast(50);

        FirebaseRecyclerOptions<ParamedicModel> options =
                new FirebaseRecyclerOptions.Builder<ParamedicModel>()
                        .setQuery(query, ParamedicModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ParamedicModel, AdminParamedicsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AdminParamedicsViewHolder holder, int position, @NonNull final ParamedicModel model) {
                rotateLoading.stop();

                final String key = getRef(position).getKey();

                holder.BindPlaces(model);

                Picasso.get()
                        .load(model.getImageurl())
                        .placeholder(R.drawable.logo2)
                        .error(R.drawable.logo2)
                        .into(holder.doctor_picture);

                holder.doctor_mobile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialContactPhone(model.getMobilenumber());
                    }
                });

                holder.doctor_details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AdminDoctorsAndParamedicsDetailsActivity.class);
                        intent.putExtra(ADMIN_EXTRA_PARAMEDIC_KEY, key);
                        startActivity(intent);
                    }
                });

                holder.view_profile_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AdminDoctorsAndParamedicsDetailsActivity.class);
                        intent.putExtra(ADMIN_EXTRA_PARAMEDIC_KEY, key);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public AdminParamedicsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.admin_item, parent, false);
                return new AdminParamedicsViewHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        rotateLoading.stop();
    }

    private void DisplayallDoctorsbySpecialty(String specialty) {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Paramedics")
                .child(specialty)
                .limitToLast(50);

        FirebaseRecyclerOptions<ParamedicModel> options =
                new FirebaseRecyclerOptions.Builder<ParamedicModel>()
                        .setQuery(query, ParamedicModel.class)
                        .build();

        firebaseRecyclerAdapterSpecialty = new FirebaseRecyclerAdapter<ParamedicModel, AdminParamedicsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AdminParamedicsViewHolder holder, int position, @NonNull final ParamedicModel model) {
                rotateLoading.stop();

                final String key = getRef(position).getKey();

                holder.BindPlaces(model);

                Picasso.get()
                        .load(model.getImageurl())
                        .placeholder(R.drawable.logo2)
                        .error(R.drawable.logo2)
                        .into(holder.doctor_picture);

                holder.doctor_mobile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialContactPhone(model.getMobilenumber());
                    }
                });

                holder.doctor_details.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AdminDoctorsAndParamedicsDetailsActivity.class);
                        intent.putExtra(ADMIN_EXTRA_PARAMEDIC_KEY, key);
                        startActivity(intent);
                    }
                });

                holder.view_profile_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), AdminDoctorsAndParamedicsDetailsActivity.class);
                        intent.putExtra(ADMIN_EXTRA_PARAMEDIC_KEY, key);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public AdminParamedicsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.admin_item, parent, false);
                return new AdminParamedicsViewHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapterSpecialty);
        rotateLoading.stop();
    }

    private void initComponent() {
        imageView = view.findViewById(R.id.toggle_image);
        linearLayout = view.findViewById(R.id.toggle_lin);
        lyt_expand_text = view.findViewById(R.id.lyt_expand_text);
        filter_search_btn = view.findViewById(R.id.filter_search_btn);
        clear_filter_btn = view.findViewById(R.id.clear_filter_btn);
        filter_txt = view.findViewById(R.id.filter_txt);
        lyt_expand_text.setVisibility(View.GONE);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSectionText(imageView);
            }
        });

        clear_filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseRecyclerAdapterSpecialty != null) {
                    toggleSectionText(imageView);

                    firebaseRecyclerAdapterSpecialty.stopListening();

                    DisplayallDoctors();

                    firebaseRecyclerAdapter.startListening();

                    filter_txt.setText("Filter");

                    firebaseRecyclerAdapterSpecialty = null;
                } else {
                    Toast.makeText(getContext(), "there's no filter to clear", Toast.LENGTH_SHORT).show();
                }
            }
        });

        filter_search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseRecyclerAdapterSpecialty != null || specialization_txt.length() != 0) {
                    if (specialization_txt.length() != 0) {
                        toggleSectionText(imageView);

                        firebaseRecyclerAdapter.stopListening();

                        DisplayallDoctorsbySpecialty(specialization_txt);

                        firebaseRecyclerAdapterSpecialty.startListening();

                        filter_txt.setText("Filter by Work Place (" + specialization_txt + ")");
                    } else {
                        Toast.makeText(getContext(), "please select filter to search", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "please select filter to search", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // nested scrollview
        nested_scroll_view = view.findViewById(R.id.nested_scroll_view);
    }

    private void toggleSectionText(View view) {
        boolean show = toggleArrow(view);
        if (show) {
            expand(lyt_expand_text, new DoctorsFragment.AnimListener() {
                @Override
                public void onFinish() {
                    nestedScrollTo(nested_scroll_view, lyt_expand_text);
                }
            });
        } else {
            collapse(lyt_expand_text);
        }
    }

    public boolean toggleArrow(View view) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(200).rotation(180);
            return true;
        } else {
            view.animate().setDuration(200).rotation(0);
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.startListening();
        }

        if (firebaseRecyclerAdapterSpecialty != null) {
            firebaseRecyclerAdapterSpecialty.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }

        if (firebaseRecyclerAdapterSpecialty != null) {
            firebaseRecyclerAdapterSpecialty.stopListening();
        }
    }

    private void dialContactPhone(final String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }

    public interface AnimListener {
        void onFinish();
    }

    public static class AdminParamedicsViewHolder extends RecyclerView.ViewHolder {
        ImageView doctor_mobile;
        CircleImageView doctor_picture;
        TextView doctor_name, doctor_specailty;
        MaterialRippleLayout doctor_details;
        Button view_profile_btn;

        AdminParamedicsViewHolder(View itemView) {
            super(itemView);

            doctor_picture = itemView.findViewById(R.id.doctor_profile_picture);
            doctor_mobile = itemView.findViewById(R.id.phonenumber_btn);
            doctor_name = itemView.findViewById(R.id.doctor_fullname);
            doctor_specailty = itemView.findViewById(R.id.doctor_specialty);
            doctor_details = itemView.findViewById(R.id.details_btn);
            view_profile_btn = itemView.findViewById(R.id.view_profile_btn);
        }

        void BindPlaces(final ParamedicModel paramedicModel) {
            doctor_name.setText(paramedicModel.getFullname());
            doctor_specailty.setText(paramedicModel.getWork_place());
        }
    }
}
