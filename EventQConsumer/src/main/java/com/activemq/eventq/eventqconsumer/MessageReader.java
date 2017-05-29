/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activemq.eventq.eventqconsumer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;

/**
 *
 * @author Scarwalker
 */
public class MessageReader implements Runnable{
    public static void main(String[] args){
        Runnable mr = new MessageReader();
        Thread t = new Thread(mr);
        t.start();
    }

    @Override
    public void run() {
        Connection connection = null;
        try{
            ActiveMQConnectionFactory jmsConnectionFactory = new ActiveMQConnectionFactory("tcp://deti-engsoft-11.ua.pt:61616");
            connection = jmsConnectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue mq_eventlog = session.createQueue("event_request");
            MessageConsumer consumer_event = session.createConsumer(mq_eventlog);
            
            //infinite loop that listens for messages
            while(true){
                TextMessage message = (TextMessage) consumer_event.receive();
                String payload = message.getText();
                System.out.println("Mensagem Recebida : "+payload);
                Queue mq_reply = session.createQueue("event_request_ack_reply");
                MessageProducer producer = session.createProducer(mq_reply);
                Queue mq_reply2 = session.createQueue("event_request_reply");
                MessageProducer producer2 = session.createProducer(mq_reply2);
                TextMessage ackMessage;
                TextMessage realMessage;
                if(payload.toLowerCase().equals("get event log")){
                    ackMessage = session.createTextMessage("GET EVENT LOG - RECEBIDO");
                    producer.send(ackMessage);
                    realMessage = session.createTextMessage("AQUI VAI A VERDADEIRA MENSAGEM");
                    producer2.send(realMessage);
                }else{
                    ackMessage = session.createTextMessage("GET EVENT LOG - MENSAGEM INVÁLIDA");
                    producer.send(ackMessage);
                    realMessage = session.createTextMessage("WRONG MESSAGE");
                    producer2.send(realMessage);
                }
            }

        }catch(JMSException e){
        }
        
        if(connection != null){
            try {
                connection.stop();
            } catch (JMSException ex) {
                Logger.getLogger(MessageReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
