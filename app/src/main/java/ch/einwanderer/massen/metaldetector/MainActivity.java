package ch.einwanderer.massen.metaldetector;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Sensor mMagnetometer;
    private SensorManager mSensorManager;
    private final int MAX_DATA_POINTS = 3;
    private List<Double> mPowerList = new ArrayList<>(MAX_DATA_POINTS);
    private ProgressBar pbPower;
    private Button btSubmit;
    private final int SCAN_QR_CODE = 159;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        pbPower = (ProgressBar) findViewById(R.id.pbPower);
        pbPower.setMax(Math.round(mMagnetometer.getMaximumRange()/3));

        btSubmit = (Button) findViewById(R.id.btSubmit);
        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent("com.google.zxing.client.android.SCAN");

                if (getPackageManager().queryIntentActivities(i, PackageManager.MATCH_ALL).isEmpty()) {
                    Toast.makeText(getApplicationContext(), "QR scan App not Installed", Toast.LENGTH_LONG).show();
                    return;
                }

                i.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(i, SCAN_QR_CODE);
            }
        });
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mMagnetometer) {
            double mag = Math.sqrt(event.values[0]*event.values[0] + event.values[1]*event.values[1] + event.values[2]*event.values[2]);
            mPowerList.add(mag);
            if (mPowerList.size() > MAX_DATA_POINTS) {
                mPowerList.remove(0);
            }
            pbPower.setProgress((int) Math.round(mPowerList.get(mPowerList.size() - 1)));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_QR_CODE && resultCode == RESULT_OK) {
            Intent logIntent = new Intent("ch.appquest.intent.LOG");

            if (getPackageManager().queryIntentActivities(logIntent, PackageManager.MATCH_ALL).isEmpty()) {
                Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
                return;
            }

            // Achtung, je nach App wird etwas anderes eingetragen
            String logmessage = intent.getStringExtra("SCAN_RESULT");
            logIntent.putExtra("ch.appquest.logmessage", logmessage);

            startActivity(logIntent);
        }
    }
}
