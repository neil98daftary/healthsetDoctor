package in.ashnehete.healthsetdoctor.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.ashnehete.healthsetdoctor.R;

import static in.ashnehete.healthsetdoctor.AppConstants.RECORD_VALUE;

public class TemperatureActivity extends AppCompatActivity {

    @BindView(R.id.tv_temperature_c)
    TextView tvTemperatureC;

    @BindView(R.id.tv_temperature_f)
    TextView tvTemperatureF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        String value = bundle.getString(RECORD_VALUE, null);
        String password = "hea25lth07";
        String encryptedMsg = value;
        try {
            String messageAfterDecrypt = AESCrypt.decrypt(password, encryptedMsg);
            value = messageAfterDecrypt;
        }catch (GeneralSecurityException e){
            //handle error - could be due to incorrect password or tampered encryptedMsg
        }

        tvTemperatureC.setText(value);

        double valueC = Double.parseDouble(value.split(" ")[0]);
        double valueF = valueC * 9.0 / 5.0 + 32;

        tvTemperatureF.setText(String.valueOf(valueF) + " °F");
    }
}
