/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.messaging.container;

import com.microsoft.azure.spring.messaging.listener.AzureMessageHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;

@Slf4j
@Getter
@Setter
abstract class AbstractListenerContainer implements BeanNameAware, DisposableBean, MessageListenerContainer {
    private final Object lifecycleMonitor = new Object();
    private String destination;
    private String group;
    private AzureMessageHandler messageHandler;
    private boolean autoStartup = true;
    private int phase = 0;

    //Settings that are changed at runtime
    private boolean active;
    private boolean running;
    private String beanName;

    protected abstract void doStart();

    protected abstract void doStop();

    protected void doDestroy() {
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public void start() {
        log.debug("Starting container with name {}", getBeanName());
        synchronized (this.getLifecycleMonitor()) {
            this.running = true;
            this.getLifecycleMonitor().notifyAll();
        }
        doStart();
    }

    @Override
    public void stop() {
        log.debug("Stopping container with name {}", getBeanName());
        synchronized (this.getLifecycleMonitor()) {
            this.running = false;
            this.getLifecycleMonitor().notifyAll();
        }
        doStop();
    }

    @Override
    public boolean isRunning() {
        synchronized (this.getLifecycleMonitor()) {
            return this.running;
        }
    }

    @Override
    public void destroy() {
        synchronized (this.lifecycleMonitor) {
            stop();
            this.active = false;
            doDestroy();
        }
    }

}
