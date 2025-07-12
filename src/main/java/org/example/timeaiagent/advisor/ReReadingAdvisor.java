package org.example.timeaiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

//重读 Advisor，让模型重新阅读问题来提高推理能力
public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {


	//执行请求前，改写 Prompt
	private ChatClientRequest before(ChatClientRequest chatClientRequest) {
		String userText = chatClientRequest.prompt().getUserMessage().getText();
		// 添加上下文参数
		chatClientRequest.context().put("re2_input_query", userText);
		// 修改用户提示词
		String newUserText = """
                %s
                Read the question again: %s
                """.formatted(userText, userText);
		Prompt newPrompt = chatClientRequest.prompt().augmentUserMessage(newUserText);
		return new ChatClientRequest(newPrompt, chatClientRequest.context());
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
		return chain.nextCall(this.before(chatClientRequest));
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
		return chain.nextStream(this.before(chatClientRequest));
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
}
