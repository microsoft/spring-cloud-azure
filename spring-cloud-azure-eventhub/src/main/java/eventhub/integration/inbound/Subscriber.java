/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package eventhub.integration.inbound;

import java.util.function.Consumer;

/**
 * A {@link Subscriber} allows you to provide a {@link Consumer} to process messages received. It also provide a
 * {@link Checkpointer} callback to checkpoint the messages successfully processed
 *
 * @param <T> message type parameter
 *
 * @author Warren Zhu
 */
public interface Subscriber<T> {

    /**
     * Start receiving message, then process using provided {@link Consumer}
     */
    void subscribe(Consumer<Iterable<T>> consumer);

    /**
     * Stop receiving message
     */
    void unsubscribe();

    Checkpointer<T> getCheckpointer();
}
