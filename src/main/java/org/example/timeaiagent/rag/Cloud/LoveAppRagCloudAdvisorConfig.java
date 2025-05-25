package org.example.timeaiagent.rag.Cloud;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//自定义基于阿里云知识库服务的 RAG 增强顾问
@Configuration
@Slf4j
public class LoveAppRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    @Bean
    public Advisor loveAppRagCloudAdvisor() {
        //创建 DashScopeApi 实例
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);
        //知识库名称
        final String KNOWLEDGE_INDEX = "恋爱大师";
        // 创建 DashScopeDocumentRetriever 实例，用于检索知识库中的文档，从云知识库中检索文档
        DocumentRetriever dashScopeDocumentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());

        //RetrievalAugmentationAdvisor 检索增强顾问
        //可以绑定文档检索器、查询转换器和查询增强器，更灵活地构造查询。
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(dashScopeDocumentRetriever)
                .build();
    }
}
