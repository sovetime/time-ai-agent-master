package org.example.timeaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;


//ReAct (Reasoning and Acting) 模式的代理抽象类
//实现了思考-行动的循环模式
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)//继承父类hashcode 和  equals方法
public abstract class ReActAgent extends BaseAgent {

    //思考是否需要执行
    public abstract boolean think();

    //执行
    public abstract String act();

    //思考和行动
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成 - 无需行动";
            }
            return act();
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return "步骤执行失败：" + e.getMessage();
        }
    }

}
