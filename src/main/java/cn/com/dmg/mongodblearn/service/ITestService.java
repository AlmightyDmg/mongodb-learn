package cn.com.dmg.mongodblearn.service;

import cn.com.dmg.mongodblearn.entity.MongoXmlEntity;
import org.dom4j.Document;

public interface ITestService {

    Document getXml(String xmlPath);

    String getXmlStr();

    /**
     * @Description 保存卷宗字符串到MongoDB
     * @author zhum
     * @date 2021/7/9 14:14
     * @param ah
     * @param xmlStr
     * @Return void
     */
    void saveXmlStr2Mongo(String ah, String xmlStr);

    /**
     * @Description 更新xml内容
     * @author zhum
     * @date 2021/7/9 14:28
     * @param ah
     * @param xmlStr
     * @Return void
     */
    void updateXmlStr(String ah,String xmlStr);

    /**
     * @Description 根据案号查询
     * @author zhum
     * @date 2021/7/9 14:35
     * @param ah
     * @Return
     */
    String getXmlStrFromMongo(String ah);

    String getXmlStrFromMongo(String ah, Integer threadNum, Integer caseNums);

    /**
     * @Description
     * @author zhum
     * @date 2021/7/9 15:38
     * @param ah
     * @Return cn.com.dmg.mongodblearn.entity.MongoXmlEntity
     */
    MongoXmlEntity getMongoXml(String ah);


    /**
     * @Description 模拟使用的时候，对一份卷宗的更新操作，包含：查询，修改，更新到数据库
     * @author zhum
     * @date 2021/7/9 14:44
     * @param xmlStr
     * @param threadNum
     * @param caseNums
     * @Return void
     */
    void doUpdateXmlStr(String ah, Integer threadNum, Integer caseNums);
}
