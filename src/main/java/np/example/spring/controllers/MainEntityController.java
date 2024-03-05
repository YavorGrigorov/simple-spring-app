package np.example.spring.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import np.example.spring.bom.MainEntity;
import np.example.spring.repos.MainEntityRepo;

@RestController
@RequestMapping(path = "/main-entity")
public class MainEntityController {

	@Autowired
	private MainEntityRepo repo;
	
	
	@GetMapping("/get-main-entities")
	public Object getMainEntities() {
		return repo.findAll();
	}
	
	@PostMapping("/add")
	public Object add(@RequestBody MainEntity entity) {
		return entity.getDate().toString();
	}
}
