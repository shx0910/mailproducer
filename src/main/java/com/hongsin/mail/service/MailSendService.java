package com.hongsin.mail.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.hongsin.mail.config.database.ReadOnlyConnection;
import com.hongsin.mail.entity.MailSend;
import com.hongsin.mail.enumeration.MailStatus;
import com.hongsin.mail.enumeration.RedisPriorityQueue;
import com.hongsin.mail.mapper.MailSend1Mapper;
import com.hongsin.mail.mapper.MailSend2Mapper;
import com.hongsin.mail.utils.FastJsonConvertUtil;

@Service
public class MailSendService {

	private static Logger logger = LoggerFactory.getLogger(MailSendService.class);
	
	@Autowired
	private MailSend1Mapper mailSend1Mapper;
	@Autowired
	private MailSend2Mapper mailSend2Mapper;
	@Autowired
	private RedisTemplate<String, String>  redisTemplate;
	
	public void insert(MailSend mailSend) throws Exception{
		int hashCode = mailSend.getSendId().hashCode();
		if(hashCode % 2 == 1) {
			mailSend1Mapper.insert(mailSend);
		}else {
			mailSend2Mapper.insert(mailSend);
		}
		logger.info("-----");
	}

	public void sendRedis(MailSend mailSend) {
		ListOperations<String, String> opsForList = redisTemplate.opsForList();
		Long priority = mailSend.getSendPriority();
		Long ret = 0L;
		Long size = 0L;
		if (priority < 4L) {//1 2 3 则进入延迟队列
			ret = opsForList.leftPush(RedisPriorityQueue.DEFER_QUEUE.getCode(), FastJsonConvertUtil.convertObjectToJSON(mailSend));
			size = opsForList.size(RedisPriorityQueue.DEFER_QUEUE.getCode());
		} else if (priority < 7L) {//4 5 6 则进入普通队列
			ret = opsForList.leftPush(RedisPriorityQueue.NORMAL_QUEUE.getCode(), FastJsonConvertUtil.convertObjectToJSON(mailSend));
			size = opsForList.size(RedisPriorityQueue.NORMAL_QUEUE.getCode());
		} else {//7 8 9 则进入紧急队列
			ret = opsForList.leftPush(RedisPriorityQueue.FAST_QUEUE.getCode(), FastJsonConvertUtil.convertObjectToJSON(mailSend));
			size = opsForList.size(RedisPriorityQueue.FAST_QUEUE.getCode());
		}
		mailSend.setSendCount(mailSend.getSendCount()+1);
		if(ret==size) {
			mailSend.setSendStatus(MailStatus.SEND_IN.getCode());
			logger.info("-- 进入队列成功，id : {}----",mailSend.getSendId());
		}else {
			logger.info("-- 进入队列失败，id : {}----",mailSend.getSendId());
		}
		if(mailSend.getSendId().hashCode() %2 ==0) {
			mailSend2Mapper.updateByPrimaryKeySelective(mailSend);
		}else {
			mailSend1Mapper.updateByPrimaryKeySelective(mailSend);
		}
	}


	/**
	 * 连接只读库，读取暂存的消息内容
	 * @Title: queryDraftList   
	 * @Description: TODO(这里用一句话描述这个方法的作用)   
	 * @param: @return      
	 * @return: List<MailSend>      
	 * @throws
	 */
	@ReadOnlyConnection
	public List<MailSend> queryDraftList() {
		List<MailSend> list = new ArrayList<MailSend>();
		list.addAll(mailSend1Mapper.queryDraftList());
		list.addAll(mailSend2Mapper.queryDraftList());
		return null;
	}
}
