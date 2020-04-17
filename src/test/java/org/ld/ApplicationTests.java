package org.ld;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ld.mapper.AdapterMapper;
import org.ld.utils.JsonUtil;
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
		System.out.println(JsonUtil.obj2Json(userDao.selectByPrimaryKey(1)));
	}

}
