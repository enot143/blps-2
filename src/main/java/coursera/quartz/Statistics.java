package coursera.quartz;

import coursera.domain.Stat;
import coursera.domain.UserCourse;
import coursera.repos.CourseRepo;
import coursera.repos.StatRepo;
import coursera.repos.UserCourseRepo;
import coursera.repos.UserRepo;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;


@DisallowConcurrentExecution
@Component
public class Statistics implements Job {
    @Autowired
    UserRepo userRepo;
    @Autowired
    CourseRepo courseRepo;
    @Autowired
    UserCourseRepo userCourseRepo;
    @Autowired
    StatRepo statRepo;

    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Date dateNow = new java.util.Date();
        long users = userRepo.count();
        long courses = courseRepo.count();
        long money = 0;

        ArrayList<UserCourse>  userCourses = (ArrayList<UserCourse>) userCourseRepo.findAll();
        for (UserCourse uc : userCourses){
            money += courseRepo.findCourseById(uc.getCourse().getId()).getCost();
        }

        long certificates = userCourseRepo.getCertificateCount();

        Stat stat = new Stat();
        stat.setCertificates(certificates);
        stat.setCourses(courses);
        stat.setDate(dateNow);
        stat.setMoney(money);
        stat.setUsers(users);
        statRepo.save(stat);

        System.out.println("statistic");
        System.out.println("date: " + dateNow);
        System.out.println("users: " + users);
        System.out.println("money: " + money);
        System.out.println("courses: " + courses);
        System.out.println("certificates: " + certificates);
    }
}