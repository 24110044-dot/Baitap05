package com.example.bt05;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.PageRequest;

import com.example.bt05.repository.CategoryRepository;
import com.example.bt05.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfSystemProperty(named = "sqlserver.integration", matches = "true")
class SqlServerJdbcIntegrationTest {

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private CategoryRepository categories;

	@Autowired
	private UserRepository users;

	@Test
	void connectsUsingConfiguredDatasourceAndQueriesBothExistingTables() {
		Integer selectOne = jdbc.queryForObject("SELECT 1", Integer.class);
		Long categoryRows = jdbc.queryForObject("SELECT COUNT_BIG(*) FROM [Categories]", Long.class);
		Long userRows = jdbc.queryForObject("SELECT COUNT_BIG(*) FROM [Users]", Long.class);

		assertThat(selectOne).isEqualTo(1);
		assertThat(categoryRows).isNotNull().isGreaterThan(0L);
		assertThat(userRows).isNotNull().isGreaterThanOrEqualTo(0L);
		assertThat(categories.count()).isEqualTo(categoryRows);
		assertThat(users.count()).isEqualTo(userRows);
		assertThat(categories.findAll(PageRequest.of(0, 5))).isNotNull();
		assertThat(users.findAll(PageRequest.of(0, 5))).isNotNull();
	}
}
