package com.hongsin.mail.service.api;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hongsin.mail.constant.Const;
import com.hongsin.mail.entity.MailSend;
import com.hongsin.mail.enumeration.MailStatus;
import com.hongsin.mail.service.MailSendService;
import com.hongsin.mail.utils.KeyUtil;

@RestController
public class ProducerController {

	private static Logger logger = LoggerFactory.getLogger(ProducerController.class);
	
	@Autowired
	private MailSendService mailSendService;
	
	@RequestMapping(value = "/send",produces = {"application/json;charset=UTF-8"})
	public void send(@RequestBody(required = false) MailSend mailSend) throws Exception{
		try {
			
			mailSend.setSendId(KeyUtil.generatorUUID());
			mailSend.setSendCount(0L);
			mailSend.setSendStatus(MailStatus.DRAFT.getCode());
			mailSend.setVersion(0L);
			mailSend.setUpdateBy(Const.SYS_RUNTIME);
			mailSend.setUpdateTime(new Date());
			mailSendService.insert(mailSend);
			//把数据扔到redis,并更新数据库状态
			mailSendService.sendRedis(mailSend);
			
		}catch(Exception e) {
			logger.error("异常信息：{}",e);
			throw new RuntimeException();
		}
	}
}
