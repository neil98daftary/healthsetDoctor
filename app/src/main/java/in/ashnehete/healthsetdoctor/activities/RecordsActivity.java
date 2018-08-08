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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.ashnehete.healthsetdoctor.R;
import in.ashnehete.healthsetdoctor.models.Record;

import static in.ashnehete.healthsetdoctor.AppConstants.BEND;
import static in.ashnehete.healthsetdoctor.AppConstants.ECG;
import static in.ashnehete.healthsetdoctor.AppConstants.GSR;
import static in.ashnehete.healthsetdoctor.AppConstants.PULSE;
import static in.ashnehete.healthsetdoctor.AppConstants.RECORD_VALUE;
import static in.ashnehete.healthsetdoctor.AppConstants.TEMPERATURE;
import static in.ashnehete.healthsetdoctor.AppConstants.USER_ID;
import static in.ashnehete.healthsetdoctor.AppConstants.USER_NAME;

public class RecordsActivity extends AppCompatActivity {

    public static final String TAG = "RecordsActivity";

    @BindView(R.id.recycler_records)
    RecyclerView recyclerRecords;

    RecordsAdapter recordsAdapter;
    List<Record> records = new ArrayList<>();
    DatabaseReference mDatabase;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        String userId = bundle.getString(USER_ID);
        String userName = bundle.getString(USER_NAME);

        getSupportActionBar().setTitle("Records for " + userName);
        Log.d(TAG, userId + " " + userName);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerRecords.setLayoutManager(mLayoutManager);
        recyclerRecords.setItemAnimator(new DefaultItemAnimator());

        mDatabase = FirebaseDatabase.getInstance().getReference().child("records").child(userId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.toString());
                for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()) {
                    Record record = recordSnapshot.getValue(Record.class);
                    records.add(record);
                }

                recordsAdapter = new RecordsAdapter(records);
                recyclerRecords.setAdapter(recordsAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        recordsAdapter = new RecordsAdapter(records);
        recyclerRecords.setAdapter(recordsAdapter);
    }

    public class RecordsAdapter extends RecyclerView.Adapter<RecordsActivity.RecordViewHolder> {

        List<Record> records;

        public RecordsAdapter(List<Record> records) {
            this.records = records;
        }

        @Override
        public RecordsActivity.RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_record, parent, false);

            return new RecordsActivity.RecordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecordsActivity.RecordViewHolder holder, int position) {
            final Record record = records.get(position);
            holder.tvRecordName.setText(record.getDevice());
            holder.tvRecordDetails.setText(record.getTimestamp());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(RECORD_VALUE, record.getValue());
                    Class deviceClass = null;
                    switch (record.getDevice()) {
                        case TEMPERATURE:
                            deviceClass = TemperatureActivity.class;
                            break;

                        case PULSE:
                            deviceClass = PulseActivity.class;
                            break;

                        case BEND:
                            deviceClass = BendActivity.class;
                            break;

                        case GSR:
                            deviceClass = GsrActivity.class;
                            break;

                        case ECG:
                            deviceClass = EcgActivity.class;
                            break;
                    }
                    if (deviceClass != null) {
                        Intent intent = new Intent(RecordsActivity.this, deviceClass);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return records.size();
        }
    }

    public class RecordViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_record_name)
        TextView tvRecordName;

        @BindView(R.id.tv_record_details)
        TextView tvRecordDetails;

        public RecordViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
