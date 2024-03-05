package np.example.spring.bom;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class CollectionEntity_A {

	@Id
	private Long id;

	@OneToMany(mappedBy = "ca", fetch = FetchType.LAZY)
	private List<CollectionEntity_B> listB;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "nested_id")
	private NestedEntity nested;
	
	public Long getId() {
		return id;
	}
	public NestedEntity getNested() {
		return nested;
	}
	public void setNested(NestedEntity nested) {
		this.nested = nested;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public List<CollectionEntity_B> getListB() {
		return listB;
	}
	public void setListB(List<CollectionEntity_B> listB) {
		this.listB = listB;
	}
}
