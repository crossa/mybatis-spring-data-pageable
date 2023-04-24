# mybatis-spring-data-pageable

## Summary

When using this plugin，‘Pageable’  can be passed as parameter。
If ‘limit’ was not exists in the SQL ，it would be add the end of the SQL statement automatically

## UseAge

1. Download the code and deploy the packege to your local repository
2. Add the dependency to pom.xml

   ```
   <dependency>
      <groupId>com.github.crossa</groupId>
      <artifactId>mybatis-spring-data-pageable</artifactId>
      <version>1.0-SNAPSHOT</version>
   </dependency>
   ```
3. Intialize the plugin

   ```
   	@Bean
   	public SqlSessionFactoryBean getSqlFactoryBean(@Autowired DataSource dataSource) throws IOException {
   		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
   		bean.setMapperLocations(
   				new PathMatchingResourcePatternResolver().getResources("classpath:mapper/ucenter/*xml*"));
   		bean.setDataSource(dataSource);
   		bean.setPlugins(new MybatisSpringDataPageableInterceptor());
   		return bean;
   	}
   ```
   4. User Pageable as paremeter
      `
      ```
      package com.demo

      import pojo.Data;
      import org.apache.ibatis.annotations.Param;
      import org.springframework.data.domain.Pageable;

      import java.util.List;
      import java.util.Map;

      public interface DataQueryMapper {
      	Long countData(@Param("arg") Map arg);
      	List<Data> findData(@Param("arg") Map arg, @Param("pageable") Pageable pageable);
      }

      ````
