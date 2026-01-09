package com.example.spring_radis_token_validation;

import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class UserService{
    private  final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public boolean markEmailVerified(Long userId){
       Optional<Users> optionalUser= Optional.ofNullable ( userRepository.findById ( userId ).orElseThrow (
               () -> new RuntimeException ( "userId not found" + userId )
       ) );
        Users user = optionalUser.get ();
        user.setEmailVerified ( true );
        userRepository.save ( user );
        return  true;
    }
}
