package bean.transform.unittest;

import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.ExtensionObjectTransform;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import bean.transform.unittest.entity.*;
import com.alibaba.fastjson.JSON;
import net.sf.cglib.beans.BeanCopier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname UnitTest
 * @Description TODO
 * @Date 2022/1/16 11:21
 * @Created by wen wang
 */
public class UnitTest {


    BeanCopier copier = BeanCopier.create(CopyFrom.class, CopyTo.class, false);

    BeanTransform beanTransFormsHandler =null;

    /**
     *  1 很荣幸代表团队来发表感言
     *  首先 说几点感谢吧，
     *  第一是感谢主管和领导们对AI-TRAFFIC 产品研发过程中的关心和支持，提供各种资源协调帮助
     *  第二 是感谢组内兄弟伙伴，在研发过程中大家共同努力攻克难点，
     *  顶着压力，加班加点保障项目开发进度，并积极参与落地推广过程中的而维护工作
     *  最后要感谢下兄弟团队，董明董工轻舟团队，测试团队，还有时空团队，
     *  轻舟是AI-TRAFFIC 的底座保障，测试是我们产品对外出口的保障，时空团队提供预测能力
     *
     *  目前，IA-TRAFFIC在在几十个个项目中落地推广使用，
     *  在交通秩序管控、交通仿真、态势研判、业务方面带来了比较可观的商业价值，也得到了用户的认可。
     *  可以说这也是对公司成就客户、价值为本理念的很好的实践吧。
     *
     *  接下来想说下对未来的展望，希望新的一年，我们AI-TRAFFIC 产品能够继续做大做强，积极推进技术改革，
     *  在云域一体化框架下完成产品的功能升级，为客户带来更大的价值，
     *  在国家交通强国的号召下继续也为交通事业贡献发光发热。希望我们组员能够继续保持战斗热情。
     *
     *  最后，提前给大家拜个年，祝虎年快乐心想事成。
     *  *
     * @return
     */

    public CopyFrom  createCopyFrom(){
        CopyFrom from = new CopyFrom();

        from.setCarDirection(2);
        from.setDistrict("xxx");
        from.setDivider(3);
        from.setFfs((float) 30);
        from.setGridId(4);
        from.setIntersectionFrom("A INTERSECTION");
        from.setIntersectionTo("B INTERSECTION");
        from.setLaneNum(300);
        from.setNodeFrom("A");
        from.setNodeTo("B");
        from.setPoi(null);
        from.setLaneWidth(3.5);
        from.setRoadData("32.555,106.789");
        from.setRoadId("in road");
        from.setRoadDirection(2);
        from.setRoadLength((float) 450);
        from.setRoadName("road A");
        from.setRoadNameFrom("road from");
        from.setRoadNameTo("road to");
        from.setRoadOut("road out");
        from.setRoadType(5);
        from.setThresholdId(1200);
        from.setOtherDistrict(12);

        Inner inner = new Inner();

        inner.setGreenRatio(30);
        inner.setPhaseId("1");
        inner.setPhaseName("phase1");
        inner.setPhaseSeqNo("3");
        inner.setRed(3);
        inner.setYellow(3);

        from.setInner(inner);

        List<ListElement> listElementListSource = new ArrayList<>();

        ListElement listElement1 = new ListElement();
        listElement1.setListElementField1("filed1");
        listElement1.setListElementField2(1001);
        listElement1.setListElementField3(1002);
        listElementListSource.add(listElement1);
        ListElement listElement2 = new ListElement();
        listElement2.setListElementField1("filed1");
        listElement2.setListElementField2(2001);
        listElement2.setListElementField3(2002);
        listElementListSource.add(listElement2);
        from.setListElementList(listElementListSource);
        List<List<ListElement>> nest = new ArrayList<>();
        nest.add(listElementListSource);
        // from.setNestList(nest);

        ListElement listElementArrEle = new ListElement();
        listElementArrEle.setListElementField1("filed1_for_array");
        listElementArrEle.setListElementField2(5001);
        listElementArrEle.setListElementField3(5002);
        ListElement[] arr1 = new ListElement[1];
        arr1[0] = listElementArrEle;
        ListElement[][] arr2 = new ListElement[1][1];
        arr2[0] = arr1;
        from.setNestArray(arr2);


        List<List<List<Double>>> threeNestList = new ArrayList<>();
        List<List<Double>> ll = new ArrayList<>();
        List<Double> l = new ArrayList<>();
        l.add(Double.valueOf(520.000));
        ll.add(l);
        threeNestList.add(ll);
        from.setThreeNestList(threeNestList);

        Inner[][] innerArray=new Inner[2][1];
        List<List<Inner>> innerDoubleList=new ArrayList<>();
        List<Inner> innerList=new ArrayList<>();
        Inner[] inners=new Inner[1];
        Inner innerElement = new Inner();

        innerElement.setGreenRatio(20);
        innerElement.setPhaseId("1");
        innerElement.setPhaseName("phase2");
        innerElement.setPhaseSeqNo("3");
        innerElement.setRed(3);
        innerElement.setYellow(3);

        innerList.add(innerElement);
        innerList.add(innerElement);
        innerDoubleList.add(innerList);

        inners[0]=innerElement;

        innerArray[0]=inners;
        innerArray[1]=inners;
        from.setInnerarray(innerArray);
        from.setInnerDoubleList(innerDoubleList);
        double d = 8.9;
        from.setDouVar(d);

        // set map value

        Inner mapInnerElement = new Inner();

        mapInnerElement.setGreenRatio(20);
        mapInnerElement.setPhaseId("1");
        mapInnerElement.setPhaseName("phase2 in map field");
        mapInnerElement.setPhaseSeqNo("3");
        mapInnerElement.setRed(3);
        mapInnerElement.setYellow(3);

        Map<String, Map<String,Inner>> twoLayerMap=new HashMap<>();
        Map<String,Inner> twoLayerInnerMap=new HashMap<>();
        twoLayerInnerMap.put("layer2",mapInnerElement);
        twoLayerMap.put("layer1",twoLayerInnerMap);
        from.setTwoLayerMap(twoLayerMap);
        return from;
    }

