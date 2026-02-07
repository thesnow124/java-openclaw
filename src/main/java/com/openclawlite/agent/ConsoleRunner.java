package com.openclawlite.agent;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author gaoshuanglong
 */
@Component
// 启动阻塞式控制台循环，将输入转交给代理。
public class ConsoleRunner implements CommandLineRunner {
    private final AgentService agentService;

    // 注入代理服务以处理用户回合。
    public ConsoleRunner(AgentService agentService) {
        this.agentService = agentService;
    }

    @Override
    // 逐行读取控制台输入，直到用户退出。
    public void run(String... args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("OpenClaw Lite (console mode). Type /exit to quit.");
        // 控制台交互的主循环。
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            String input = line.trim();
            if (input.isEmpty()) {
                continue;
            }
            if ("/exit".equalsIgnoreCase(input) || "/quit".equalsIgnoreCase(input)) {
                break;
            }
            try {
                String reply = agentService.runTurn(input);
                System.out.println(reply);
            } catch (Exception e) {
                // 调用失败时提示配置检查信息。
                System.out.println("调用模型失败：" + e.getMessage());
        System.out.println("请检查 ZHIPUAI_API_KEY / ZHIPUAI_BASE_URL / ZHIPUAI_MODEL 是否正确。");
            }
        }
    }
}
