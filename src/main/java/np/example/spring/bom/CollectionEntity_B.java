package np.example.spring.bom;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class CollectionEntity_B {

	@Id
	private Long id;
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "ca_id")
	private CollectionEntity_A ca;
	
	public Long getId() {
		return id;
	}

	public CollectionEntity_A getCa() {
		return ca;
	}

	public void setCa(CollectionEntity_A ca) {
		this.ca = ca;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
