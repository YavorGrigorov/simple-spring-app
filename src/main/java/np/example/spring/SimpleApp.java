package np.example.spring;

import java.util.Date;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@SpringBootApplication
public class SimpleApp {

	StdDeserializer<Date> s;
	
	public static void main(String... args) {
		SpringApplication.run(SimpleApp.class, args);
	}
}
