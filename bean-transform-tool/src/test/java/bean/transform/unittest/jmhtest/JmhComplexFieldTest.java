package bean.transform.unittest.jmhtest;

import bean.transform.unittest.entity.*;
import bean.transform.unittest.jmhtest.selma.SelmaComplexFieldMapper;
import bean.transform.unittest.jmhtest.selma.SelmaMapper;
import com.alibaba.fastjson.JSON;
import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;
import fr.xebia.extras.selma.Selma;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author by wen wang
 * @description TODO
 * @created 2022/3/18 20:45
 */
@BenchmarkMode({Mode.Throughput}) // 指定mode为Mode.AverageTime
@OutputTimeUnit(TimeUnit.SECONDS) // 指定输出的耗时时长的单位
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 50, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@State(Scope.Benchmark)
public class JmhComplexFieldTest {
    //Get SelmaMapper
    SelmaComplexFieldMapper selmaMapper = Selma.builder(SelmaComplexFieldMapper.class).build();
    BeanTransform beanTransFormsHandler;
    ComplexFieldEntity complexFieldEntity;

    public CopyFrom createCopyFrom() {
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
        from.setRoadId("301");
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
        from.setNestList(nest);

        ListElement listElementArrEle = new ListElement();
        listElementArrEle.setListElementField1("filed1_for_array");
        listElementArrEle.setListElementField2(5001);
        listElementArrEle.setListElementField3(5002);
        ListElement[] arr1 = new ListElement[1];
        arr1[0] = listElementArrEle;
        ListElement[][] arr2 = new ListElement[1][1];
        arr2[0] = arr1;
        from.setNestArray(arr2);


        Set<Stack<ListElement>> setStack = new HashSet<>();

        Stack<ListElement> elementStack = new Stack<>();
        elementStack.push(listElementArrEle);
        setStack.add(elementStack);
        from.setNestSetStack(setStack);


        List<List<List<Double>>> threeNestList = new ArrayList<>();
        List<List<Double>> ll = new ArrayList<>();
        List<Double> l = new ArrayList<>();
        l.add(Double.valueOf(520.000));
        ll.add(l);
        threeNestList.add(ll);
        from.setThreeNestList(threeNestList);


        List<List<List<Character>>> threeNestCharacterList = new ArrayList<>();
        List<List<Character>> llCharacterList = new ArrayList<>();
        List<Character> lCharacterList = new ArrayList<>();
        lCharacterList.add(Character.valueOf('2'));
        llCharacterList.add(lCharacterList);
        threeNestCharacterList.add(llCharacterList);
        from.setThreeNestCharacterList(threeNestCharacterList);


        byte byteValue = (Double.valueOf(520.000)).byteValue();

        System.out.println("Double to byteValue=" + byteValue);
        System.out.println("Double to byteValue=" + JSON.toJSONString(byteValue));

        List<List<List<String>>> threeNestStringList = new ArrayList<>();
        List<List<String>> llStrings = new ArrayList<>();
        List<String> lStrings = new ArrayList<>();
        List<String> lStrings2 = new ArrayList<>();
        lStrings.add("20.3");
        lStrings.add("213");
        lStrings.add("214");

        lStrings2.add("125.9");
        llStrings.add(lStrings);
        llStrings.add(lStrings2);
        threeNestStringList.add(llStrings);
        from.setThreeNestStringList(threeNestStringList);

        Inner[][] innerArray = new Inner[2][1];
        List<List<Inner>> innerDoubleList = new ArrayList<>();
        List<Inner> innerList = new ArrayList<>();
        Inner[] inners = new Inner[1];
        Inner innerElement = new Inner();


        char rr = 9;
        Object ob = 9;

        innerElement.setGreenRatio(20);
        innerElement.setPhaseId("1");
        innerElement.setPhaseName("phase2");
        innerElement.setPhaseSeqNo("3");
        innerElement.setRed(3);
        innerElement.setYellow(3);

        innerList.add(innerElement);
        innerList.add(innerElement);
        innerDoubleList.add(innerList);

        List<String> stringList = new ArrayList<>();
        stringList.add("key is Inner Object,share with innerArray[0]");
        Map<Inner, List<String>> mapKeyNotPrimitive = new HashMap<>();
        mapKeyNotPrimitive.put(innerElement, stringList);
        from.setMapKeyNotPrimitive(mapKeyNotPrimitive);

        inners[0] = innerElement;

        innerArray[0] = inners;
        innerArray[1] = inners;
        from.setInnerarray(innerArray);
        from.setInnerDoubleList(innerDoubleList);

        List<Inner[][]> listContainArray = new ArrayList<>();
        listContainArray.add(innerArray);
        from.setListContainArray(listContainArray);
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

        Map<String, Map<String, Inner>> twoLayerMap = new HashMap<>();
        Map<String, Inner> twoLayerInnerMap = new HashMap<>();
        twoLayerInnerMap.put("layer2", mapInnerElement);
        twoLayerMap.put("layer1", twoLayerInnerMap);
        from.setTwoLayerMap(twoLayerMap);


        List<Map<String, Inner>> listContainMap = new ArrayList<>();
        List<Map<String, Map<String, Inner>>> listContaintwoLayerMap = new ArrayList<>();
        listContaintwoLayerMap.add(twoLayerMap);
        from.setListContainTwoLayerMap(listContaintwoLayerMap);

        listContainMap.add(twoLayerInnerMap);

        from.setListContainMap(listContainMap);

        Inner mapContainListInnerElement = new Inner();

        mapContainListInnerElement.setGreenRatio(30);
        mapContainListInnerElement.setPhaseId("1");
        mapContainListInnerElement.setPhaseName("phase2 in mapContainList field");
        mapContainListInnerElement.setPhaseSeqNo("3");
        mapContainListInnerElement.setRed(3);
        mapContainListInnerElement.setYellow(3);
        Map<String, List<Inner>> mapContainListField = new HashMap<>();

        List<Inner> mapContainListList = new ArrayList<>();

        mapContainListList.add(mapContainListInnerElement);
        mapContainListField.put("fieldMapwithList", mapContainListList);
        from.setMapContainList(mapContainListField);

        from.setObject(new Object());

        from.setJsonObject(JSON.parseObject(JSON.toJSONString(mapContainListInnerElement)));

        return from;
    }

