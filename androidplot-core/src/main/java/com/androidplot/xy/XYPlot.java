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

package com.androidplot.xy;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import com.androidplot.Plot;
import com.androidplot.R;
import com.androidplot.ui.*;
import com.androidplot.ui.TextOrientationType;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.util.AttrUtils;
import com.androidplot.util.PixelUtils;
import com.androidplot.util.SeriesUtils;

import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A View to graphically display x/y coordinates.
 */
public class XYPlot extends Plot<XYSeries, XYSeriesFormatter, XYSeriesRenderer> {

    private static final int DEFAULT_LEGEND_WIDGET_H_DP = 10;
    private static final int DEFAULT_LEGEND_WIDGET_ICON_SIZE_DP = 7;

    private static final int DEFAULT_GRAPH_WIDGET_H_DP = 18;
    private static final int DEFAULT_GRAPH_WIDGET_W_DP = 10;

    private static final int DEFAULT_DOMAIN_LABEL_WIDGET_H_DP = 10;
    private static final int DEFAULT_DOMAIN_LABEL_WIDGET_W_DP = 80;

    private static final int DEFAULT_RANGE_LABEL_WIDGET_H_DP = 50;
    private static final int DEFAULT_RANGE_LABEL_WIDGET_W_DP = 10;

    private static final int DEFAULT_LEGEND_WIDGET_Y_OFFSET_DP = 0;
    private static final int DEFAULT_LEGEND_WIDGET_X_OFFSET_DP = 40;
    private static final int DEFAULT_GRAPH_WIDGET_Y_OFFSET_DP = 0;
    private static final int DEFAULT_GRAPH_WIDGET_X_OFFSET_DP = 0;

    private static final int DEFAULT_DOMAIN_LABEL_WIDGET_Y_OFFSET_DP = 0;
    private static final int DEFAULT_DOMAIN_LABEL_WIDGET_X_OFFSET_DP = 20;

    private static final int DEFAULT_RANGE_LABEL_WIDGET_Y_OFFSET_DP = 0;
    private static final int DEFAULT_RANGE_LABEL_WIDGET_X_OFFSET_DP = 0;

    private static final int DEFAULT_DOMAIN_TICK_EXTENSION_DP = 2;
    private static final int DEFAULT_RANGE_TICK_EXTENSION_DP = 2;

    private static final int DEFAULT_PLOT_LEFT_MARGIN_DP = 1;
    private static final int DEFAULT_PLOT_RIGHT_MARGIN_DP = 1;
    private static final int DEFAULT_PLOT_TOP_MARGIN_DP = 1;
    private static final int DEFAULT_PLOT_BOTTOM_MARGIN_DP = 1;

    private static final int DEFAULT_DOMAIN_TICK_LABEL_WIDTH = 15;
    private static final int DEFAULT_RANGE_TICK_LABEL_WIDTH = 41;

    private BoundaryMode domainOriginBoundaryMode;
    private BoundaryMode rangeOriginBoundaryMode;

    // widgets
    private XYLegendWidget legendWidget;
    private XYGraphWidget graphWidget;
    private TextLabelWidget domainLabelWidget;
    private TextLabelWidget rangeLabelWidget;

    private XYStepModel domainStepModel;
    private XYStepModel rangeStepModel;

    private XYConstraints constraints = new XYConstraints();

    // these are the final min/max used for dispplaying data
    private Number calculatedMinX;
    private Number calculatedMaxX;
    private Number calculatedMinY;
    private Number calculatedMaxY;

    // previous calculated min/max vals.
    // primarily used for GROW/SHRINK operations.
    private Number prevMinX;
    private Number prevMaxX;
    private Number prevMinY;
    private Number prevMaxY;

    // uses set boundary min and max values
    // should be null if not used.
    private Number rangeTopMin = null;
    private Number rangeTopMax = null;
    private Number rangeBottomMin = null;
    private Number rangeBottomMax = null;
    private Number domainLeftMin = null;
    private Number domainLeftMax = null;
    private Number domainRightMin = null;
    private Number domainRightMax = null;

    private Number userDomainOrigin;
    private Number userRangeOrigin;

    private Number calculatedDomainOrigin;
    private Number calculatedRangeOrigin;

    @SuppressWarnings("FieldCanBeLocal")
    private Number domainOriginExtent = null;
    @SuppressWarnings("FieldCanBeLocal")
    private Number rangeOriginExtent = null;

    private boolean drawDomainOriginEnabled = true;
    private boolean drawRangeOriginEnabled = true;

    private ArrayList<YValueMarker> yValueMarkers;
    private ArrayList<XValueMarker> xValueMarkers;

    private RectRegion defaultBounds;

    private PreviewMode previewMode;
    public enum PreviewMode {
        LineAndPoint,
        Candlestick,
        Bar
    }

    public XYPlot(Context context, String title) {
        super(context, title);
    }

    public XYPlot(Context context, String title, RenderMode mode) {
        super(context, title, mode);
    }

