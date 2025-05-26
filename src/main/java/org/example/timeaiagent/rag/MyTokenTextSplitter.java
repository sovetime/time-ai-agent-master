package org.example.timeaiagent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;


//自定义基于 Token 的切词器
@Component
public class MyTokenTextSplitter {

    // 默认切词器
    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    //  自定义切词器
    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(
                200,   //  每个文本块的目标大小
                100,            //  每个文本块的最小大小
                10,             //  要被包含的块的最小长度
                5000,           //  从文本中生成的最大块数
                true);          //  是否在块中保留分隔符
        return splitter.apply(documents);
    }
}
