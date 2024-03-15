package com.alibou.security.auth;

import com.alibou.security.emailing.EmailDetails;
import com.alibou.security.emailing.EmailService;
import com.alibou.security.exceptionHandling.CustomException;
import com.alibou.security.exceptionHandling.CustomWarning;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthenticationController {

    private final AuthenticationService service;

    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @RequestBody RegisterRequest request
    ) {
        try {
            return ResponseEntity.ok(service.register(request));
        } catch (CustomException e) {
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }

    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<Object> resetPassword(@PathVariable String email) {
        String link;
        try {
            link = service.getEmailResetToken(email);
        } catch (CustomException e) {
            e.printStackTrace();
            CustomWarning warning = new CustomWarning(e.getStatus(), e.getMessage());
            return new ResponseEntity<>(warning, new HttpHeaders(), warning.getStatus());
        }
        var emailDetails = EmailDetails.builder().recipient(email).subject("Въстановяване на паролата")
                .msgBody("Кликнете върху следния линк, за да въстановите паролата си: "
                        + "http://localhost:3000/change-password/" + link).build();
        if (emailService.sendSimpleMail(emailDetails))
            return ResponseEntity.ok().body("Изпратен Ви е имейл с инструкции за въстановяване на паролата");
        else return ResponseEntity.badRequest().body("Грешка при изпращане на имейла, опитайте по-късно");
    }

    @GetMapping("/resetPassword/validateToken/{token}")
    public ResponseEntity<Object> validateResetToken(@PathVariable String token) {
        if (service.validateResetToken(token)) {
            return ResponseEntity.ok("Успешно верифициране на имейла, вече може да се логнете в акаунта си");
        }
        else return ResponseEntity.badRequest().body("Грешен тоукън за въстановяване на парола");
    }

    @PostMapping("/resetPassword/reset")
    public ResponseEntity<Object> setNewPassword(
            @RequestBody AuthenticationRequest request
    ) {
        if (service.resetPassword(request)) return new ResponseEntity<>(HttpStatus.OK);
        else return ResponseEntity.badRequest().body("Грешка при обновяването на паролата, моля опитайте отново");
    }

}
