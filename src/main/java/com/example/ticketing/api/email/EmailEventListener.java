package com.example.ticketing.api.email;

import com.example.ticketing.api.user.UserRepository;
import com.example.ticketing.common.exception.CommonErrorCode;
import com.example.ticketing.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EmailEventListener {
//    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    //비동기 방식 + transactionalEventListener의 디폴드 설정인 "AFTER_COMMIT"
    //비동기로 sendEmail을 호출하는 reservation()의 트랜젝션이 끝나면 반응한다.
    @Async
    @TransactionalEventListener
    public void sendEmail(EmailEvent emailEvent) throws InterruptedException {
        String userEmail = userRepository.findById(emailEvent.getUserId()).orElseThrow(
                ()-> new RestApiException(CommonErrorCode.NOT_FOUND)
        ).getEmail();
        // 이메일 전송 로직
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(userEmail);
//        message.setSubject("Ticket 정보");
//        message.setFrom(userEmail);
//        message.setText("Ticket");
//        System.out.println(message.getText());
//        mailSender.send(message);

        //이메일 전송 시간지연
        Thread.sleep(5000);//

        System.out.println("이메일 전송 완료 : 좌석ID : "+emailEvent.getTicket().getSeatId());
    }
}
