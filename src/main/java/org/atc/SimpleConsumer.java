/*
 * Copyright 2015 Asitha Nanayakkara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atc;

import org.atc.config.SubscriberConfig;

import javax.jms.MessageConsumer;
import javax.naming.NamingException;

/**
 * Generic interface for a message consumer used by the {@link org.atc.ConsumerThread} to consume
 * messages
 */
public interface SimpleConsumer {

    SubscriberConfig getConfigs();

    ATCMessage receive() throws ATCException;

    void close() throws ATCException;

    void unsubscribe() throws ATCException;

    MessageConsumer subscribe(SubscriberConfig conf) throws NamingException, ATCException;
}
