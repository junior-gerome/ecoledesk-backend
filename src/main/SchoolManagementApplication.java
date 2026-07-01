// package com.school;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.domain.EntityScan;
// import org.springframework.context.annotation.ComponentScan;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// // Configuration principale de l'application Spring Boot
// // Définition des packages à scanner pour les composants, entités JPA et repositories
// @SpringBootApplication(scanBasePackages = "com.school.management")
// @EntityScan(basePackages = "com.school.management.model")
// @ComponentScan({"com.school.management", "com.school.service"}) // Scanne aussi les services dans com.school.service
// @EnableJpaRepositories(basePackages = "com.school.management.repository")
// public class SchoolManagementApplication {
//     public static void main(String[] args) {
//         SpringApplication.run(SchoolManagementApplication.class, args);
//     }
// }

// // package school.primary;

// // import org.springframework.boot.SpringApplication;
// // import org.springframework.boot.autoconfigure.SpringBootApplication;

// // @SpringBootApplication
// // public class PrimaryApplication {

// // 	public static void main(String[] args) {
// // 		SpringApplication.run(PrimaryApplication.class, args);
// // 	}

// // }


package com.school;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
// @EnableJpaRepositories(basePackages = "com.school.management.repository")
@EnableWebMvc // Ajoutez cette annotation si elle n'y est pas

public class SchoolManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchoolManagementApplication.class, args);
	}

}
