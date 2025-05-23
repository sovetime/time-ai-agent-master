package org.example.timeaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;

//自定义日志 Advisor
// 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
//
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	//提供一个唯一标识符
	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	//指定Advisor的顺序，越小越先执行
	@Override
	public int getOrder() {
		return 0;
	}

	//自定义日志Advisor，SpringAI 内置的 SimpleLoggerAdvisor 日志拦截器是debug级别的，SpringBoot是info，看不到打印信息
	private AdvisedRequest before(AdvisedRequest request) {
		log.info("AI Request: {}", request.userText());
		return request;
	}

	private void observeAfter(AdvisedResponse advisedResponse) {
		log.info("AI Response: {}", advisedResponse.response().getResult().getOutput().getText());
	}

	//非流式处理
	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		//处理请求（前置处理）
		advisedRequest = before(advisedRequest);

		//调用链中的下一个 Advisor
		AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);

		observeAfter(advisedResponse);

		return advisedResponse;
	}

	//流式处理
	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		//处理请求（前置处理）
		advisedRequest = before(advisedRequest);
		//调用链中的下一个 Advisor并处理流式响应
		Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);

		return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, this::observeAfter);
	}

}
