package cn.com.dmg.mongodblearn.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Slf4j
public class MongoXmlEntity{
    @Field
    private String id;
    @Field
    private String ah;
    @Field
    private String ahMd5;
    @Field
    private String xmlContent;
}
