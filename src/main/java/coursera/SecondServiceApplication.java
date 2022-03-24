package coursera;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import coursera.service.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

@SpringBootApplication
@EnableJpaRepositories
public class SecondServiceApplication {

    @Autowired
    Receiver receiver;
    private final String QUEUE_NAME = "spring-boot-queue";

    @Bean
    ConnectionFactory connectionFactory() {
        return new RMQConnectionFactory();
    }

    @Bean
    public Destination jmsDestination() {
        RMQDestination jmsDestination = new RMQDestination();
        jmsDestination.setAmqpRoutingKey("attempts.key");
        jmsDestination.setAmqp(true);
        jmsDestination.setAmqpQueueName(QUEUE_NAME);
        return jmsDestination;
    }

    @Bean
    public DefaultMessageListenerContainer jmsListener(ConnectionFactory connectionFactory) {
        DefaultMessageListenerContainer jmsListener = new DefaultMessageListenerContainer();
        jmsListener.setConnectionFactory(connectionFactory);
        jmsListener.setDestination(jmsDestination());
        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver);
        jmsListener.setMessageListener(adapter);
        return jmsListener;
    }


    public static void main(String[] args) {
        SpringApplication.run(SecondServiceApplication.class, args);
    }
}
