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
                200,   //  最大长度
                100,            //  最小长度
                10,             //  拆分阈值
                5000,           //  最大数量
                true);          //  是否忽略标点符号
        return splitter.apply(documents);
    }
}