    public UnitTest() {


        try {
          //  beanTransFormsHandler =TransformUtilGenerate.generate(CopyFrom.class, CopyTo.class, true, true, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    CopyFrom from=createCopyFrom();


    @Test
    public void test() throws Exception {


        System.out.println();
        System.out.println();


        CopyFrom from=createCopyFrom();
        int times = 1;

        int loop = 1;




        System.out.println(org.objectweb.asm.Type.getInternalName(char.class));



        System.out.println(" cglib  BeanTransFormsHandler  springBeanUtils   Copier 转换拷贝bean 对比测试");


        beanTransFormsHandler = TransformUtilGenerate.generate(CopyFrom.class, CopyTo.class, true, true,null);



        //  BeanUtils.copyProperties(from,new CopyTo());
        //   org.apache.commons.beanutils.BeanUtils.cloneBean(from);

        CopyTo copyTo1 = new CopyTo();
        CopyTo copyTo2 = new CopyTo();
        CopyTo copyTo3 = new CopyTo();
        CopyTo copyTo4 = new CopyTo();
        CopyTo copyTo5 = new CopyTo();


        for (int lo = 0; lo < loop; ++lo) {
            long time1 = System.nanoTime();

            for (int j = 0; j < times; ++j) {

                copyTo1 = new CopyTo();
                copier.copy(from, copyTo1, null);

                // System.out.println(JSON.toJSONString(compareTo));
            }
            long time2 = System.nanoTime();
            System.out.println("from=" + JSON.toJSONString(from));
            System.out.println(" copyTo1=" + JSON.toJSONString(copyTo1));
            long time3_1 = System.nanoTime();


            for (int K = 0; K < times; ++K) {


                copyTo2 =  beanTransFormsHandler.beanTransform(CopyFrom.class,
                        from,
                        CopyTo.class);

                from.getInnerDoubleList().get(0).get(1).setPhaseName("修改phase");

                System.out.println("修改 from ="+JSON.toJSONString(from));
                System.out.println("修改 copyTo2 ="+JSON.toJSONString(copyTo2));

                //  System.out.println("from="+JSON.toJSONString(from));
                // System.out.println("copyTo2="+JSON.toJSONString(copyTo2));


            }


            long time3 = System.nanoTime();
            System.out.println("from=" + JSON.toJSONString(from));
            System.out.println("copyTo2=" + JSON.toJSONString(copyTo2));

            long time4_1 = System.nanoTime();
            for (int K = 0; K < times; ++K) {

                copyTo3 = new CopyTo();
                //  org.springframework.beans.BeanUtils.copyProperties(from,copyTo3);
                //   org.springframework.beans.BeanUtils.copyProperties(from,copyTo3);


            }

            long time4 = System.nanoTime();


            for (int K = 0; K < times; ++K) {

                copyTo3 = new CopyTo();
                //  org.apache.commons.beanutils.BeanUtils.copyProperties(from,copyTo3);

                // 深度克隆拷贝
                // CopyFrom from1= (CopyFrom) org.apache.commons.beanutils.BeanUtils.cloneBean(from);


            }

            long time5 = System.nanoTime();

            //  MapperStructConvert mapperStructConvert=  MapperStructConvert.INSTANCE;
            long time6_1 = System.nanoTime();

            for (int K = 0; K < times; ++K) {

                //    copyTo5 =mapperStructConvert.transform(from);


            }

            long time6 = System.nanoTime();

            System.out.println("MapperStructConvert:" + JSON.toJSONString(copyTo5));


            System.out.println(" 转换拷贝bean " + times + "次 总时间（毫秒）, " + ",cglib vs BeanTransFormsHandler vs spring " + "： "
                    + (time2 - time1) / 1000000 + "   "
                    + (time3 - time3_1) / 1000000 + "  " +
                    (time4 - time4_1) / 1000000 + "  ");



        }


    }


    public void tes(){
        tes1();
    }

    public Object tes1(){
        int a=3;
        return a;
    }
}
