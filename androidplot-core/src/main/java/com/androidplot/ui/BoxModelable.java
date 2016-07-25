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

package com.androidplot.ui;

import android.graphics.RectF;

/**
 * Encapsulates the functionality of a BoxModel.
 * See http://www.w3.org/TR/CSS21/box.html for a good explanation of how
 * the box model works.
 */
public interface BoxModelable {
    /**
     * Returns a RectF instance describing the inner edge of the margin layer.
     * @param boundsRect
     * @return
     */
    RectF getMarginatedRect(RectF boundsRect);

    /**
     * Returns a RectF instance describing the inner edge of the padding layer.
     * @param marginRect
     * @return
     */
    RectF getPaddedRect(RectF marginRect);


    void setMargins(float left, float top, float right, float bottom);

    void setPadding(float left, float top, float right, float bottom);

    float getMarginLeft();

    void setMarginLeft(float marginLeft);

    float getMarginTop();

    void setMarginTop(float marginTop);

    float getMarginRight();

    void setMarginRight(float marginRight);

    float getMarginBottom();

    void setMarginBottom(float marginBottm);

    float getPaddingLeft();

    void setPaddingLeft(float paddingLeft);

    float getPaddingTop();

    void setPaddingTop(float paddingTop);

    float getPaddingRight();

    void setPaddingRight(float paddingRight);

    float getPaddingBottom();

    void setPaddingBottom(float paddingBottom);
}
