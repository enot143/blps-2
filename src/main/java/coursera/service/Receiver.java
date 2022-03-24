package coursera.service;

import com.rabbitmq.jms.client.message.RMQBytesMessage;
import coursera.domain.Test;
import coursera.domain.UserCourseKey;
import coursera.domain.UserTest;
import coursera.dto.AttemptDTO;
import coursera.exceptions.TestException;
import coursera.repos.CourseRepo;
import coursera.repos.TestRepo;
import coursera.repos.UserCourseRepo;
import coursera.repos.UserTestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;


@Service
public class Receiver implements MessageListener {
    @Autowired
    TestRepo testRepo;
    @Autowired
    UserTestRepo userTestRepo;
    @Autowired
    UserCourseRepo userCourseRepo;
    @Autowired
    CourseRepo courseRepo;

    @Override
    @Transactional
    public void onMessage(Message message) {
        byte[] data = new byte[1024];
        try {
            ((RMQBytesMessage) message).readBytes(data);
        } catch (JMSException e) {
            System.err.println("Failed to read message");
            e.printStackTrace();
            return;
        }
        try (
                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream is = new ObjectInputStream(in);
        ) {
            AttemptDTO attemptDTO = (AttemptDTO) is.readObject();
            if (testRepo == null){
                System.out.println("error");
            }
            Test t = testRepo.findById(attemptDTO.getTestId()).orElseThrow(() -> new TestException("Test is not found"));
            //проверка что дедлайн теста не истек
            Date dateNow = new java.util.Date();
            if (t.getWeek().getDeadline().before(dateNow)){
                throw new TestException("Missed deadline");
            }
            //проверка что пользователь уже выполнял тест и попытки закончились
            UserTest ut = userTestRepo.getUserTestByUserIdAndTestId(attemptDTO.getUserId(), attemptDTO.getTestId());
            if (ut == null){
                throw new TestException("User has never started this test");
            }
            if (ut.getAttempts() > 0){
                throw new TestException("User still have enough attempts");
            }
            //проверка чт лимит добавляемых попыток на курс не потрачен
            int availableAttempts = courseRepo.getCourseById(t.getWeek().getCourse().getId()).getMaxAttempts();
            UserCourseKey userCourseKey = new UserCourseKey();
            userCourseKey.setCourseId(t.getWeek().getCourse().getId());
            userCourseKey.setUserId(attemptDTO.getUserId());
            int addedAttempts = userCourseRepo.getById(userCourseKey).getAdded_attempts();
            if (addedAttempts + attemptDTO.getQuantity() <= availableAttempts){
                userTestRepo.addAttempts(t.getId(), attemptDTO.getUserId(), attemptDTO.getQuantity());
                userCourseRepo.setAttempts(t.getWeek().getCourse().getId(), attemptDTO.getUserId(), addedAttempts + attemptDTO.getQuantity());
                System.out.println("Successfully added attempts");
            }
            else throw  new TestException("Limit is less than requested quantity");
        } catch (IOException | ClassNotFoundException | IllegalArgumentException | TestException e) {
            System.out.println("Fail : " + e.getMessage());
//            e.printStackTrace();
        }
    }
}