    public XYPlot(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public XYPlot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    @Override
    protected void onPreInit() {
        legendWidget = new XYLegendWidget(
                getLayoutManager(),
                this,
                new Size(
                        PixelUtils.dpToPix(DEFAULT_LEGEND_WIDGET_H_DP),
                        SizeLayoutType.ABSOLUTE, 0.5f, SizeLayoutType.RELATIVE),
                new DynamicTableModel(0, 1),
                new Size(
                        PixelUtils.dpToPix(DEFAULT_LEGEND_WIDGET_ICON_SIZE_DP),
                        SizeLayoutType.ABSOLUTE,
                        PixelUtils.dpToPix(DEFAULT_LEGEND_WIDGET_ICON_SIZE_DP),
                        SizeLayoutType.ABSOLUTE));

        graphWidget = new XYGraphWidget(
                getLayoutManager(),
                this,
                new Size(
                        PixelUtils.dpToPix(DEFAULT_GRAPH_WIDGET_H_DP),
                        SizeLayoutType.FILL,
                        PixelUtils.dpToPix(DEFAULT_GRAPH_WIDGET_W_DP),
                        SizeLayoutType.FILL));

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.DKGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);
        graphWidget.setBackgroundPaint(backgroundPaint);


        domainLabelWidget = new TextLabelWidget(
                getLayoutManager(),
                new Size(
                        PixelUtils.dpToPix(DEFAULT_DOMAIN_LABEL_WIDGET_H_DP),
                        SizeLayoutType.ABSOLUTE,
                        PixelUtils.dpToPix(DEFAULT_DOMAIN_LABEL_WIDGET_W_DP),
                        SizeLayoutType.ABSOLUTE),
                TextOrientationType.HORIZONTAL);
        rangeLabelWidget = new TextLabelWidget(
                getLayoutManager(),
                new Size(
                        PixelUtils.dpToPix(DEFAULT_RANGE_LABEL_WIDGET_H_DP),
                        SizeLayoutType.ABSOLUTE,
                        PixelUtils.dpToPix(DEFAULT_RANGE_LABEL_WIDGET_W_DP),
                        SizeLayoutType.ABSOLUTE),
                TextOrientationType.VERTICAL_ASCENDING);

        legendWidget.position(
                PixelUtils.dpToPix(DEFAULT_LEGEND_WIDGET_X_OFFSET_DP),
                XLayoutStyle.ABSOLUTE_FROM_RIGHT,
                PixelUtils.dpToPix(DEFAULT_LEGEND_WIDGET_Y_OFFSET_DP),
                YLayoutStyle.ABSOLUTE_FROM_BOTTOM,
                AnchorPosition.RIGHT_BOTTOM);

        graphWidget.position(
                PixelUtils.dpToPix(DEFAULT_GRAPH_WIDGET_X_OFFSET_DP),
                XLayoutStyle.ABSOLUTE_FROM_RIGHT,
                PixelUtils.dpToPix(DEFAULT_GRAPH_WIDGET_Y_OFFSET_DP),
                YLayoutStyle.ABSOLUTE_FROM_CENTER,
                AnchorPosition.RIGHT_MIDDLE);

        domainLabelWidget.position(
                PixelUtils.dpToPix(DEFAULT_DOMAIN_LABEL_WIDGET_X_OFFSET_DP),
                XLayoutStyle.ABSOLUTE_FROM_LEFT,
                PixelUtils.dpToPix(DEFAULT_DOMAIN_LABEL_WIDGET_Y_OFFSET_DP),
                YLayoutStyle.ABSOLUTE_FROM_BOTTOM,
                AnchorPosition.LEFT_BOTTOM);

        rangeLabelWidget.position(
                PixelUtils.dpToPix(DEFAULT_RANGE_LABEL_WIDGET_X_OFFSET_DP),
                XLayoutStyle.ABSOLUTE_FROM_LEFT,
                PixelUtils.dpToPix(DEFAULT_RANGE_LABEL_WIDGET_Y_OFFSET_DP),
                YLayoutStyle.ABSOLUTE_FROM_CENTER,
                AnchorPosition.LEFT_MIDDLE);

        getLayoutManager().moveToTop(getTitleWidget());
        getLayoutManager().moveToTop(getLegendWidget());

        getDomainLabelWidget().pack();
        getRangeLabelWidget().pack();
        setPlotMarginLeft(PixelUtils.dpToPix(DEFAULT_PLOT_LEFT_MARGIN_DP));
        setPlotMarginRight(PixelUtils.dpToPix(DEFAULT_PLOT_RIGHT_MARGIN_DP));
        setPlotMarginTop(PixelUtils.dpToPix(DEFAULT_PLOT_TOP_MARGIN_DP));
        setPlotMarginBottom(PixelUtils.dpToPix(DEFAULT_PLOT_BOTTOM_MARGIN_DP));

        xValueMarkers = new ArrayList<>();
        yValueMarkers = new ArrayList<>();

        setDefaultBounds(new RectRegion(-1, 1, -1, 1));

        domainStepModel = new XYStepModel(XYStepMode.SUBDIVIDE, 10);
        rangeStepModel = new XYStepModel(XYStepMode.SUBDIVIDE, 10);
    }

