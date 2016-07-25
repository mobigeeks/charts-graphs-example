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

package com.androidplot;

import android.content.res.TypedArray;
import android.graphics.*;
import com.androidplot.exception.PlotRenderException;
import com.androidplot.ui.RenderStack;
import com.androidplot.ui.SeriesAndFormatter;
import com.androidplot.ui.SeriesRenderer;
import com.androidplot.ui.Formatter;
import com.androidplot.util.Configurator;
import mockit.*;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PlotTest {

    static class MockPlotListener implements PlotListener {

        public void onBeforeDraw(Plot source, Canvas canvas) {}

        public void onAfterDraw(Plot source, Canvas canvas) {}
    }

    static class MockSeries implements Series {

        public String getTitle() {
            return null;
        }

    }

    static class MockSeries2 implements Series {

        public String getTitle() {
            return null;
        }
    }

    static class MockSeries3 implements Series {

        public String getTitle() {
            return null;
        }
    }

    static class MockRenderer1 extends SeriesRenderer {

        public MockRenderer1(Plot plot) {
            super(plot);
        }

        @Override
        public void onRender(Canvas canvas, RectF plotArea, Series series, Formatter formatter, RenderStack stack) throws PlotRenderException {

        }

        @Override
        public void doDrawLegendIcon(Canvas canvas, RectF rect, Formatter formatter) {

        }
    }
    static class MockRenderer2 extends SeriesRenderer {

        public MockRenderer2(Plot plot) {
            super(plot);
        }

        @Override
        public void onRender(Canvas canvas, RectF plotArea, Series series, Formatter formatter, RenderStack stack) throws PlotRenderException {

        }

        @Override
        public void doDrawLegendIcon(Canvas canvas, RectF rect, Formatter formatter) {

        }
    }

    static class MockFormatter1 extends Formatter<MockPlot> {

        @Override
        public Class<? extends SeriesRenderer> getRendererClass() {
            return MockRenderer1.class;
        }

        @Override
        public SeriesRenderer getRendererInstance(MockPlot plot) {
            return new MockRenderer1(plot);
        }
    }

    static class MockFormatter2 extends Formatter<MockPlot> {

        @Override
        public Class<? extends SeriesRenderer> getRendererClass() {
            return MockRenderer2.class;
        }

        @Override
        public SeriesRenderer getRendererInstance(MockPlot plot) {
            return new MockRenderer2(plot);
        }
    }

    //@MockClass(realClass = Plot.class)
    public static class MockPlot extends Plot<MockSeries, Formatter, SeriesRenderer> {
        public MockPlot(String title) {
            super(RuntimeEnvironment.application, title);
        }

        @Override
        protected void onPreInit() {

        }

        @Override
        protected void processAttrs(TypedArray attrs) {

        }

        /*@Override
        protected SeriesRenderer doGetRendererInstance(Class clazz) {
            if(clazz == MockRenderer1.class) {
                return new MockRenderer1(this);
            } else if(clazz == MockRenderer2.class) {
                return new MockRenderer2(this);
            } else {
                return null;
            }
        }*/
    }

    /*@Before
    public void setUp() throws Exception {
        Mockit.setUpMocks(MockPaint.class,MockContext.class);
    }*/

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testAddSeries() throws Exception {
        Plot plot = new MockPlot("MockPlot");

        MockSeries m1 = new MockSeries();
        Class cl = MockRenderer1.class;

        plot.addSeries(m1, new MockFormatter1());
        assertEquals(1, plot.getSeriesRegistry().size());

        // duplicate Renderer added, registry size should not grow:
        plot.addSeries(m1, new MockFormatter1());

        assertEquals(1, plot.getRenderers().size());
        assertEquals(1, plot.getRenderer(cl).getSeriesList().size());

        MockSeries m2 = new MockSeries();

        plot.addSeries(m2, new MockFormatter1());

        // still should only be one renderer type:
        assertEquals(1, plot.getRendererList().size());

        // we added a new instance of cl to the renderer so there should be 2 in the subregistry:
        assertEquals(2, plot.getRenderer(cl).getSeriesList().size());


        // lets add another renderer:
        plot.addSeries(m1, new MockFormatter2());

        assertEquals(2, plot.getRendererList().size());
    }

    @Test
    public void testRemoveSeries() throws Exception {

        Plot plot = new MockPlot("MockPlot");

        MockSeries m1 = new MockSeries();
        MockSeries m2 = new MockSeries();
        MockSeries m3 = new MockSeries();

        plot.addSeries(m1, new MockFormatter1());
        plot.addSeries(m2, new MockFormatter1());
        plot.addSeries(m3, new MockFormatter1());

        plot.addSeries(m1, new MockFormatter2());
        plot.addSeries(m2, new MockFormatter2());
        plot.addSeries(m3, new MockFormatter2());


        // a quick sanity check:
        assertEquals(2, plot.getRendererList().size());
        assertEquals(3, plot.getRenderer(MockRenderer1.class).getSeriesList().size());
        assertEquals(3, plot.getRenderer(MockRenderer2.class).getSeriesList().size());

        plot.removeSeries(m1, MockRenderer1.class);
        assertEquals(2, plot.getRenderer(MockRenderer1.class).getSeriesList().size());

        plot.removeSeries(m2, MockRenderer1.class);
        assertEquals(1, plot.getRenderer(MockRenderer1.class).getSeriesList().size());

        plot.removeSeries(m2, MockRenderer1.class);
        assertEquals(1, plot.getRenderer(MockRenderer1.class).getSeriesList().size());

        plot.removeSeries(m3, MockRenderer1.class);

        // add em all back
        plot.addSeries(m1, new MockFormatter1());
        plot.addSeries(m2, new MockFormatter1());
        plot.addSeries(m3, new MockFormatter1());

        plot.addSeries(m1, new MockFormatter1());
        plot.addSeries(m2, new MockFormatter1());
        plot.addSeries(m3, new MockFormatter1());


        // a quick sanity check:
        assertEquals(2, plot.getRendererList().size());
        assertEquals(3, plot.getRenderer(MockRenderer1.class).getSeriesList().size());
        assertEquals(3, plot.getRenderer(MockRenderer2.class).getSeriesList().size());

        // now lets try removing a series from all renderers:
        plot.removeSeries(m1);
        assertEquals(2, plot.getRenderer(MockRenderer1.class).getSeriesList().size());
        assertEquals(2, plot.getRenderer(MockRenderer2.class).getSeriesList().size());

        // and now lets remove the remaining series:
        plot.removeSeries(m2);
        plot.removeSeries(m3);
    }


    @Test
    public void testGetFormatter() throws Exception {
        Plot plot = new MockPlot("MockPlot");

        MockSeries m1 = new MockSeries();
        MockSeries m2 = new MockSeries();
        MockSeries m3 = new MockSeries();

        MockFormatter1 f1 = new MockFormatter1();
        MockFormatter1 f2 = new MockFormatter1();
        MockFormatter2 f3 = new MockFormatter2();

        plot.addSeries(m1, f1);
        plot.addSeries(m2, f2);
        plot.addSeries(m3, new MockFormatter1());

        plot.addSeries(m1, new MockFormatter1());
        plot.addSeries(m2, f3);
        plot.addSeries(m3, new MockFormatter1());

        assertEquals(plot.getRenderer(MockRenderer1.class).getFormatter(m1), f1);
        assertEquals(plot.getRenderer(MockRenderer1.class).getFormatter(m2), f2);
        assertEquals(plot.getRenderer(MockRenderer2.class).getFormatter(m2), f3);

        assertNotSame(plot.getRenderer(MockRenderer2.class).getFormatter(m2), f1);

    }

    @Test
    public void testGetRendererList() throws Exception {

        Plot plot = new MockPlot("MockPlot");

        MockSeries m1 = new MockSeries();
        MockSeries m2 = new MockSeries();
        MockSeries m3 = new MockSeries();

        plot.addSeries(m1, new MockFormatter1());
        plot.addSeries(m2, new MockFormatter1());
        plot.addSeries(m3, new MockFormatter1());

        plot.addSeries(m1, new MockFormatter2());
        plot.addSeries(m2, new MockFormatter2());
        plot.addSeries(m3, new MockFormatter2());

        List<SeriesRenderer> rList = plot.getRendererList();
        assertEquals(2, rList.size());
    }

    @Test
    public void testAddListener() throws Exception {
        Plot plot = new MockPlot("MockPlot");
        ArrayList<PlotListener> listeners = Deencapsulation.getField(plot, "listeners");

        assertEquals(0, listeners.size());

        MockPlotListener pl1 = new MockPlotListener();
        MockPlotListener pl2 = new MockPlotListener();

        plot.addListener(pl1);

        assertEquals(1, listeners.size());

        // should return false on a double entry attempt
        assertFalse(plot.addListener(pl1));

        // make sure the listener wasnt added anyway:
        assertEquals(1, listeners.size());

        plot.addListener(pl2);

        assertEquals(2, listeners.size());
                
    }

    @Test
    public void testRemoveListener() throws Exception {
        Plot plot = new MockPlot("MockPlot");
        ArrayList<PlotListener> listeners = Deencapsulation.getField(plot, "listeners");

        assertEquals(0, listeners.size());

        MockPlotListener pl1 = new MockPlotListener();
        MockPlotListener pl2 = new MockPlotListener();
        MockPlotListener pl3 = new MockPlotListener();

        plot.addListener(pl1);
        plot.addListener(pl2);

        assertEquals(2, listeners.size());

        assertFalse(plot.removeListener(pl3));

        assertTrue(plot.removeListener(pl1));

        assertEquals(1, listeners.size());

        assertFalse(plot.removeListener(pl1));

        assertEquals(1, listeners.size());

        assertTrue(plot.removeListener(pl2));

        assertEquals(0, listeners.size());

    }

    @Test
    public void testConfigure() throws Exception {
        Plot plot = new MockPlot("MockPlot");

        HashMap<String, String> params = new HashMap<String, String>();
        String param1 = "this is a test.";
        String param2 = "use_background_thread";
        String param3 = "#FF0000";
        params.put("title", param1);
        params.put("renderMode", param2);
        params.put("backgroundPaint.color", param3);

        Configurator.configure(RuntimeEnvironment.application, plot, params);

        assertEquals(param1, plot.getTitle());
        assertEquals(Plot.RenderMode.USE_BACKGROUND_THREAD, plot.getRenderMode());
        assertEquals(Color.parseColor(param3), plot.getBackgroundPaint().getColor());
    }
}
