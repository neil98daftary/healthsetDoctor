package in.ashnehete.healthsetdoctor.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMode;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.ashnehete.healthsetdoctor.R;

import static in.ashnehete.healthsetdoctor.AppConstants.RECORD_VALUE;

public class EcgActivity extends AppCompatActivity {

    private static final String TAG = "EcgActivity";

    @BindView(R.id.plot_ecg)
    XYPlot plotEcg;

    private ECGModel series;
    private Redrawer redrawer;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        final String value = bundle.getString(RECORD_VALUE, null);

        setupGraph();

        new Thread() {
            public void run() {
                getData(value);
            }
        }.start();
    }

    private void getData(String value) {
        URL url = null;
        try {
            url = new URL(value);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = url.openStream();
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;

            while ((n = is.read(byteChunk)) > 0) {
                Log.d("ECG VALUES", String.valueOf(byteChunk));
                baos.write(byteChunk, 0, n);
            }
        } catch (IOException e) {
            System.err.printf("Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
            e.printStackTrace();
            // Perform any other exception handling that's appropriate.
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            StringTokenizer st = new StringTokenizer(new String(baos.toByteArray()), ",");
            Log.i(TAG, "Tokens: " + st.countTokens());
            plotEcg.getOuterLimits().set(0, st.countTokens(), 0, 800);
            while (st.hasMoreTokens()) {
                series.update(Integer.parseInt(st.nextToken()));
            }
        }
    }

    private void setupGraph() {
        plotEcg.getGraph().getGridBackgroundPaint().setColor(Color.BLACK);
        plotEcg.getGraph().getBackgroundPaint().setColor(Color.BLACK);
        plotEcg.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).getPaint().setColor(Color.TRANSPARENT);
        plotEcg.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).getPaint().setColor(Color.TRANSPARENT);
        plotEcg.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        plotEcg.getGraph().getDomainSubGridLinePaint().setColor(Color.TRANSPARENT);
        plotEcg.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        plotEcg.getGraph().getRangeSubGridLinePaint().setColor(Color.TRANSPARENT);

        plotEcg.getGraph().setSize(new Size(
                1.0f, SizeMode.RELATIVE,
                1.0f, SizeMode.RELATIVE));


        series = new ECGModel();
        LineAndPointFormatter seriesFormatter = new LineAndPointFormatter(Color.RED, null, null, null);

//        seriesFormatter.setInterpolationParams(
//                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        plotEcg.setRangeBoundaries(0, 800, BoundaryMode.FIXED);
        plotEcg.setDomainBoundaries(0, 600, BoundaryMode.FIXED);

        // reduce the number of range labels
        plotEcg.setLinesPerRangeLabel(3);

        plotEcg.addSeries(series, seriesFormatter);

        PanZoom.attach(plotEcg, PanZoom.Pan.HORIZONTAL, PanZoom.Zoom.NONE);

        series.start(new WeakReference<>(plotEcg.getRenderer(AdvancedLineAndPointRenderer.class)));

        // set a redraw rate of 30hz and start immediately:
        redrawer = new Redrawer(plotEcg, 30, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        redrawer.finish();
    }

    public static class ECGModel implements XYSeries {

        private final List<Integer> data;
        private int latestIndex;

        private WeakReference<AdvancedLineAndPointRenderer> rendererRef;

        public ECGModel() {
            data = new ArrayList<>();
            latestIndex = data.size();
        }

        public void update(int point) {
            Log.i(TAG, "update: " + point);
            data.add(point);

            if (rendererRef.get() != null) {
                rendererRef.get().setLatestIndex(data.size());
            }
        }

        public void start(final WeakReference<AdvancedLineAndPointRenderer> rendererRef) {
            this.rendererRef = rendererRef;
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public Number getX(int index) {
            return index;
        }

        @Override
        public Number getY(int index) {
            return data.get(index);
        }

        @Override
        public String getTitle() {
            return "Signal";
        }

        public String getDataString() {
            return data.toString();
        }
    }
}
