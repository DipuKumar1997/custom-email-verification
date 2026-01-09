# Email Verification Service with Redis 

## Project Overview
This Spring Boot application provides a robust email verification system using Redis for token storage and  asynchronous email sending.

## Key Features
- User registration with email verification (currently i disbale the formLogin().disable() and httpBasic().disable())
- Token-based email verification
- Redis caching for temporary token storage(if we donot want to hit the db for email verification)

## Configuration Components

### Redis Configuration
- Uses Redis for temporary token storage
- TTL is set as 5 Timeunit.MINUTES
- Supports key-value storage for email verification tokens

## Endpoints

### User Registration
- **POST** `/api/create`
  - Creates a new user
  - Generates a unique verification token
  - Sends verification email
  - Stores token in Redis

### Email Verification
- **POST** `/api/verify-email`
  - Verifies email using JWT token(just check the purpose claims is there or not and purpose claim is equls to EMAIL_VERIFY)
  - Marks user as email verified
  - 

- **POST** `/api/verify-email-redis-impl`
  - Alternative email verification using Redis token
    
## Kafka Configuration (in future i will add the kafka for handling the email as asynchronously)
```java
@Configuration
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP\_SERVERS\_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY\_SERIALIZER\_CLASS\_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE\_SERIALIZER\_CLASS\_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
