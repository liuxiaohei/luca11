package org.ld.examples;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ld.LucaApplication;
import org.ld.mapper.AdapterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {LucaApplication.class})
public class ApplicationTests {

	@Autowired
	private AdapterMapper userDao;

	@Test
	public void contextLoads() {
		System.out.println(userDao.selectByPrimaryKey(1).getId());
	}

}
