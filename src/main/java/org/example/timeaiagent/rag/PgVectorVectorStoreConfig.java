package org.example.timeaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

// 配置向量数据库
@Configuration
public class PgVectorVectorStoreConfig {

    //文档加载器
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        // 创建向量数据库
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    //向量维度
                .distanceType(COSINE_DISTANCE)       //向量之间的相似性度量方式为余弦距离
                .indexType(HNSW)                     //索引类型
                .initializeSchema(true)              //是否在初始化时自动创建表结构
                .schemaName("public")                //数据库中的模式名称为 public
                .vectorTableName("vector_store")     //指定存储向量数据的表名为 vector_store
                .maxDocumentBatchSize(10000)         //设置每次批量插入的最大文档数量为 10000
                .build();

        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        vectorStore.add(documents);
        return vectorStore;
    }
}
