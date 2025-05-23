package org.example.timeaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        String command = "docker ps";
        String result = terminalOperationTool.executeTerminalCommand(command);
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
}
