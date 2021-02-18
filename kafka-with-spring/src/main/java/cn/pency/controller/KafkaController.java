package cn.pency.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *@author King老师   享学课堂 https://enjoy.ke.qq.com
 *往期视频咨询芊芊老师  QQ：2130753077  VIP课程咨询 依娜老师QQ：2133576719
 *类说明：
 */
@Controller
@RequestMapping("/kafka")
public class KafkaController {

	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;

	/**
	 * @param message
	 * @return String
	 */
	@ResponseBody
	@RequestMapping("spring")
	public String queueSender(@RequestParam("message")String message){
		String opt="";
		try {
			kafkaTemplate.send("kafka-spring-topic",message);
			opt = "suc";
		} catch (Exception e) {
			opt = e.getCause().toString();
		}
		return opt;
	}

	/**
	 * @param message
	 * @return String
	 */
	@ResponseBody
	@RequestMapping("springb")
	public String topicSender(@RequestParam("message")String message){
		String opt = "";
		try {
			kafkaTemplate.send("kafka-spring-topic-b",message);
			opt = "suc";
		} catch (Exception e) {
			opt = e.getCause().toString();
		}
		return opt;
	}

}
