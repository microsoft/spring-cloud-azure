/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eventhub.integration;

/**
 * Azure event hub internal headers for Spring Messaging messages.
 *
 * @author Warren Zhu
 */
public class EventHubHeaders {

    private static final String PREFIX = "azure_event_hub_";

    public static final String PARTITION_ID = PREFIX + "partition_id";

    public static final String PARTITION_KEY = PREFIX + "partition_key";

    public static final String NAME = PREFIX + "name";
}
