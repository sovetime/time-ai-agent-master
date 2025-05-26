package org.example.timeaiagent.agent;

import org.example.timeaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;


@Component
public class TimeManus extends ToolCallAgent {

    public TimeManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        //父类初始化
        super(allTools);
        this.setName("TimeManus");

        //系统提示词
        //你是TimeManus，一个全能的人工智能助手，旨在解决用户提出的任何任务。
        //您可以使用各种工具来高效地完成复杂的请求
        String SYSTEM_PROMPT = """
                You are TimeManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);

        //下一步提示词
        //根据用户需求，主动选择最合适的工具或工具组合。
        //对于复杂的任务，您可以分解问题并逐步使用不同的工具来解决它。
        //使用每个工具后，清楚地解释执行结果并建议下一步。
        //如果你想在任何时候停止交互，请使用“terminate”工具/函数调用
        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);

        this.setMaxSteps(20);//最大步骤数

        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
