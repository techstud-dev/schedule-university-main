package com.techstud.scheduleuniversity.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.ssl.trust-store-location}")
    private String trustStoreLocation;

    @Value("${spring.kafka.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${spring.kafka.ssl.key-store-location}")
    private String keyStoreLocation;

    @Value("${spring.kafka.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${spring.kafka.ssl.key-password}")
    private String keyPassword;

    @Value("${spring.kafka.security.protocol}")
    private String securityProtocol;

    @Value("${spring.kafka.ssl.key-store-type}")
    private String keyStoreType;

    @Value("${spring.kafka.ssl.trust-store-type}")
    private String trustStoreType;

    @Bean
    public KafkaAdmin kafkaAdmin() throws IOException {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configureSsl(configs);
        return new KafkaAdmin(configs);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() throws IOException {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configureSsl(configProps);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() throws IOException {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() throws IOException {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configureSsl(configProps);
        log.info("Kafka configs: {}", configProps);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() throws IOException {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    private void configureSsl(Map<String, Object> configProps) throws IOException {
        File tempKeystore = extractResourceToTempFile(keyStoreLocation, "keystore", ".jks");
        File tempTruststore = extractResourceToTempFile(trustStoreLocation, "truststore", ".jks");


        configProps.put("security.protocol", securityProtocol);
        configProps.put("ssl.truststore.location", tempTruststore.getAbsolutePath());
        configProps.put("ssl.truststore.password", trustStorePassword);
        configProps.put("ssl.truststore.type", trustStoreType);
        configProps.put("ssl.keystore.location", tempKeystore.getAbsolutePath());
        configProps.put("ssl.keystore.password", keyStorePassword);
        configProps.put("ssl.key.password", keyPassword);
        configProps.put("ssl.keystore.type", keyStoreType);
    }

    private File extractResourceToTempFile(String resourcePath, String prefix, String suffix) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + resourcePath);
        }
        InputStream inputStream = resource.getInputStream();
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        Files.copy(inputStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }
}
