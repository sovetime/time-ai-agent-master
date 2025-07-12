package org.example.timeaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.example.timeaiagent.agent.model.AgentState;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;


//处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {
    // 可用的工具
    private final ToolCallback[] availableTools;
    // 保存工具调用信息的响应结果（要调用那些工具）
    private ChatResponse toolCallChatResponse;
    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;
    // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        // 父类初始化
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }


    //思考是否需要执行工具调用
    @Override
    public boolean think() {
        // 拼接下一步提示词
        if (StrUtil.isNotBlank(this.getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(this.getNextStepPrompt());
            //Memory 记忆拼接
            this.getMessageList().add(userMessage);
        }

        //消息列表获取
        List<Message> messageList = this.getMessageList();
        // 构造提示词
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            //调用大模型
            ChatResponse chatResponse = this.getChatClient()
                    .prompt(prompt)//提示词
                    .system(this.getSystemPrompt())//系统提示
                    .tools(availableTools)//可以调用的工具
                    .call()
                    .chatResponse();

            // 记录响应，用于等下 Act
            this.toolCallChatResponse = chatResponse;
            // 获取大模型输出
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // 输出提示信息
            String result = assistantMessage.getText();

            log.info(this.getName() + "的思考：" + result);
            log.info(this.getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            // 拼接工具信息
            String toolCallInfo = toolCallList.stream().map(
                    toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));//拼接\n
            log.info(toolCallInfo);

            // 如果不需要调用工具，返回 false
            if (toolCallList.isEmpty()) {
                // 只有不调用工具时，才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题：" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            return false;
        }
    }


    // 执行工具调用
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }

        // 构造提示词
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        // 执行工具调用并获取结果
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        this.setMessageList(toolExecutionResult.conversationHistory());
        // 获取助手消息
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled) {
            // 任务结束，更改状态
            setState(AgentState.FINISHED);
        }

        //返回结果
        String results = toolResponseMessage.getResponses().stream().map(
                response -> "工具 " + response.name() + " 返回的结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
}
