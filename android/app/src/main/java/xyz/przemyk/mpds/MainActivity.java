package xyz.przemyk.mpds;

import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.material.snackbar.Snackbar;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import me.aflak.arduino.ArduinoListener;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity implements ArduinoListener {
    private ArduinoFixed arduino;
    private long start;
    private GraphView graphView;
    private LineGraphSeries<DataPoint> graphSeries;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        view = findViewById(R.id.layout);

        arduino = new ArduinoFixed(this);

        graphView = findViewById(R.id.graph);
        graphSeries = new LineGraphSeries<>();
        graphView.addSeries(graphSeries);
        graphView.setBackgroundColor(Color.BLACK);
        GridLabelRenderer labelRenderer = graphView.getGridLabelRenderer();
        labelRenderer.setGridColor(Color.WHITE);
        labelRenderer.setHorizontalLabelsColor(Color.WHITE);
        labelRenderer.setVerticalLabelsColor(Color.WHITE);
        labelRenderer.setHorizontalAxisTitle("Time [s]");
        labelRenderer.setHorizontalAxisTitleColor(Color.WHITE);
        labelRenderer.setVerticalAxisTitle("Voltage");
        labelRenderer.setVerticalAxisTitleColor(Color.WHITE);
        final Viewport graphViewport = graphView.getViewport();

        graphViewport.setXAxisBoundsManual(true);
        graphViewport.setMinX(0);
        graphViewport.setMaxX(10);

        graphViewport.setYAxisBoundsManual(true);
        graphViewport.setMinY(0);
        graphViewport.setMaxY(5);
    }

    @Override
    protected void onStart() {
        super.onStart();
        start = System.currentTimeMillis();
        arduino.setArduinoListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        arduino.unsetArduinoListener();
        arduino.close();
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
    public void onArduinoAttached(UsbDevice device) {
        Snackbar.make(view, "Arduino attached!", Snackbar.LENGTH_SHORT).show();
        arduino.open(device);
    }

    @Override
    public void onArduinoDetached() {
        Snackbar.make(view, "Arduino detached!", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onArduinoMessage(byte[] bytes) {
        String data = new String(bytes);
        if (!data.equals("\r") && !data.isEmpty()) {
            runOnUiThread(() -> graphSeries.appendData(
                    new DataPoint((System.currentTimeMillis() - start) / 1000d, Double.parseDouble(data)),
                    true, 3000));
        }
    }

    @Override
    public void onArduinoOpened() {
        Snackbar.make(view, "Arduino opened!", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onUsbPermissionDenied() {
        Snackbar.make(view, "USB permission denied... New attempt in 3 sec", Snackbar.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> arduino.reopen(), 3000);
    }
}
