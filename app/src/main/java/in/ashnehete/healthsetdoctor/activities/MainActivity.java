package in.ashnehete.healthsetdoctor.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.ashnehete.healthsetdoctor.R;
import in.ashnehete.healthsetdoctor.models.User;

import static in.ashnehete.healthsetdoctor.AppConstants.USER_ID;
import static in.ashnehete.healthsetdoctor.AppConstants.USER_NAME;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @BindView(R.id.recycler_patients)
    RecyclerView recyclerPatients;

    PatientsAdapter patientsAdapter;
    List<User> patients = new ArrayList<>();
    List<String> patient_ids = new ArrayList<>();
    DatabaseReference mDatabase;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerPatients.setLayoutManager(mLayoutManager);
        recyclerPatients.setItemAnimator(new DefaultItemAnimator());

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase.orderByChild("doctor/id").equalTo(user.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, dataSnapshot.toString());
                        for (DataSnapshot patientSnapshot : dataSnapshot.getChildren()) {
                            User patient = patientSnapshot.getValue(User.class);
                            patients.add(patient);
                            patient_ids.add(patientSnapshot.getKey());
                        }

                        patientsAdapter = new PatientsAdapter(patients);
                        recyclerPatients.setAdapter(patientsAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.toString());
                    }
                }
        );

    }

    public void openManual(View view){
        Intent myIntent = new Intent(MainActivity.this, ManualECG.class);
        MainActivity.this.startActivity(myIntent);
    }

    public class PatientsAdapter extends RecyclerView.Adapter<PatientViewHolder> {

        List<User> patients;

        public PatientsAdapter(List<User> patients) {
            this.patients = patients;
        }

        @Override
        public PatientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_patient, parent, false);

            return new PatientViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PatientViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: " + position);
            final User patient = patients.get(position);
            final String patient_id = patient_ids.get(position);

            String password = "hea25lth07";
            String encryptedMsg = patient.getName();
            try {
                String messageAfterDecrypt = AESCrypt.decrypt(password, encryptedMsg);
                encryptedMsg = messageAfterDecrypt;
            }catch (GeneralSecurityException e){
                //handle error - could be due to incorrect password or tampered encryptedMsg
            }

            holder.tvPatientName.setText(encryptedMsg);
            String details = "Age: " + patient.getAge() +
                    "\tWeight: " + patient.getWeight() +
                    "\tHeight: " + patient.getHeight();
            holder.tvPatientDetails.setText(details);
            final String finalEncryptedMsg = encryptedMsg;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(USER_ID, patient_id);
                    bundle.putString(USER_NAME, finalEncryptedMsg);
                    Intent intent = new Intent(MainActivity.this, RecordsActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return patients.size();
        }
    }

    public class PatientViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_patient_name)
        TextView tvPatientName;

        @BindView(R.id.tv_patient_details)
        TextView tvPatientDetails;

        public PatientViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
