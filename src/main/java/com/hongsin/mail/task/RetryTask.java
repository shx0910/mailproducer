package com.hongsin.mail.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hongsin.mail.entity.MailSend;
import com.hongsin.mail.service.MailSendService;

@Component
public class RetryTask {

	private static Logger logger = LoggerFactory.getLogger(RetryTask.class);
	@Autowired
	private MailSendService mailSendService;
	
	@Scheduled(initialDelay = 5000, fixedDelay = 10000)
	public void retry() {
		logger.info("--------开始重新执行发送邮件任务---------");
		List<MailSend> lists = mailSendService.queryDraftList();
		// 重新发送邮件
		for (MailSend mailSend : lists) {
			mailSendService.sendRedis(mailSend);
		}
	}
	
}
