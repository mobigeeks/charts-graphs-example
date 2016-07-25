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

package com.androidplot.util;

import org.junit.Test;
import java.util.regex.Pattern;
import static junit.framework.Assert.assertTrue;

public class PixelUtilsTest {

    @org.junit.After
    public void tearDown() throws Exception {

    }

    @Test
    public void testDimensionPattern() {
        Pattern DIMENSION_PATTERN = Pattern.compile(PixelUtils.DIMENSION_REGEX);
        assertTrue("Dimension failed dimension pattern match", DIMENSION_PATTERN.matcher("20dp").matches());
        assertTrue("Negative dimension failed dimension pattern match", DIMENSION_PATTERN.matcher("-20dp").matches());
    }
}
