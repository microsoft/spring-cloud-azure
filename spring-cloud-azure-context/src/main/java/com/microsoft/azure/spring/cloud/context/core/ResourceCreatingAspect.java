/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core;

import com.microsoft.azure.CloudException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

/**
 * Aspect to handle azure resource creation exception and log
 *
 * @author Warren Zhu
 */
@Aspect
public class ResourceCreatingAspect {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceCreatingAspect.class);

    private static String getResourceName(Object[] args) {
        Assert.notEmpty(args, "Create method args should not be empty");
        Object lastArg = args[args.length - 1];

        if (lastArg instanceof String) {
            return (String) lastArg;
        }

        // When use tuple as creator parameter, key is always second of Tuple
        if (lastArg instanceof Tuple) {
            Tuple tuple = (Tuple) lastArg;
            return (String) tuple.getSecond();
        }

        throw new IllegalArgumentException("Create method parameter must be String or Tuple");
    }

    @Around("execution(* com.microsoft.azure.spring.cloud.context.core.AzureAdmin.getOrCreate*(..))")
    public Object handleExceptionAndLog(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String name = getResourceName(joinPoint.getArgs());
        String methodName = joinPoint.getSignature().getName();
        LOG.info("Calling {} with name '{}' ...", joinPoint.getSignature().getName(), name);

        try {
            return joinPoint.proceed();
        } catch (CloudException e) {
            LOG.error("{} with name '{}' failed due to: {}", methodName, name, e.getMessage());
            throw new RuntimeException(
                    String.format("%s with name '%s' failed due to: %s", methodName, name, e.getMessage()));
        } finally {
            stopWatch.stop();
            LOG.info("{} with name '{}' finished in {} seconds", methodName, name, stopWatch.getTotalTimeSeconds());
        }
    }

}
