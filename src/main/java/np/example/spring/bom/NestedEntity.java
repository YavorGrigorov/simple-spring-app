package np.example.spring.bom;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class NestedEntity {

	@Id
	private Long id;
	@OneToMany(mappedBy = "nested", fetch = FetchType.LAZY)
	private List<CollectionEntity_A> listA;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public List<CollectionEntity_A> getListA() {
		return listA;
	}
	public void setListA(List<CollectionEntity_A> listA) {
		this.listA = listA;
	}
}
