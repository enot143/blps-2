package coursera.repos;

import coursera.domain.Stat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatRepo extends JpaRepository<Stat, Long> {

}
