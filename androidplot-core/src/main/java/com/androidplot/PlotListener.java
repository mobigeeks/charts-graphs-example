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

import android.graphics.Canvas;

/**
 * Defines methods used for monitoring events generated by a Plot.
 */
public interface PlotListener {

    /**
     * Fired immediately before the Plot "source" is drawn onto canvas.
     * Commonly used by implementing Series instances to activate a read
     * lock on it's self in preparation for the Plot's imminent reading
     * of that series.
     * @param source
     * @param canvas
     */
    public void onBeforeDraw(Plot source, Canvas canvas);

    /**
     * Fired immediately after the Plot "source" is drawn onto canvas.
     * Just as onBeforeDraw(...) is commonly used by Series implementations
     * to activate a read lock, this method is commonly used to release that
     * same lock.
     * @param source
     * @param canvas
     */
    public void onAfterDraw(Plot source, Canvas canvas);
}
