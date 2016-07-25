/*
 * Copyright 2015 AndroidPlot.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.androidplot.demos;

import java.util.Arrays;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.Size;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYLegendWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.PixelUtils;

/**
 * The simplest possible example of using AndroidPlot to plot some data.
 */
public class DualScaleXYPlotExampleActivity extends Activity implements OnClickListener {

    private XYPlot plot1, plot2;
    private Boolean series2_onRight = true;
    private LineAndPointFormatter series1Format, series2Format;
    private Button button;

    // Declare and enable buttons to toggle whether the 2nd series is on left or right.

    // Create a couple arrays of y-values to plot:
    private Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
    private Number[] series2Numbers = {444, 613, 353, 876, 924, 1004};
    XYSeries series1, series2;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dual_scale_xy_plot_example);

        final float f26 = PixelUtils.dpToPix(26);
        final float f10 = PixelUtils.dpToPix(10);
        final Size sm = new Size(0, SizeLayoutType.FILL, 0, SizeLayoutType.FILL);

        plot1 = (XYPlot) findViewById(R.id.mySimpleXYPlot_L);
        plot2 = (XYPlot) findViewById(R.id.mySimpleXYPlot_R);

        // Disable Hardware Acceleration on the xyPlot view object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            plot1.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            plot2.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        /*
         * Setup the Plots
         */
        plot1.setPlotMargins(0, 0, 0, 0);
        plot2.setPlotMargins(0, 0, 0, 0);

        plot1.setPlotPadding(0, 0, 0, 0);
        plot2.setPlotPadding(0, 0, 0, 0);

        plot2.getDomainLabelWidget().setVisible(false);
        plot2.getRangeLabelWidget().setVisible(false);
        plot2.getTitleWidget().setVisible(false);
        plot2.setBorderPaint(null);
        plot2.setBackgroundPaint(null);

        /* 
         * Setup the Graph Widgets
         */
        XYGraphWidget graphWidget_L = plot1.getGraphWidget();
        XYGraphWidget graphWidget_R = plot2.getGraphWidget();

        graphWidget_L.setSize(sm);
        graphWidget_R.setSize(sm);

        graphWidget_L.setMargins(0, 0, 0, 0);
        graphWidget_R.setMargins(0, 0, 0, 0);

        graphWidget_L.setPadding(f26, f10, f26, f26);
        graphWidget_R.setPadding(f26, f10, f26, f26);

        graphWidget_L.setRangeAxisPosition(true, false, 4, "10");
        graphWidget_R.setRangeAxisPosition(false, false, 4, "10");

        graphWidget_L.setRangeTickLabelVerticalOffset(-3);
        graphWidget_R.setRangeTickLabelVerticalOffset(-3);

        graphWidget_L.setRangeOriginTickLabelPaint(null);
        graphWidget_R.setRangeOriginTickLabelPaint(null);

        graphWidget_L.setRangeTickLabelWidth(0);
        graphWidget_R.setRangeTickLabelWidth(0);

        graphWidget_L.setDomainTickLabelWidth(0);
        graphWidget_R.setDomainTickLabelWidth(0);

        graphWidget_R.setBackgroundPaint(null);
        graphWidget_R.setDomainTickLabelPaint(null);
        graphWidget_R.setGridBackgroundPaint(null);
        graphWidget_R.setDomainOriginTickLabelPaint(null);
        graphWidget_R.setRangeOriginLinePaint(null);
        graphWidget_R.setDomainGridLinePaint(null);
        graphWidget_R.setRangeGridLinePaint(null);

        graphWidget_L.getRangeTickLabelPaint().setTextSize(PixelUtils.dpToPix(8));
        graphWidget_R.getRangeTickLabelPaint().setTextSize(PixelUtils.dpToPix(8));

        graphWidget_L.getDomainOriginTickLabelPaint().setTextSize(PixelUtils.dpToPix(8));
        graphWidget_L.getDomainTickLabelPaint().setTextSize(PixelUtils.dpToPix(8));

        float textSize = graphWidget_L.getRangeTickLabelPaint().getTextSize();
        graphWidget_L.setRangeTickLabelVerticalOffset((textSize / 2) * -1);
        graphWidget_R.setRangeTickLabelVerticalOffset(graphWidget_L.getRangeTickLabelVerticalOffset());

        /*
         * Position the Graph Widgets in the Centre
         */
        graphWidget_L.position(0, XLayoutStyle.ABSOLUTE_FROM_CENTER, 0, YLayoutStyle.ABSOLUTE_FROM_CENTER, AnchorPosition.CENTER);
        graphWidget_R.position(0, XLayoutStyle.ABSOLUTE_FROM_CENTER, 0, YLayoutStyle.ABSOLUTE_FROM_CENTER, AnchorPosition.CENTER);

        /* 
         * Position the Label Widgets
         */
        plot1.getDomainLabelWidget().setWidth(100);
        plot1.getRangeLabelWidget().setWidth(100);
        plot1.getDomainLabelWidget().position(0, XLayoutStyle.RELATIVE_TO_CENTER, 1, YLayoutStyle.ABSOLUTE_FROM_BOTTOM, AnchorPosition.BOTTOM_MIDDLE);
        plot1.getRangeLabelWidget().position(1, XLayoutStyle.ABSOLUTE_FROM_LEFT, -20, YLayoutStyle.ABSOLUTE_FROM_CENTER, AnchorPosition.LEFT_BOTTOM);

        /*
         *  Setup and Position the LEFT Legend
         */
        XYLegendWidget legendWidget_LEFT = plot1.getLegendWidget();
        legendWidget_LEFT.setSize(new Size(100, SizeLayoutType.ABSOLUTE, 200, SizeLayoutType.ABSOLUTE));
        legendWidget_LEFT.setPadding(1, 1, 1, 1);
        legendWidget_LEFT.setTableModel(new DynamicTableModel(1, 3));
        legendWidget_LEFT.setIconSize(new Size(PixelUtils.dpToPix(10), SizeLayoutType.ABSOLUTE, PixelUtils.dpToPix(10), SizeLayoutType.ABSOLUTE));
        legendWidget_LEFT.getTextPaint().setTextSize(PixelUtils.dpToPix(9));
        legendWidget_LEFT.position(PixelUtils.dpToPix(30), XLayoutStyle.ABSOLUTE_FROM_LEFT, f10 + 2, YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.LEFT_TOP);

       
        /*
         *  Setup and Position the RIGHT Legend
         */
        XYLegendWidget legendWidget_RIGHT = plot2.getLegendWidget();
        legendWidget_RIGHT.setSize(new Size(100, SizeLayoutType.ABSOLUTE, 200, SizeLayoutType.ABSOLUTE));
        legendWidget_RIGHT.setPadding(1, 1, 1, 1);
        legendWidget_RIGHT.setTableModel(new DynamicTableModel(1, 3));
        legendWidget_RIGHT.setIconSize(new Size(PixelUtils.dpToPix(10), SizeLayoutType.ABSOLUTE, PixelUtils.dpToPix(10), SizeLayoutType.ABSOLUTE));
        legendWidget_RIGHT.getTextPaint().setTextSize(PixelUtils.dpToPix(9));
        legendWidget_RIGHT.getTextPaint().setTextAlign(Align.RIGHT);
        legendWidget_RIGHT.setMarginLeft(185);
        legendWidget_RIGHT.position(PixelUtils.dpToPix(30), XLayoutStyle.ABSOLUTE_FROM_RIGHT, f10 + 2, YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.RIGHT_TOP);

        // Setup the Series
        series1 = new SimpleXYSeries(Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
        series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        // Setup the formatters
        series1Format = new LineAndPointFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 100, 0), null, new PointLabelFormatter(Color.WHITE));
        series2Format = new LineAndPointFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), null, new PointLabelFormatter(Color.WHITE));

        // Setup the Button
        button = (Button) findViewById(R.id.toggleSeries2);
        button.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateView();

    }

    private void updateView() {

        // Remove all current series from each plot
        plot1.clear();
        plot2.clear();

        // Add series to each plot as needed.
        plot1.addSeries(series1, series1Format);
        if (series2_onRight) {
            plot2.addSeries(series2, series2Format);
        } else {
            plot1.addSeries(series2, series2Format);
        }

        // Finalise each Plot based on whether they have any series or not.
        if (!plot2.getSeriesRegistry().isEmpty()) {
            plot2.setVisibility(XYPlot.VISIBLE);
            plot2.redraw();
        } else {
            plot2.setVisibility(XYPlot.INVISIBLE);
        }

        if (!plot1.getSeriesRegistry().isEmpty()) {
            plot1.setVisibility(XYPlot.VISIBLE);
            plot1.redraw();
        } else {
            plot1.setVisibility(XYPlot.INVISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        if (series2_onRight) {
            series2_onRight = false;
        } else {
            series2_onRight = true;
        }
        updateView();
    }
}
