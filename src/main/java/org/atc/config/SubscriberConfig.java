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

package org.atc.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubscriberConfig extends PubSubConfig {

    @XmlAttribute
    private String subscriptionID;
    @XmlAttribute
    private boolean unsubscribeOnFinish;
    @XmlAttribute
    private boolean enableClientAcknowledgment;
    @XmlAttribute
    private long receiveWaitTimeMillis;

    final void setSubscriptionID(String subscriptionID) {
        this.subscriptionID = subscriptionID;
    }

    public final String getSubscriptionID() {
        return subscriptionID;
    }

    public final boolean isUnsubscribeOnFinish() {
        return unsubscribeOnFinish;
    }

    final void setUnsubscribeOnFinish(boolean unsubscribeOnFinish) {
        this.unsubscribeOnFinish = unsubscribeOnFinish;
    }

    final SubscriberConfig copy() throws NoSuchFieldException, IllegalAccessException {
        SubscriberConfig copy = new SubscriberConfig();
        copyMembers(this, copy);
        return copy;
    }

    public boolean isEnableClientAcknowledgment() {
        return enableClientAcknowledgment;
    }

    public void setEnableClientAcknowledgment(boolean enableClientAcknowledgment) {
        this.enableClientAcknowledgment = enableClientAcknowledgment;
    }

    public long getReceiveWaitTimeMillis() {
        return receiveWaitTimeMillis;
    }

    public void setReceiveWaitTimeMillis(long receiveWaitTimeMillis) {
        this.receiveWaitTimeMillis = receiveWaitTimeMillis;
    }
}
