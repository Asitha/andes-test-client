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

package org.atc.amqp;


import org.atc.ATCMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Helper class to convert to and from {@link org.atc.ATCMessage} to {@link javax.jms.Message}
 */
public class MessageUtils {

    private MessageUtils() {
    }

    /**
     * Converts {@link javax.jms.Message} to an {@link org.atc.ATCMessage}
     * @param jmsMessage {@link javax.jms.Message} to be converted
     * @return corresponding {@link org.atc.ATCMessage} is returned
     * @throws JMSException
     */
    public static ATCMessage fromJMSToATC(Message jmsMessage) throws JMSException{

        ATCMessage message = new ATCMessage();
        message.setMessageID(jmsMessage.getJMSMessageID());
        message.setTimeStamp(jmsMessage.getJMSTimestamp());
        if(jmsMessage instanceof TextMessage) {
            TextMessage t = (TextMessage) jmsMessage;
            message.setContent(t.getText());
            return message;
        }
        return null;
    }

    /**
     * Converts from {@link org.atc.ATCMessage} to {@link javax.jms.Message}
     * @param session {@link javax.jms.Session} to create the new message. Session that is used to
     *                                         publish the {@link javax.jms.Message}
     * @param message {@link org.atc.ATCMessage} to be converted
     * @return {@link javax.jms.Message}
     * @throws JMSException
     */
    public static Message fromATCToJMS(Session session, ATCMessage message) throws JMSException {
        Message jmsMessage = session.createTextMessage(message.getStringContent());
        jmsMessage.setJMSTimestamp(message.getTimeStamp());
        jmsMessage.setJMSMessageID(message.getMessageID());
        return jmsMessage;
    }
}