    public JmhComplexFieldTest() {
        try {
            BeanTransform beanTransFormsHandler1 = TransformUtilGenerate.generate(CopyFrom.class, ComplexFieldEntity.class, true, true, null);
            CopyFrom copyFrom = createCopyFrom();
            System.out.println("copyFrom=" + JSON.toJSONString(copyFrom));
            complexFieldEntity = beanTransFormsHandler1.beanTransform(CopyFrom.class, copyFrom, ComplexFieldEntity.class);
            System.out.println("complexFieldEntity=" + JSON.toJSONString(complexFieldEntity));
            beanTransFormsHandler = TransformUtilGenerate.generate(ComplexFieldEntity.class, TargetComplexFieldEntity.class, true, true, null);

            TargetComplexFieldEntity beanTransformTarget = beanTransFormsHandler.beanTransform(ComplexFieldEntity.class,
                    complexFieldEntity,
                    TargetComplexFieldEntity.class);

            System.out.println("beanTransformTarget=" + JSON.toJSONString(beanTransformTarget));

            TargetComplexFieldEntity selmaTarget = selmaMapper.mapper(complexFieldEntity);

            System.out.println("selmaTarget=" + JSON.toJSONString(selmaTarget));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void benchMarkBeanTransformsHandler() throws Exception {

        TargetComplexFieldEntity beanTransformTarget = beanTransFormsHandler.beanTransform(ComplexFieldEntity.class,
                complexFieldEntity,
                TargetComplexFieldEntity.class);
    }

    @Benchmark
    public void benchMarkSelmaMapper() throws Exception {

        TargetComplexFieldEntity selmaTarget = selmaMapper.mapper(complexFieldEntity);
    }

    public static void main(String[] args) throws RunnerException {
        //String[] args
        //String[] args
        String docPath = System.getProperty("user.dir") + File.separator + "doc" + File.separator + "benchMarkJmhComplexFieldTest.log";
        ;
        System.out.println("JmhComplexFieldTest output path: " + docPath);
        Options options = new OptionsBuilder()
                .include(JmhComplexFieldTest.class.getSimpleName())
                .output(docPath)
                .build();
        new Runner(options).run();
    }


}
