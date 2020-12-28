package ecomm.payments.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    // Bean di creazione di una mappa con i parametri di configurazione per il client kafka
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers); // Host e porta sulla quale kafka è in ascolto
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class); // Classe di serializzazione da utilizzare per serializzare la chiave
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class); // Classe di serializzazione da utilizzare per serializzare i valori
        return props;
    }
    // implementation for a singleton shared Producer instance.
    @Bean
    public ProducerFactory<String, String> producerFactory() { return new DefaultKafkaProducerFactory<>(producerConfigs()); }

    // KafkaTemplate fornisce le utility per inviare messaggi a kafka
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Bean che crea automaticamente il topic in kafka se non esiste
    // Possiamo creare un bean per ogni topic da creare
    @Bean
    public NewTopic topic1() {
        return TopicBuilder.name("http_errors").build();
    }
    @Bean
    public NewTopic topic2() { return TopicBuilder.name("orders").build(); }
    @Bean
    public NewTopic topic3() { return TopicBuilder.name("logging").build(); }
}
