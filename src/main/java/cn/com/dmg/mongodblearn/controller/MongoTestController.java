package cn.com.dmg.mongodblearn.controller;

import cn.com.dmg.mongodblearn.dao.MongoTestDao;
import cn.com.dmg.mongodblearn.entity.MongoTest;
import cn.com.dmg.mongodblearn.entity.MongoXmlEntity;
import cn.com.dmg.mongodblearn.service.ITestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MongoTestController {

    @Autowired
    private MongoTestDao mtdao;

    @Autowired
    private ITestService testService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping(value="/test1")
    public void saveTest() throws Exception {
        MongoTest mgtest=new MongoTest();
        mgtest.setId(11);
        mgtest.setAge(33);
        mgtest.setName("ceshi");
        mtdao.saveTest(mgtest);
    }

    @GetMapping(value="/test2")
    public MongoTest findTestByName(){
        MongoTest mgtest= mtdao.findTestByName("ceshi");
        System.out.println("mgtest is "+mgtest);
        return mgtest;
    }

    @GetMapping(value="/test3")
    public void updateTest(){
        MongoTest mgtest=new MongoTest();
        mgtest.setId(11);
        mgtest.setAge(44);
        mgtest.setName("ceshi2");
        mtdao.updateTest(mgtest);
    }

    @GetMapping(value="/test4")
    public void deleteTestById(){
        mtdao.deleteTestById(11);
    }


    @GetMapping(value="/saveXml/{num}")
    public void saveXml(@PathVariable(name = "num") Integer num){
        String ah = "(2020)鲁1302民初";
        for (int i = 0; i < num; i++) {
            String tempAh = ah + i + "号";
            String xmlStr = this.testService.getXmlStr();
            this.testService.saveXmlStr2Mongo(tempAh,xmlStr);
        }
    }

    @GetMapping(value="/getXmlStr")
    public void getXmlStr(){
        String ah = "(2020)鲁1302民初18919号";
        String xmlStr = this.testService.getXmlStrFromMongo(ah);
        log.info(xmlStr);
    }


    @GetMapping(value="/doUpdateXmlStr/{threadNum}/{caseNums}")
    public void doUpdateXmlStr(@PathVariable(name = "threadNum")Integer threadNum,
                          @PathVariable(name = "caseNums")Integer caseNums){

        log.info("当前在模拟对{}个不同卷宗的操作，每个卷宗的操作者有{}个人",caseNums,threadNum);

        String ah = "(2020)鲁1302民初";
        for (int i = 0; i < caseNums; i++) {
            String tempAh = ah + i + "号";
            for (int j = 0; j < threadNum; j++) {
                new Thread(()->{
                    this.testService.doUpdateXmlStr(tempAh,threadNum,caseNums);
                }).start();
            }
        }

    }



    @GetMapping(value="/readOnlyXmlStr/{threadNum}/{caseNums}")
    public void readOnlyXmlStr(@PathVariable(name = "threadNum")Integer threadNum,
                               @PathVariable(name = "caseNums")Integer caseNums){

        log.info("模拟{}个人同时打开卷宗",caseNums);

        String ah = "(2020)鲁1302民初";
        for (int i = 0; i < caseNums; i++) {
            String tempAh = ah + i + "号";
            for (int j = 0; j < threadNum; j++) {
                new Thread(()->{
                    String xmlStr = this.testService.getXmlStrFromMongo(tempAh,threadNum,caseNums);
                }).start();
            }

        }
    }

}
