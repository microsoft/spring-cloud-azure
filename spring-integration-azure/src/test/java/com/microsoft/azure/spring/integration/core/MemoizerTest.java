/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.mockito.Mockito.*;

public class MemoizerTest {
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";

    @Test
    public void memoize() {
        ExpensiveOperation expensiveOperation = mock(ExpensiveOperation.class);
        when(expensiveOperation.compute(INPUT)).thenReturn(OUTPUT);
        Function<String, String> memoized = Memoizer.memoize(expensiveOperation::compute);
        Assert.assertEquals(memoized.apply(INPUT), OUTPUT);
        Assert.assertEquals(memoized.apply(INPUT), OUTPUT);
        verify(expensiveOperation, times(1)).compute(INPUT);
    }

    @Test
    public void memoizeWithMap() {
        Map<String, String> map = new ConcurrentHashMap<>();
        ExpensiveOperation expensiveOperation = mock(ExpensiveOperation.class);
        when(expensiveOperation.compute(INPUT)).thenReturn(OUTPUT);
        Function<String, String> memoized = Memoizer.memoize(map, expensiveOperation::compute);
        Assert.assertEquals(memoized.apply(INPUT), OUTPUT);
        Assert.assertEquals(memoized.apply(INPUT), OUTPUT);
        verify(expensiveOperation, times(1)).compute(INPUT);
        Assert.assertTrue(map.size() == 1);
    }

    interface ExpensiveOperation{
        String compute(String input);
    }
}
