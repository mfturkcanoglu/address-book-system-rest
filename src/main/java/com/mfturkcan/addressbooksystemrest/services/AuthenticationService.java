package com.mfturkcan.addressbooksystemrest.services;

import com.mfturkcan.addressbooksystemrest.dtos.AuthenticationRequest;
import com.mfturkcan.addressbooksystemrest.dtos.AuthenticationResponse;
import com.mfturkcan.addressbooksystemrest.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final BookUserDetailService bookUserDetailService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(BookUserDetailService bookUserDetailService,
                                 JwtUtils jwtUtils,
                                 AuthenticationManager authenticationManager) {
        this.bookUserDetailService = bookUserDetailService;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse authenticateUser(AuthenticationRequest authenticationRequest){

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();

        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername()
                            , authenticationRequest.getPassword()));
        }
        catch(BadCredentialsException e){
            authenticationResponse.setMessage("Incorrect username or password");
            authenticationResponse.setStatus(HttpStatus.BAD_REQUEST);
            return authenticationResponse;
        }
        catch (Exception e){
            authenticationResponse.setMessage(e.getMessage());
            authenticationResponse.setStatus(HttpStatus.BAD_REQUEST);
            System.out.println(e.getMessage());
            e.printStackTrace();
            return authenticationResponse;
        }

        final UserDetails userDetail = bookUserDetailService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtils.generateToken(userDetail);

        authenticationResponse.setJwt(jwt);
        authenticationResponse.setStatus(HttpStatus.OK);
        return authenticationResponse;
    }

    public AuthenticationResponse getUser(String jwt){
        var response = new AuthenticationResponse();

        try{
            var username = jwtUtils.extractUsername(jwt);
            response.setMessage(username);
            response.setJwt(jwt);
            response.setStatus(HttpStatus.ACCEPTED);
        }
        catch(ExpiredJwtException e){
            response.setMessage("Expired token");
            response.setStatus(HttpStatus.BAD_REQUEST);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e){
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}