package com.example.bt05.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bt05.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByUsernameIgnoreCaseAndUserIdNot(String username, Integer userId);

	boolean existsByEmailIgnoreCaseAndUserIdNot(String email, Integer userId);

	java.util.Optional<User> findByUsernameIgnoreCase(String username);

	long countByRoleAndStatusTrue(String role);

	long countByRole(String role);

	@Query("""
			select u from User u
			where (:keyword = '' or
				lower(u.username) like lower(concat('%', :keyword, '%')) or
				lower(u.email) like lower(concat('%', :keyword, '%')) or
				lower(coalesce(u.fullName, '')) like lower(concat('%', :keyword, '%')))
			and (:role = '' or u.role = :role)
			and (:status is null or u.status = :status)
			""")
	Page<User> search(@Param("keyword") String keyword, @Param("role") String role,
			@Param("status") Boolean status, Pageable pageable);
}
