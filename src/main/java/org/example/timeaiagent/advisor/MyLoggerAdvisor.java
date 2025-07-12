package org.example.timeaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;


//自定义日志 Advisor，默认的 SimpleLoggerAdvisor 日志拦截器是debug级别的，SpringBoot是info，看不到打印信息
// 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

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
	private ChatClientRequest before(ChatClientRequest request) {
		log.info("AI Request: {}", request.prompt());
		return request;
	}

	private void observeAfter(ChatClientResponse chatClientResponse) {
		log.info("AI Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
	}

	//非流式处理
	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
		chatClientRequest = before(chatClientRequest);
		ChatClientResponse chatClientResponse = chain.nextCall(chatClientRequest);
		observeAfter(chatClientResponse);
		return chatClientResponse;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
		chatClientRequest = before(chatClientRequest);
		Flux<ChatClientResponse> chatClientResponseFlux = chain.nextStream(chatClientRequest);
		return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponseFlux, this::observeAfter);
	}

}