    @Override
    protected void onAfterConfig() {
        // display some generic series data in editors that support it:
        if(isInEditMode()) {

            switch (previewMode) {
                case LineAndPoint: {
                    addSeries(new SimpleXYSeries(Arrays.asList(1, 2, 3, 3, 4), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Red"),
                            new LineAndPointFormatter(Color.RED, null, null, null));
                    addSeries(new SimpleXYSeries(Arrays.asList(2, 1, 4, 2, 5), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Green"),
                            new LineAndPointFormatter(Color.GREEN, null, null, null));
                    addSeries(new SimpleXYSeries(Arrays.asList(3, 3, 2, 3, 3), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Blue"),
                            new LineAndPointFormatter(Color.BLUE, null, null, null));
                }
                break;
                case Candlestick: {
                    CandlestickSeries candlestickSeries = new CandlestickSeries(
                            new CandlestickSeries.Item(1, 10, 2, 9),
                            new CandlestickSeries.Item(4, 18, 6, 5),
                            new CandlestickSeries.Item(3, 11, 5, 10),
                            new CandlestickSeries.Item(2, 17, 2, 15),
                            new CandlestickSeries.Item(6, 11, 11, 7),
                            new CandlestickSeries.Item(8, 16, 10, 15));
                    CandlestickMaker.make(this, new CandlestickFormatter(), candlestickSeries);
                }
                break;
                case Bar: {
                    throw new UnsupportedOperationException("Not yet implemented.");
                }
                default:
                    throw new UnsupportedOperationException("Unexpected preview mode: " + previewMode);
            }
        }
    }

    @Override
    protected void processAttrs(TypedArray attrs) {
        this. previewMode = PreviewMode.values()[attrs.getInt(
                R.styleable.xy_XYPlot_previewMode, PreviewMode.LineAndPoint.ordinal())];

        // graph size & position
        AttrUtils.configureWidget(attrs, getGraphWidget(),
                R.styleable.xy_XYPlot_graphHeightSizeLayoutType, R.styleable.xy_XYPlot_graphHeight,
                R.styleable.xy_XYPlot_graphWidthSizeLayoutType, R.styleable.xy_XYPlot_graphWidth,
                R.styleable.xy_XYPlot_graphLayoutStyleX, R.styleable.xy_XYPlot_graphPositionX,
                R.styleable.xy_XYPlot_graphLayoutStyleY, R.styleable.xy_XYPlot_graphPositionY,
                R.styleable.xy_XYPlot_graphAnchorPosition, R.styleable.xy_XYPlot_graphVisible);

        String domainLabelAttr = attrs.getString(R.styleable.xy_XYPlot_domainLabel);
        if(domainLabelAttr != null) {
            setDomainLabel(domainLabelAttr);
        }

        String rangeLabelAttr = attrs.getString(R.styleable.xy_XYPlot_rangeLabel);
        if(rangeLabelAttr != null) {
            setRangeLabel(rangeLabelAttr);
        }

        graphWidget.setDomainTickLabelWidth(
                attrs.getDimension(R.styleable.xy_XYPlot_domainTickLabelWidth, DEFAULT_DOMAIN_TICK_LABEL_WIDTH));

        graphWidget.setRangeTickLabelWidth(
                attrs.getDimension(R.styleable.xy_XYPlot_rangeTickLabelWidth, DEFAULT_RANGE_TICK_LABEL_WIDTH));

        AttrUtils.configureStep(attrs, getDomainStepModel(),
                R.styleable.xy_XYPlot_domainStepMode, R.styleable.xy_XYPlot_domainStep);

        AttrUtils.configureStep(attrs, getRangeStepModel(),
                R.styleable.xy_XYPlot_rangeStepMode, R.styleable.xy_XYPlot_rangeStep);

        // domainLabel size & position
        AttrUtils.configureWidget(attrs, getDomainLabelWidget(),
                R.styleable.xy_XYPlot_domainLabelHeightSizeLayoutType, R.styleable.xy_XYPlot_domainLabelHeight,
                R.styleable.xy_XYPlot_domainLabelWidthSizeLayoutType, R.styleable.xy_XYPlot_domainLabelWidth,
                R.styleable.xy_XYPlot_domainLabelLayoutStyleX, R.styleable.xy_XYPlot_domainLabelPositionX,
                R.styleable.xy_XYPlot_domainLabelLayoutStyleY, R.styleable.xy_XYPlot_domainLabelPositionY,
                R.styleable.xy_XYPlot_domainLabelAnchorPosition, R.styleable.xy_XYPlot_domainLabelVisible);

        // rangeLabel size & position
        AttrUtils.configureWidget(attrs, getRangeLabelWidget(),
                R.styleable.xy_XYPlot_rangeLabelHeightSizeLayoutType, R.styleable.xy_XYPlot_rangeLabelHeight,
                R.styleable.xy_XYPlot_rangeLabelWidthSizeLayoutType, R.styleable.xy_XYPlot_rangeLabelWidth,
                R.styleable.xy_XYPlot_rangeLabelLayoutStyleX, R.styleable.xy_XYPlot_rangeLabelPositionX,
                R.styleable.xy_XYPlot_rangeLabelLayoutStyleY, R.styleable.xy_XYPlot_rangeLabelPositionY,
                R.styleable.xy_XYPlot_rangeLabelAnchorPosition, R.styleable.xy_XYPlot_rangeLabelVisible);

        // domainLabelPaint
        AttrUtils.configureTextPaint(attrs, getDomainLabelWidget().getLabelPaint(),
                R.styleable.xy_XYPlot_domainLabelTextColor, R.styleable.xy_XYPlot_domainLabelTextSize);

        // rangeLabelPaint
        AttrUtils.configureTextPaint(attrs, getRangeLabelWidget().getLabelPaint(),
                R.styleable.xy_XYPlot_rangeLabelTextColor, R.styleable.xy_XYPlot_rangeLabelTextSize);

        // graphWidget
        AttrUtils.configureBoxModelable(attrs, getGraphWidget(),
                R.styleable.xy_XYPlot_graphMarginTop, R.styleable.xy_XYPlot_graphMarginBottom,
                R.styleable.xy_XYPlot_graphMarginLeft, R.styleable.xy_XYPlot_graphMarginRight,
                R.styleable.xy_XYPlot_graphPaddingTop, R.styleable.xy_XYPlot_graphPaddingBottom,
                R.styleable.xy_XYPlot_graphPaddingLeft, R.styleable.xy_XYPlot_graphPaddingRight);

        graphWidget.setDomainTickExtension(attrs.getDimension(R.styleable.xy_XYPlot_domainTickExtension,
                PixelUtils.dpToPix(DEFAULT_DOMAIN_TICK_EXTENSION_DP)));

        graphWidget.setRangeTickExtension(attrs.getDimension(R.styleable.xy_XYPlot_rangeTickExtension,
                PixelUtils.dpToPix(DEFAULT_RANGE_TICK_EXTENSION_DP)));

        graphWidget.setShowDomainLabels(attrs.getBoolean(R.styleable.xy_XYPlot_showDomainLabels, true));
        graphWidget.setShowRangeLabels(attrs.getBoolean(R.styleable.xy_XYPlot_showRangeLabels, true));

        // gridRect
        AttrUtils.configureBoxModelable(attrs, getGraphWidget().getGridBox(),
                R.styleable.xy_XYPlot_gridMarginTop, R.styleable.xy_XYPlot_gridMarginBottom,
                R.styleable.xy_XYPlot_gridMarginLeft, R.styleable.xy_XYPlot_gridMarginRight,
                R.styleable.xy_XYPlot_gridPaddingTop, R.styleable.xy_XYPlot_gridPaddingBottom,
                R.styleable.xy_XYPlot_gridPaddingLeft, R.styleable.xy_XYPlot_gridPaddingRight);

        // domainTickLabelPaint
        AttrUtils.configureTextPaint(attrs, getGraphWidget().getDomainTickLabelPaint(),
                R.styleable.xy_XYPlot_domainTickLabelTextColor,
                R.styleable.xy_XYPlot_domainTickLabelTextSize);

        // rangeTickLabelPaint
        AttrUtils.configureTextPaint(attrs, getGraphWidget().getRangeTickLabelPaint(),
                R.styleable.xy_XYPlot_rangeTickLabelTextColor,
                R.styleable.xy_XYPlot_rangeTickLabelTextSize);

        // domainOriginTickLabelPaint
        AttrUtils.configureTextPaint(attrs, getGraphWidget().getDomainOriginTickLabelPaint(),
                R.styleable.xy_XYPlot_domainOriginTickLabelTextColor,
                R.styleable.xy_XYPlot_domainOriginTickLabelTextSize);

        // rangeOriginTickLabelPaint
        AttrUtils.configureTextPaint(attrs, getGraphWidget().getRangeOriginTickLabelPaint(),
                R.styleable.xy_XYPlot_rangeOriginTickLabelTextColor,
                R.styleable.xy_XYPlot_rangeOriginTickLabelTextSize);

        // domainOriginLinePaint
        AttrUtils.configureLinePaint(attrs, getGraphWidget().getDomainOriginLinePaint(),
                R.styleable.xy_XYPlot_domainOriginLineColor,
                R.styleable.xy_XYPlot_domainOriginLineThickness);

        // rangeOriginLinePaint
        AttrUtils.configureLinePaint(attrs, getGraphWidget().getRangeOriginLinePaint(),
                R.styleable.xy_XYPlot_rangeOriginLineColor,
                R.styleable.xy_XYPlot_rangeOriginLineThickness);

        // legendWTextPaint
        AttrUtils.configureTextPaint(attrs, getLegendWidget().getTextPaint(),
                R.styleable.xy_XYPlot_legendTextColor,
                R.styleable.xy_XYPlot_legendTextSize);

        // legendIconSize
        AttrUtils.configureSize(attrs, getLegendWidget().getIconSize(),
                R.styleable.xy_XYPlot_legendIconHeightSizeLayoutType, R.styleable.xy_XYPlot_legendIconHeight,
                R.styleable.xy_XYPlot_legendIconWidthSizeLayoutType, R.styleable.xy_XYPlot_legendIconWidth);

        // legend size & position
        AttrUtils.configureWidget(attrs, getLegendWidget(),
                R.styleable.xy_XYPlot_legendHeightSizeLayoutType, R.styleable.xy_XYPlot_legendHeight,
                R.styleable.xy_XYPlot_legendWidthSizeLayoutType, R.styleable.xy_XYPlot_legendWidth,
                R.styleable.xy_XYPlot_legendLayoutStyleX, R.styleable.xy_XYPlot_legendPositionX,
                R.styleable.xy_XYPlot_legendLayoutStyleY, R.styleable.xy_XYPlot_legendPositionY,
                R.styleable.xy_XYPlot_legendAnchorPosition, R.styleable.xy_XYPlot_legendVisible);

        AttrUtils.configureLinePaint(attrs, getGraphWidget().getDomainGridLinePaint(),
                R.styleable.xy_XYPlot_graphDomainLineColor, R.styleable.xy_XYPlot_graphDomainLineThickness);

        AttrUtils.configureLinePaint(attrs, getGraphWidget().getRangeGridLinePaint(),
                R.styleable.xy_XYPlot_graphRangeLineColor, R.styleable.xy_XYPlot_graphRangeLineThickness);

        getGraphWidget().getBackgroundPaint().setColor(attrs.getColor(
                R.styleable.xy_XYPlot_graphBackgroundColor, getGraphWidget().getBackgroundPaint().getColor()));

        getGraphWidget().getGridBackgroundPaint().setColor(attrs.getColor(
                R.styleable.xy_XYPlot_gridBackgroundColor, getGraphWidget().getGridBackgroundPaint().getColor()));
    }

    public void setGridPadding(float left, float top, float right, float bottom) {
        getGraphWidget().getGridBox().setPadding(left, top, right, bottom);
    }

    @Override
    protected void notifyListenersBeforeDraw(Canvas canvas) {
        super.notifyListenersBeforeDraw(canvas);

        // this call must be AFTER the notify so that if the listener
        // is a synchronized series, it has the opportunity to
        // place a read lock on it's data.
        calculateMinMaxVals();
    }

    /**
     * Checks whether the point is within the plot's graph area.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean containsPoint(float x, float y) {
        if (getGraphWidget().getGridDimensions().marginatedRect != null) {
            return getGraphWidget().getGridDimensions().marginatedRect.contains(x, y);
        }
        return false;
    }

    /**                                                           `
     * Convenience method - wraps containsPoint(PointF).
     *
     * @param point
     * @return
     */
    public boolean containsPoint(PointF point) {
        return containsPoint(point.x, point.y);
    }

    public void setCursorPosition(PointF point) {
        getGraphWidget().setCursorPosition(point);
    }

    public void setCursorPosition(float x, float y) {
        getGraphWidget().setCursorPosition(x, y);
    }

    public Number getYVal(PointF point) {
        return getGraphWidget().getYVal(point);
    }

    public Number getXVal(PointF point) {
        return getGraphWidget().getXVal(point);
    }

    public void calculateMinMaxVals() {
        prevMinX = calculatedMinX;
        prevMaxX = calculatedMaxX;
        prevMinY = calculatedMinY;
        prevMaxY = calculatedMaxY;

        calculatedMinX = constraints.getMinX();
        calculatedMaxX = constraints.getMaxX();
        calculatedMinY = constraints.getMinY();
        calculatedMaxY = constraints.getMaxY();

        // only calculate if we must:
        if(calculatedMinX == null || calculatedMaxX == null || calculatedMinY == null || calculatedMaxY == null) {

            XYBounds bounds = SeriesUtils.minMax(constraints, getSeriesRegistry().getSeriesList());

            if(calculatedMinX == null) calculatedMinX = bounds.getMinX();
            if(calculatedMaxX == null) calculatedMaxX = bounds.getMaxX();
            if(calculatedMinY == null) calculatedMinY = bounds.getMinY();
            if(calculatedMaxY == null) calculatedMaxY = bounds.getMaxY();
        }

        // at this point we now know what points are going to be visible on our
        // plot, but we still need to make corrections based on modes being used:
        // (grow, shrink etc.)
        switch (constraints.getDomainFramingModel()) {
            case ORIGIN:
                updateDomainMinMaxForOriginModel();
                break;
            case EDGE:
                calculatedMaxX = getCalculatedUpperBoundary(
                        constraints.getDomainUpperBoundaryMode(), prevMaxX, calculatedMaxX);
                calculatedMinX = getCalculatedLowerBoundary(
                        constraints.getDomainLowerBoundaryMode(), prevMinX, calculatedMinX);
                calculatedMinX = applyUserMinMax(calculatedMinX, domainLeftMin,
                        domainLeftMax);
                calculatedMaxX = applyUserMinMax(calculatedMaxX,
                        domainRightMin, domainRightMax);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Domain Framing Model not yet supported: " + constraints.getDomainFramingModel());
        }

        switch (constraints.getRangeFramingModel()) {
            case ORIGIN:
                updateRangeMinMaxForOriginModel();
                break;
            case EDGE:
            	if (getSeriesRegistry().size() > 0) {
                    calculatedMaxY = getCalculatedUpperBoundary(
                            constraints.getRangeUpperBoundaryMode(), prevMaxY, calculatedMaxY);
                    calculatedMinY = getCalculatedLowerBoundary(
                            constraints.getRangeLowerBoundaryMode(), prevMinY, calculatedMinY);
	                calculatedMinY = applyUserMinMax(calculatedMinY, rangeBottomMin, rangeBottomMax);
	                calculatedMaxY = applyUserMinMax(calculatedMaxY, rangeTopMin, rangeTopMax);
            	}
                break;
            default:
                throw new UnsupportedOperationException(
                        "Range Framing Model not yet supported: " + constraints.getRangeFramingModel());
        }

        calculatedDomainOrigin = userDomainOrigin != null ?
                userDomainOrigin : getCalculatedMinX();

        calculatedRangeOrigin = this.userRangeOrigin != null ?
                userRangeOrigin : getCalculatedMinY();
    }

    protected Number getCalculatedUpperBoundary(BoundaryMode mode, Number previousMax, Number calculatedMax) {
        switch (mode) {
            case FIXED:
                break;
            case AUTO:
                break;
            case GROW:
                if (!(previousMax == null || calculatedMax.doubleValue() > previousMax.doubleValue())) {
                    calculatedMax = previousMax;
                }
                break;
            case SHRINK:
                if (!(previousMax == null || calculatedMax.doubleValue() < previousMax.doubleValue())) {
                    calculatedMax = previousMax;
                }
                break;
            default:
                throw new UnsupportedOperationException("BoundaryMode not supported: " + mode);
        }
        return calculatedMax;
    }

    protected Number getCalculatedLowerBoundary(BoundaryMode mode, Number previousMin, Number calculatedMin) {
        switch (mode) {
            case FIXED:
                break;
            case AUTO:
                break;
            case GROW:
                if (!(previousMin == null || calculatedMin.doubleValue() < previousMin.doubleValue())) {
                    return previousMin;
                }
                break;
            case SHRINK:
                if (!(previousMin == null || calculatedMin.doubleValue() > previousMin.doubleValue())) {
                    return previousMin;
                }
                break;
            default:
                throw new UnsupportedOperationException(
                        "BoundaryMode not supported: " + mode);
        }
        return calculatedMin;
    }

    /**
     * Apply user supplied min and max to the calculated boundary value.
     *
     * @param value
     * @param min
     * @param max
     */
    private Number applyUserMinMax(Number value, Number min, Number max) {
        value = (((min == null) || (value == null) || (value.doubleValue() > min.doubleValue()))
                ? value
                : min);
        value = (((max == null) || (value == null) || (value.doubleValue() < max.doubleValue()))
                ? value
                : max);
        return value;
    }

    /**
     * Centers the domain axis on origin.
     *
     * @param origin
     */
    public void centerOnDomainOrigin(Number origin) {
        centerOnDomainOrigin(origin, null, BoundaryMode.AUTO);
    }

    /**
     * Centers the domain on origin, calculating the upper and lower boundaries of the axis
     * using mode and extent.
     *
     * @param origin
     * @param extent
     * @param mode
     */
    public void centerOnDomainOrigin(Number origin, Number extent, BoundaryMode mode) {
        if (origin == null) {
            throw new NullPointerException("Origin param cannot be null.");
        }
        constraints.setDomainFramingModel(XYFramingModel.ORIGIN);
        setUserDomainOrigin(origin);
        domainOriginExtent = extent;
        domainOriginBoundaryMode = mode;

        Number[] minMax = getOriginMinMax(domainOriginBoundaryMode, userDomainOrigin, domainOriginExtent);
        constraints.setMinX(minMax[0]);
        constraints.setMaxX(minMax[1]);
    }

    /**
     * Centers the range axis on origin.
     *
     * @param origin
     */
    public void centerOnRangeOrigin(Number origin) {
        centerOnRangeOrigin(origin, null, BoundaryMode.AUTO);
    }

    /**
     * Centers the domain on origin, calculating the upper and lower boundaries of the axis
     * using mode and extent.
     *
     * @param origin
     * @param extent
     * @param mode
     */
    @SuppressWarnings("SameParameterValue")
    public void centerOnRangeOrigin(Number origin, Number extent, BoundaryMode mode) {
        if (origin == null) {
            throw new NullPointerException("Origin param cannot be null.");
        }
        constraints.setRangeFramingModel(XYFramingModel.ORIGIN);
        setUserRangeOrigin(origin);
        rangeOriginExtent = extent;
        rangeOriginBoundaryMode = mode;

        Number[] minMax = getOriginMinMax(rangeOriginBoundaryMode, userRangeOrigin, rangeOriginExtent);
        constraints.setMinY(minMax[0]);
        constraints.setMaxY(minMax[1]);
    }

    /**
     *
     * @param mode
     * @param origin
     * @param extent
     * @return result[0] is min, result[1] is max
     */
    protected Number[] getOriginMinMax(BoundaryMode mode, Number origin, Number extent) {
        if (mode == BoundaryMode.FIXED) {
            double o = origin.doubleValue();
            double e = extent.doubleValue();
            return new Number[] {o - e, o + e};

        }
        return new Number[] {null, null};
    }

    /**
     * Returns the distance between x and y.
     * Result is never a negative number.
     *
     * @param x
     * @param y
     * @return
     */
    private double distance(double x, double y) {
        if (x > y) {
            return x - y;
        } else {
            return y - x;
        }
    }

    public void updateDomainMinMaxForOriginModel() {
        double origin = userDomainOrigin.doubleValue();
        double maxXDelta = distance(calculatedMaxX.doubleValue(), origin);
        double minXDelta = distance(calculatedMinX.doubleValue(), origin);
        double delta = maxXDelta > minXDelta ? maxXDelta : minXDelta;
        double dlb = origin - delta;
        double dub = origin + delta;
        switch (domainOriginBoundaryMode) {
            case AUTO:
                calculatedMinX = dlb;
                calculatedMaxX = dub;

                break;
            // if fixed, then the value already exists within "user" vals.
            case FIXED:
                break;
            case GROW: {

                if (prevMinX == null || dlb < prevMinX.doubleValue()) {
                    calculatedMinX = dlb;
                } else {
                    calculatedMinX = prevMinX;
                }

                if (prevMaxX == null || dub > prevMaxX.doubleValue()) {
                    calculatedMaxX = dub;
                } else {
                    calculatedMaxX = prevMaxX;
                }
            }
            break;
            case SHRINK:
                if (prevMinX == null || dlb > prevMinX.doubleValue()) {
                    calculatedMinX = dlb;
                } else {
                    calculatedMinX = prevMinX;
                }

                if (prevMaxX == null || dub < prevMaxX.doubleValue()) {
                    calculatedMaxX = dub;
                } else {
                    calculatedMaxX = prevMaxX;
                }
                break;
            default:
                throw new UnsupportedOperationException("Domain Origin Boundary Mode not yet supported: " + domainOriginBoundaryMode);
        }
    }

    public void updateRangeMinMaxForOriginModel() {
        switch (rangeOriginBoundaryMode) {
            case AUTO:
                double origin = userRangeOrigin.doubleValue();
                double maxYDelta = distance(calculatedMaxY.doubleValue(), origin);
                double minYDelta = distance(calculatedMinY.doubleValue(), origin);
                if (maxYDelta > minYDelta) {
                    calculatedMinY = origin - maxYDelta;
                    calculatedMaxY = origin + maxYDelta;
                } else {
                    calculatedMinY = origin - minYDelta;
                    calculatedMaxY = origin + minYDelta;
                }
                break;
            case FIXED:
            case GROW:
            case SHRINK:
            default:
                throw new UnsupportedOperationException(
                        "Range Origin Boundary Mode not yet supported: " + rangeOriginBoundaryMode);
        }
    }

    /**
     * Convenience method - wraps XYGraphWidget.getTicksPerRangeLabel().
     * Equivalent to getGraphWidget().getTicksPerRangeLabel().
     *
     * @return
     */
    public int getTicksPerRangeLabel() {
        return graphWidget.getTicksPerRangeLabel();
    }

    /**
     * Convenience method - wraps XYGraphWidget.setTicksPerRangeLabel().
     * Equivalent to getGraphWidget().setTicksPerRangeLabel().
     *
     * @param ticksPerRangeLabel
     */
    public void setTicksPerRangeLabel(int ticksPerRangeLabel) {
        graphWidget.setTicksPerRangeLabel(ticksPerRangeLabel);
    }

    /**
     * Convenience method - wraps XYGraphWidget.getTicksPerDomainLabel().
     * Equivalent to getGraphWidget().getTicksPerDomainLabel().
     *
     * @return
     */
    public int getTicksPerDomainLabel() {
        return graphWidget.getTicksPerDomainLabel();
    }

    /**
     * Convenience method - wraps XYGraphWidget.setTicksPerDomainLabel().
     * Equivalent to getGraphWidget().setTicksPerDomainLabel().
     *
     * @param ticksPerDomainLabel
     */
    public void setTicksPerDomainLabel(int ticksPerDomainLabel) {
        graphWidget.setTicksPerDomainLabel(ticksPerDomainLabel);
    }

    public XYStepMode getDomainStepMode() {
        return domainStepModel.getMode();
    }

    public void setDomainStepMode(XYStepMode domainStepMode) {
        domainStepModel.setMode(domainStepMode);
    }

    public double getDomainStepValue() {
        return domainStepModel.getValue();
    }

    public void setDomainStepValue(double domainStepValue) {
        domainStepModel.setValue(domainStepValue);
    }

    public void setDomainStep(XYStepMode mode, double value) {
        setDomainStepMode(mode);
        setDomainStepValue(value);
    }

    public XYStepMode getRangeStepMode() {
        return rangeStepModel.getMode();
    }

    public void setRangeStepMode(XYStepMode rangeStepMode) {
        rangeStepModel.setMode(rangeStepMode);
    }

    public double getRangeStepValue() {
        return rangeStepModel.getValue();
    }

    public void setRangeStepValue(double rangeStepValue) {
        rangeStepModel.setValue(rangeStepValue);
    }

    public void setRangeStep(XYStepMode mode, double value) {
        setRangeStepMode(mode);
        setRangeStepValue(value);
    }

    public String getDomainLabel() {
        return getDomainLabelWidget().getText();
    }

    public void setDomainLabel(String domainLabel) {
        getDomainLabelWidget().setText(domainLabel);
    }

    public String getRangeLabel() {
        return getRangeLabelWidget().getText();
    }

    public void setRangeLabel(String rangeLabel) {
        getRangeLabelWidget().setText(rangeLabel);
    }

    public XYLegendWidget getLegendWidget() {
        return legendWidget;
    }

    public void setLegendWidget(XYLegendWidget legendWidget) {
        this.legendWidget = legendWidget;
    }

    public XYGraphWidget getGraphWidget() {
        return graphWidget;
    }

    public void setGraphWidget(XYGraphWidget graphWidget) {
        this.graphWidget = graphWidget;
    }

    public TextLabelWidget getDomainLabelWidget() {
        return domainLabelWidget;
    }

    public void setDomainLabelWidget(TextLabelWidget domainLabelWidget) {
        this.domainLabelWidget = domainLabelWidget;
    }

    public TextLabelWidget getRangeLabelWidget() {
        return rangeLabelWidget;
    }

    public void setRangeLabelWidget(TextLabelWidget rangeLabelWidget) {
        this.rangeLabelWidget = rangeLabelWidget;
    }

    /**
     * Convenience method - wraps XYGraphWidget.getRangeValueFormat().
     *
     * @return
     */
    public Format getRangeValueFormat() {
        return graphWidget.getRangeValueFormat();
    }

    /**
     * Convenience method - wraps XYGraphWidget.setRangeValueFormat().
     *
     * @param rangeValueFormat
     */
    public void setRangeValueFormat(Format rangeValueFormat) {
        graphWidget.setRangeValueFormat(rangeValueFormat);
    }

    /**
     * Convenience method - wraps XYGraphWidget.getDomainValueFormat().
     *
     * @return
     */
    public Format getDomainValueFormat() {
        return graphWidget.getDomainValueFormat();
    }

    /**
     * Convenience method - wraps XYGraphWidget.setDomainValueFormat().
     *
     * @param domainValueFormat
     */
    public void setDomainValueFormat(Format domainValueFormat) {
        graphWidget.setDomainValueFormat(domainValueFormat);
    }

    /**
     * Setup the boundary mode, boundary values only applicable in FIXED mode.
     *
     * @param lowerBoundary
     * @param upperBoundary
     * @param mode
     */
    public synchronized void setDomainBoundaries(Number lowerBoundary, Number upperBoundary, BoundaryMode mode) {
        setDomainBoundaries(lowerBoundary, mode, upperBoundary, mode);
    }

    /**
     * Setup the boundary mode, boundary values only applicable in FIXED mode.
     *
     * @param lowerBoundary
     * @param lowerBoundaryMode
     * @param upperBoundary
     * @param upperBoundaryMode
     */
    public synchronized void setDomainBoundaries(Number lowerBoundary, BoundaryMode lowerBoundaryMode,
                                                 Number upperBoundary, BoundaryMode upperBoundaryMode) {
        setDomainLowerBoundary(lowerBoundary, lowerBoundaryMode);
        setDomainUpperBoundary(upperBoundary, upperBoundaryMode);
    }

    /**
     * Setup the boundary mode, boundary values only applicable in FIXED mode.
     *
     * @param lowerBoundary
     * @param upperBoundary
     * @param mode
     */
    public synchronized void setRangeBoundaries(Number lowerBoundary, Number upperBoundary, BoundaryMode mode) {
        setRangeBoundaries(lowerBoundary, mode, upperBoundary, mode);
    }

    /**
     * Setup the boundary mode, boundary values only applicable in FIXED mode.
     *
     * @param lowerBoundary
     * @param lowerBoundaryMode
     * @param upperBoundary
     * @param upperBoundaryMode
     */
    public synchronized void setRangeBoundaries(Number lowerBoundary, BoundaryMode lowerBoundaryMode,
                                                Number upperBoundary, BoundaryMode upperBoundaryMode) {
        setRangeLowerBoundary(lowerBoundary, lowerBoundaryMode);
        setRangeUpperBoundary(upperBoundary, upperBoundaryMode);
    }

    protected synchronized void setDomainUpperBoundaryMode(BoundaryMode mode) {
        constraints.setDomainUpperBoundaryMode(mode);
    }

    protected synchronized void setUserMaxX(Number maxX) {
        constraints.setMaxX(maxX);
    }

    /**
     * Setup the boundary mode, boundary values only applicable in FIXED mode.
     *
     * @param boundary
     * @param mode
     */
    public synchronized void setDomainUpperBoundary(Number boundary, BoundaryMode mode) {
        setUserMaxX((mode == BoundaryMode.FIXED) ? boundary : null);
        setDomainUpperBoundaryMode(mode);
        setDomainFramingModel(XYFramingModel.EDGE);
    }

    protected synchronized void setDomainLowerBoundaryMode(BoundaryMode mode) {
        constraints.setDomainLowerBoundaryMode(mode);
    }

    protected synchronized void setUserMinX(Number minX) {
        constraints.setMinX(minX);
    }

    /**
     * Setup the boundary mode, boundary values only applicable in FIXED mode.
     *
     * @param boundary
     * @param mode
     */
    public synchronized void setDomainLowerBoundary(Number boundary, BoundaryMode mode) {
        setUserMinX((mode == BoundaryMode.FIXED) ? boundary : null);
        setDomainLowerBoundaryMode(mode);
        setDomainFramingModel(XYFramingModel.EDGE);
    }

    protected synchronized void setRangeUpperBoundaryMode(BoundaryMode mode) {
        constraints.setRangeUpperBoundaryMode(mode);
    }

    protected synchronized void setUserMaxY(Number maxY) {
        constraints.setMaxY(maxY);
    }

    /**
     * Setup the boundary mode, boundary values only applicable in FIXED mode.
     *
     * @param boundary
     * @param mode
     */
    public synchronized void setRangeUpperBoundary(Number boundary, BoundaryMode mode) {
        setUserMaxY((mode == BoundaryMode.FIXED) ? boundary : null);
        setRangeUpperBoundaryMode(mode);
        setRangeFramingModel(XYFramingModel.EDGE);
    }

    protected synchronized void setRangeLowerBoundaryMode(BoundaryMode mode) {
        constraints.setRangeLowerBoundaryMode(mode);
    }

    protected synchronized void setUserMinY(Number minY) {
        constraints.setMinY(minY);
    }

    /**
     * Setup the boundary mode, boundary values only applicable in FIXED mode.
     *
     * @param boundary
     * @param mode
     */
    public synchronized void setRangeLowerBoundary(Number boundary, BoundaryMode mode) {
        setUserMinY((mode == BoundaryMode.FIXED) ? boundary : null);
        setRangeLowerBoundaryMode(mode);
        setRangeFramingModel(XYFramingModel.EDGE);
    }

    public Number getDomainOrigin() {
        return calculatedDomainOrigin;
    }

    public Number getRangeOrigin() {
        return calculatedRangeOrigin;
    }

    public synchronized void setUserDomainOrigin(Number origin) {
        if (origin == null) {
            throw new NullPointerException("Origin value cannot be null.");
        }
        this.userDomainOrigin = origin;
    }

    public synchronized void setUserRangeOrigin(Number origin) {
        if (origin == null) {
            throw new NullPointerException("Origin value cannot be null.");
        }
        this.userRangeOrigin = origin;
    }

    @SuppressWarnings("SameParameterValue")
    protected void setDomainFramingModel(XYFramingModel model) {
        constraints.setDomainFramingModel(model);
    }

    @SuppressWarnings("SameParameterValue")
    protected void setRangeFramingModel(XYFramingModel model) {
        constraints.setRangeFramingModel(model);
    }

    /**
     * CalculatedMinX value after the the framing model has been applied.
     *
     * @return
     */
    public Number getCalculatedMinX() {
        return calculatedMinX != null ? calculatedMinX : getDefaultBounds().getMinX();
    }

    /**
     * CalculatedMaxX value after the the framing model has been applied.
     *
     * @return
     */
    public Number getCalculatedMaxX() {
        return calculatedMaxX != null ? calculatedMaxX : getDefaultBounds().getMaxX();
    }

    /**
     * CalculatedMinY value after the the framing model has been applied.
     *
     * @return
     */
    public Number getCalculatedMinY() {
        return calculatedMinY != null ? calculatedMinY : getDefaultBounds().getMinY();
    }

    /**
     * CalculatedMaxY value after the the framing model has been applied.
     *
     * @return
     */
    public Number getCalculatedMaxY() {
        return calculatedMaxY != null ? calculatedMaxY : getDefaultBounds().getMaxY();
    }

    public boolean isDrawDomainOriginEnabled() {
        return drawDomainOriginEnabled;
    }

    public void setDrawDomainOriginEnabled(boolean drawDomainOriginEnabled) {
        this.drawDomainOriginEnabled = drawDomainOriginEnabled;
    }

    public boolean isDrawRangeOriginEnabled() {
        return drawRangeOriginEnabled;
    }

    public void setDrawRangeOriginEnabled(boolean drawRangeOriginEnabled) {
        this.drawRangeOriginEnabled = drawRangeOriginEnabled;
    }

    /**
     * Appends the specified marker to the end of plot's yValueMarkers list.
     *
     * @param marker The YValueMarker to be added.
     * @return true if the object was successfully added, false otherwise.
     */
    public boolean addMarker(YValueMarker marker) {
        if (yValueMarkers.contains(marker)) {
            return false;
        } else {
            return yValueMarkers.add(marker);
        }
    }

    /**
     * Removes the specified marker from the plot.
     *
     * @param marker
     * @return The YValueMarker removed if successfull,  null otherwise.
     */
    public YValueMarker removeMarker(YValueMarker marker) {
        int markerIndex = yValueMarkers.indexOf(marker);
        if (markerIndex == -1) {
            return null;
        } else {
            return yValueMarkers.remove(markerIndex);
        }
    }

    /**
     * Convenience method - combines removeYMarkers() and removeXMarkers().
     *
     * @return
     */
    public int removeMarkers() {
        int removed = removeXMarkers();
        removed += removeYMarkers();
        return removed;
    }

    /**
     * Removes all YValueMarker instances from the plot.
     *
     * @return
     */
    public int removeYMarkers() {
        int numMarkersRemoved = yValueMarkers.size();
        yValueMarkers.clear();
        return numMarkersRemoved;
    }

    /**
     * Appends the specified marker to the end of plot's xValueMarkers list.
     *
     * @param marker The XValueMarker to be added.
     * @return true if the object was successfully added, false otherwise.
     */
    public boolean addMarker(XValueMarker marker) {
        return !xValueMarkers.contains(marker) && xValueMarkers.add(marker);
    }

    /**
     * Removes the specified marker from the plot.
     *
     * @param marker
     * @return The XValueMarker removed if successfull,  null otherwise.
     */
    public XValueMarker removeMarker(XValueMarker marker) {
        int markerIndex = xValueMarkers.indexOf(marker);
        if (markerIndex == -1) {
            return null;
        } else {
            return xValueMarkers.remove(markerIndex);
        }
    }

    /**
     * Removes all XValueMarker instances from the plot.
     *
     * @return
     */
    public int removeXMarkers() {
        int numMarkersRemoved = xValueMarkers.size();
        xValueMarkers.clear();
        return numMarkersRemoved;
    }

    protected List<YValueMarker> getYValueMarkers() {
        return yValueMarkers;
    }

    protected List<XValueMarker> getXValueMarkers() {
        return xValueMarkers;
    }

    public RectRegion getDefaultBounds() {
        return defaultBounds;
    }

    public void setDefaultBounds(RectRegion defaultBounds) {
        this.defaultBounds = defaultBounds;
    }

    /**
     * @return the rangeTopMin
     */
    public Number getRangeTopMin() {
        return rangeTopMin;
    }

    /**
     * @param rangeTopMin the rangeTopMin to set
     */
    public synchronized void setRangeTopMin(Number rangeTopMin) {
        this.rangeTopMin = rangeTopMin;
    }

    /**
     * @return the rangeTopMax
     */
    public Number getRangeTopMax() {
        return rangeTopMax;
    }

    /**
     * @param rangeTopMax the rangeTopMax to set
     */
    public synchronized void setRangeTopMax(Number rangeTopMax) {
        this.rangeTopMax = rangeTopMax;
    }

    /**
     * @return the rangeBottomMin
     */
    public Number getRangeBottomMin() {
        return rangeBottomMin;
    }

    /**
     * @param rangeBottomMin the rangeBottomMin to set
     */
    public synchronized void setRangeBottomMin(Number rangeBottomMin) {
        this.rangeBottomMin = rangeBottomMin;
    }

    /**
     * @return the rangeBottomMax
     */
    public Number getRangeBottomMax() {
        return rangeBottomMax;
    }

    /**
     * @param rangeBottomMax the rangeBottomMax to set
     */
    public synchronized void setRangeBottomMax(Number rangeBottomMax) {
        this.rangeBottomMax = rangeBottomMax;
    }

    /**
     * @return the domainLeftMin
     */
    public Number getDomainLeftMin() {
        return domainLeftMin;
    }

    /**
     * @param domainLeftMin the domainLeftMin to set
     */
    public synchronized void setDomainLeftMin(Number domainLeftMin) {
        this.domainLeftMin = domainLeftMin;
    }

    /**
     * @return the domainLeftMax
     */
    public Number getDomainLeftMax() {
        return domainLeftMax;
    }

    /**
     * @param domainLeftMax the domainLeftMax to set
     */
    public synchronized void setDomainLeftMax(Number domainLeftMax) {
        this.domainLeftMax = domainLeftMax;
    }

    /**
     * @return the domainRightMin
     */
    public Number getDomainRightMin() {
        return domainRightMin;
    }

    /**
     * @param domainRightMin the domainRightMin to set
     */
    public synchronized void setDomainRightMin(Number domainRightMin) {
        this.domainRightMin = domainRightMin;
    }

    /**
     * @return the domainRightMax
     */
    public Number getDomainRightMax() {
        return domainRightMax;
    }

    /**
     * @param domainRightMax the domainRightMax to set
     */
    public synchronized void setDomainRightMax(Number domainRightMax) {
        this.domainRightMax = domainRightMax;
    }

    public XYStepModel getDomainStepModel() {
        return domainStepModel;
    }

    public void setDomainStepModel(XYStepModel domainStepModel) {
        this.domainStepModel = domainStepModel;
    }

    public XYStepModel getRangeStepModel() {
        return rangeStepModel;
    }

    public void setRangeStepModel(XYStepModel rangeStepModel) {
        this.rangeStepModel = rangeStepModel;
    }
}