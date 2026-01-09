package com.example.spring_radis_token_validation;

import io.jsonwebtoken.Claims;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

@Slf4j
@RestController
@RequestMapping("/api")
public class VerifyEmailController {
    private static final Logger logger = getLogger ( VerifyEmailController.class.getName () );

    private final RedisTemplate<String , Object> radisTemplate;
    private final JwtService jwtService;
    private  final UserRepository userRepository;
    private final UserService userService;
    private final EmailService emailService;

    public VerifyEmailController(RedisTemplate<String, Object> radisTemplate, JwtService jwtService, UserRepository userRepository, UserService userService, EmailService emailService) {
        this.radisTemplate = radisTemplate;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAUserAndGetToken(@RequestBody Users user){
        Users u= userRepository.save ( user );
        // this token is for the email so i want to save like email-verify:id - UUID
        // Generate a unique token for email verification
        String token = UUID.randomUUID().toString();
        // Save to Redis with a 5-minute TTL
        // Format: "email-verify:{userId}" -> {token}
        saveData("email-verify:" + token, u.getEmail (), 5, TimeUnit.MINUTES);
        emailService.sendVerificationEmail(u.getEmail(), token);//send-email async || use the rabbitmq or Kafka
        // String verify = jwtService.generateTokenFOrEmailVerify (u.getId ());
        //return ResponseEntity.status ( HttpStatus.MULTI_STATUS ).body(token);
        return ResponseEntity.ok("Registration successful. Please check your email.");
    }
    public void saveData(String key, Object value, long timeout, TimeUnit unit) {
        // Standard signature: set(K key, V value, long timeout, TimeUnit unit)
        radisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @PostMapping("/verify-email")
    public void verifyEmail(@RequestHeader("Authorization") String token) {
        token = token.substring ( 7 );
        System.out.println ("bearber token "+ token);
        Claims claims = jwtService.verify(token);
        System.out.println ("method trigger in Verify Email in controller "+ claims.getSubject ());
        System.out.println ("method trigger in Verify Email in controller "+ claims.getExpiration ());
        System.out.println ("method trigger in Verify Email in controller "+ claims.getId ());
        System.out.println ("method trigger in Verify Email in controller "+ claims);
        System.out.println ("method trigger in Verify Email in controller "+ claims.toString ());
        if (!"EMAIL_VERIFY".equals(claims.get("purpose")))
            throw new SecurityException("Invalid purpose");
        Long userId = Long.valueOf(claims.getSubject());
         boolean isOrNot =userService.markEmailVerified(userId);
        System.out.println ("email verified status "+ isOrNot);
        //  tokenBlacklist.add(token); // optional single-use
    }


    //   @RequestParam("tokenId") String tokenId
    @PostMapping("/check-redis-availabilty-using-lattice-driver")
    public void verifyEmailUsingRedisCache() {
        RedisURI uri = RedisURI.Builder
                .redis("localhost", 6379)
                .build();
        RedisClient client = RedisClient.create(uri);
        System.out.println (client);
        StatefulRedisConnection<String, String> connection = client.connect();
        RedisCommands<String, String> commands = connection.sync();
        commands.set("foo", "bar");
        String result = commands.get("foo");
        System.out.println(result); // >>> bar
        connection.close();
        client.shutdown();
    }

    @PostMapping("/verify-email-redis-impl")
    public ResponseEntity<?> verifyEmailUsingRedisCache(@RequestParam String tokenId) {
        //this tokenId is already stored in the radis so check from there
        String email = (String) radisTemplate.opsForValue().get("email-verify:" + tokenId);//key -> email-verify:uuid
        System.out.println (email);
        //you get the email because same like this this is best way
        //you can use this also email-verify:emailId from SecurityContextHolder.getAuthentication().getUserName() or use the jwt
        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token. Send again >>>");
        }
        Users user = userRepository.findByEmail((email));
        user.setEmailVerified (true);
        userRepository.save(user);
        boolean deleteFromRadisOrNot=radisTemplate.delete("email-verify:" + tokenId);
        //  logger.notify ();
        logger.info ( "deleteFromRadiosOrNot "+ deleteFromRadisOrNot );
        return  ResponseEntity.ok().body ( "successfully verified the email address "+ user.getEmail ());
    }
}
