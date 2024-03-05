package np.example.spring.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import np.example.spring.bom.MainEntity;

@Repository
public interface MainEntityRepo extends JpaRepository<MainEntity, Long>{

}
