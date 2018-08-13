package com.hongsin.mail;

import java.sql.Connection;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.pagehelper.PageHelper;
import com.hongsin.mail.entity.MstDict;
import com.hongsin.mail.mapper.MstDictMapper;
import com.hongsin.mail.service.MstDictService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTests {

	@Resource(name = "masterDataSource")
	private DataSource masterDataSource;

	@Resource(name = "slaveDataSource")
	private DataSource slaveDataSource;

	@Test
	public void contextLoads() throws Exception {
		Connection c1 = masterDataSource.getConnection("root", "root");
		System.out.println("c1：" + c1.getMetaData().getURL());
		Connection c2 = slaveDataSource.getConnection("root", "root");
		System.out.println("c2：" + c2.getMetaData().getURL());
	}

	@Autowired
	private MstDictMapper mstDictMapper;

	@Test
	public void test1() throws Exception {
		PageHelper.startPage(1, 2);
		List<MstDict> list = mstDictMapper.selectAll();
		for (MstDict md : list) {
			System.err.println(md.getName());
		}
	}

	@Autowired
	private MstDictService mstDictService;

	@Test
	public void test2() throws Exception {
		List<MstDict> list = mstDictService.findByStatus("1");
		if(list!=null && list.size()>0) {
			for (MstDict md : list) {
				System.err.println(md.getName());
			}
		}
	}
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Test
	public void test3() throws Exception{
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
		opsForValue.set("name","shaohongxing");
		System.out.println("name:"+ opsForValue.get("name"));
	}
}
