package cn.com.dmg.mongodblearn.service.impl;

import cn.com.dmg.mongodblearn.entity.MongoXmlEntity;
import cn.com.dmg.mongodblearn.service.ITestService;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class TestServiceImpl implements ITestService {

    @Resource
    private MongoTemplate mongoTemplate;

    private static List<Long> myList = new CopyOnWriteArrayList<>();
    private static List<Long> myReadOnlyList = new CopyOnWriteArrayList<>();

    @Override
    public Document getXml(String xmlPath) {
        File f = new File(xmlPath);
        if(!f.exists()){
            return null;
        }
        SAXReader reader = new SAXReader();
        Document doc=null;
        try {
            doc = reader.read(f);
        } catch (DocumentException e) {
        }
        return doc;
    }

    @Override
    public String getXmlStr() {
        String xmlPath = "C:/Users/zhum/Desktop/jz_max.xml";
        File file = new File(xmlPath);
        org.w3c.dom.Document document = XmlUtil.readXML(file);
        String xmlStr = XmlUtil.toStr(document);
        return xmlStr;
    }

    @Override
    public void saveXmlStr2Mongo(String ah, String xmlStr) {
        MD5 md5 = MD5.create();
        String ahMd5 = md5.digestHex(ah);

        MongoXmlEntity mongoXml = new MongoXmlEntity();
        mongoXml.setAh(ah);
        mongoXml.setAhMd5(ahMd5);
        mongoXml.setXmlContent(xmlStr);

        this.mongoTemplate.save(mongoXml);
    }

    @Override
    public void updateXmlStr(String ah, String xmlStr) {
        MD5 md5 = MD5.create();
        String ahMd5 = md5.digestHex(ah);
        Query query=new Query(Criteria.where("ahMd5").is(ahMd5));
        Update update= new Update().set("xmlContent", xmlStr);
        //更新查询返回结果集的第一条
        mongoTemplate.updateFirst(query,update, MongoXmlEntity.class);
    }

    @Override
    public String getXmlStrFromMongo(String ah) {

        Query query=new Query(Criteria.where("ah").is(ah));
        MongoXmlEntity mongoXml =  mongoTemplate.findOne(query , MongoXmlEntity.class);
        return mongoXml.getXmlContent();
    }

    @Override
    public String getXmlStrFromMongo(String ah, Integer threadNum,Integer caseNums) {
        long start = System.currentTimeMillis();
        String xmlStrFromMongo = getXmlStrFromMongo(ah);
        long end = System.currentTimeMillis();
        myReadOnlyList.add(end-start);
        //判断myList的长度是否达到期望的最大值 达到则进行统计
        if(myReadOnlyList.size() == (threadNum * caseNums)){
            this.caculate(myReadOnlyList);
        }
        return xmlStrFromMongo;
    }

    @Override
    public MongoXmlEntity getMongoXml(String ah) {
        Query query=new Query(Criteria.where("ah").is(ah));
        MongoXmlEntity mongoXml =  mongoTemplate.findOne(query , MongoXmlEntity.class);
        return mongoXml;
    }

    @Override
    public void doUpdateXmlStr(String ah, Integer threadNum, Integer caseNums) {
        synchronized (ah){
            long start = System.currentTimeMillis();
            //查询
            String xmlStrFromMongo = this.getXmlStrFromMongo(ah);
            //修改
            xmlStrFromMongo = Thread.currentThread().getName() + xmlStrFromMongo;
            //更新
            this.updateXmlStr(ah,xmlStrFromMongo);
            long end = System.currentTimeMillis();
            log.info("模拟对卷宗的查询，修改和保存到数据库的操作，共用时{}毫秒",end-start);
            myList.add(end-start);
            //判断myList的长度是否达到期望的最大值 达到则进行统计
            if(myList.size() == (threadNum*caseNums)){
                this.caculate(myList);
            }

        }

    }

    private void caculate(List<Long> myList) {
        //先对集合排序  小到大
        myList.sort(new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                if(o1>o2){
                    return 1;
                }else if(o1 < o2){
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        LongSummaryStatistics longSummaryStatistics = myList.stream().mapToLong((s) -> s).summaryStatistics();


        //最大值
        long max = longSummaryStatistics.getMax();
        //最小值
        long min = longSummaryStatistics.getMin();
        //平均值
        double average = longSummaryStatistics.getAverage();
        //总个数
        //long count = longSummaryStatistics.getCount();
        //中位数
        Long middleNum;
        int size = myList.size();
        if (size % 2 == 0) {
            middleNum = ((myList.get(size / 2 - 1) + myList.get(size / 2))) / 2;
        } else {
            middleNum = myList.get(size / 2);
        }
        //众数
        List<Long> modeIris = getModeIris(myList);
        for (Long iris : modeIris) {
            log.info("众数为：" + iris);
        }

        log.info("执行之后，用时的最大值为{}，最小值为{}，平均值为{}，中位数为{}",max,min,average,middleNum);

        //算完之后清空
        myList.clear();

    }


    //求每一个属性的众数
    private List<Long> getModeIris(List<Long> array) {
        Map<Long, Integer> map = new HashMap<>();
        Set<Map.Entry<Long, Integer>> set = map.entrySet();
        List<Integer> list = new ArrayList<>();
        List<Long> listMode = new ArrayList<>();
        //统计元素出现的次数，存入Map集合
        for (Long item : array) {
            if (!map.containsKey(item)) {
                map.put(item, 1);
            } else {
                map.put(item, map.get(item) + 1);
            }
        }
        //将出现的次数存入List集合
        for (Map.Entry<Long, Integer> entry : set) {
            list.add(entry.getValue());
        }
        //得到最大值
        int max = this.getMax(list);
        log.info("众数的人数为：{}",max);
        //根据最大值获取众数
        for (Map.Entry<Long, Integer> entry : set) {
            if (entry.getValue() == max) {
                listMode.add(entry.getKey());
            }
        }
        for (Long item:listMode) {
            System.out.println(item);
        }
        return listMode;
    }

    //求最大值
    private int getMax(List<Integer> list) {
        int max = 0;
        for (int i = 0; i < list.size(); i++) {
            if (max < list.get(i)) {
                max = list.get(i);
            }
        }
        return max;
    }
}
