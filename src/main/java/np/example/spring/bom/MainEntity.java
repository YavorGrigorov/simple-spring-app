package np.example.spring.bom;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class MainEntity {

	@Id
	private Long id;
	@OneToOne
	@JoinColumn(name = "nested_id")
	private NestedEntity nested;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm'T'dd-MM-yyyy'Z'", with = {}, without = {})
	private Date date;
	
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public NestedEntity getNested() {
		return nested;
	}
	public void setNested(NestedEntity nested) {
		this.nested = nested;
	}
	
}
