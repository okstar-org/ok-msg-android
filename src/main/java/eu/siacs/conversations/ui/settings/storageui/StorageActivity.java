package eu.siacs.conversations.ui.settings.storageui;

import static eu.siacs.conversations.Config.LOGTAG;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;

import eu.siacs.conversations.R;
import eu.siacs.conversations.ui.XmppActivity;
import eu.siacs.conversations.ui.settings.storageui.item.AutoClearItem;
import eu.siacs.conversations.ui.settings.storageui.item.StorageItem;
import eu.siacs.conversations.ui.util.StyledAttributes;
import eu.siacs.conversations.utils.ThemeHelper;

public class StorageActivity extends XmppActivity implements OnChartValueSelectedListener {

    private PieChart chart;
    private RecyclerView recyclerView;
    private RecyclerView autoClearRecyclerView;
    private StorageAdapter storageAdapter;
    private AutoClearAdapter autoClearAdapter;

    protected final String[] parties = new String[] {
           "all","image","audio","video","doc","binary"
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeHelper.find(this));
        ThemeHelper.applyCustomColors(this);
        setContentView(R.layout.activity_storage);
        getWindow().getDecorView().setBackgroundColor(StyledAttributes.getColor(this, R.attr.color_background_secondary));
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_setting_storage_ui);
        setSupportActionBar(toolbar);
        configureActionBar(getSupportActionBar());

        initChart();

        initRecyclerView();
    }

    private void initChart(){
        chart = findViewById(R.id.chart1);

        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);

        chart.setDragDecelerationFrictionCoef(0.95f);

        //chart.setCenterTextTypeface(tfLight);
        chart.setCenterText(generateCenterSpannableText());

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.BLACK);

        chart.setTransparentCircleColor(Color.BLACK);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);

        chart.setDrawCenterText(true);

        chart.setRotationAngle(0);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);
        chart.setDrawEntryLabels(false);

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        chart.setOnChartValueSelectedListener(this);

        chart.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        //设置说明文案,setEnabled = false，说明文案不显示
        Legend l = chart.getLegend();
        l.setDrawInside(false);
        l.setEnabled(false);

        // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
//        chart.setEntryLabelTypeface(tfRegular);
        chart.setEntryLabelTextSize(12f);

        setData(6, 1);
    }


    private void setData(int count, float range) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        //(float) ((Math.random() * range) + range / 5
        for (int i = 0; i < count ; i++) {
            entries.add(new PieEntry(range, parties[i % parties.length],
                    null));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Election Results");

        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
//        data.setValueTypeface(tfLight);
        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("14.8\nGB");
        s.setSpan(new RelativeSizeSpan(1.7f), 0, s.length(), 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
        s.setSpan(new RelativeSizeSpan(.8f), 0, s.length(), 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), 0, s.length(), 0);
        return s;
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_storage);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,  LinearLayoutManager.VERTICAL, false));
        storageAdapter = new StorageAdapter();
        recyclerView.setAdapter(storageAdapter);

        storageAdapter.addStorageItem(new StorageItem("音频文件", "<1%", "14.8MB", false));
        storageAdapter.addStorageItem(new StorageItem("视频文件", "<1.2%", "20MB", false));
        storageAdapter.addStorageItem(new StorageItem("图片文件", "<2%", "890KB", false));
        storageAdapter.addStorageItem(new StorageItem("其他文件", "<3%", "16GB", false));
        storageAdapter.addStorageItem(new StorageItem("文档文件", "<5.4%", "14.2MB", false));

        autoClearRecyclerView = findViewById(R.id.recycler_view_auto_clear);
        autoClearRecyclerView.setLayoutManager(new LinearLayoutManager(this,  LinearLayoutManager.VERTICAL, false));
        autoClearAdapter = new AutoClearAdapter();
        autoClearRecyclerView.setAdapter(autoClearAdapter);
        autoClearAdapter.addStorageItem(new AutoClearItem("音频文件", "14.8MB"));
        autoClearAdapter.addStorageItem(new AutoClearItem("视频文件", "20MB"));


    }


    @Override
    protected void refreshUiReal() {

    }

    @Override
    protected void onBackendConnected() {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.d(LOGTAG,
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i(LOGTAG, "nothing selected");
    }
}