package com.alibou.security.schedulingtasks;

import com.alibou.security.lessons.LessonTermin;
import com.alibou.security.lessons.LessonTerminRepo;
import com.alibou.security.user.StudentRepository;
import com.alibou.security.user.TeacherRepository;
import com.alibou.security.user.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//TODO schedule this task upon date creation and add a course task (take code from generateMeeting method

public class MeetingGenerator implements Runnable{
    private final LessonTermin termin;
    private final LessonTerminRepo lessonTerminRepo;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;

    public MeetingGenerator(LessonTermin termin, LessonTerminRepo lessonTerminRepo, UserRepository userRepository,
                            SimpMessagingTemplate messagingTemplate) {
        this.termin = termin;
        this.lessonTerminRepo = lessonTerminRepo;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void run() {
        if (termin.getLinkToClassroom() != null) return;
        String meetingId = "meet.jit.si/" + UUID.randomUUID().toString();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        List<String> studentToken = new ArrayList<>();
        termin.setLinkToClassroom(meetingId);
        lessonTerminRepo.save(termin);
        studentToken.add(userRepository.findLastToken(termin.getStudent().getId()).get(0).getToken());
        NotificationSenderTask notificationTask = new NotificationSenderTask(messagingTemplate,
                "Вашият урок започва след 5 минути, учителят вече създаде мийтинга. \n" +
                        "Кликнете линка, за да се присъедините:" + meetingId, studentToken, termin.getLesson().getTitle());
        service.schedule(notificationTask, termin.getDateTime().getTime() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS);
    }
}
