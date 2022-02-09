package bean.transform.unittest.jmhtest;

import bean.transform.unittest.entity.Inner;
import bean.transform.unittest.entity.ListElement;

import bean.transform.unittest.jmhtest.selma.SelmaMapper;
import com.shzz.common.tool.bean.transform.BeanTransform;
import com.shzz.common.tool.bean.transform.asm.TransformUtilGenerate;

import bean.transform.unittest.jmhtest.mapperstruct.MapperStructConvert;
import fr.xebia.extras.selma.Selma;
import net.sf.cglib.beans.BeanCopier;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Classname TransformTest
 * @Description TODO
 * @Date 2021/10/11 22:16
 * @Created by wen wang
 */

@BenchmarkMode({Mode.Throughput}) // 指定mode为Mode.AverageTime
@OutputTimeUnit(TimeUnit.SECONDS) // 指定输出的耗时时长的单位
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 50, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@State(Scope.Benchmark)
public class Jmh {


    BeanCopier copier = BeanCopier.create(BeanFrom.class, BeanTo.class, false);

    BeanTransform beanTransFormsHandler = null;

    Manual manual = new Manual();

    MapperStructConvert mapperStructConvert = MapperStructConvert.INSTANCE;

    //Get SelmaMapper
    SelmaMapper selmaMapper = Selma.builder(SelmaMapper.class).build();

    public Jmh() {
        try {
            beanTransFormsHandler = TransformUtilGenerate.generate(BeanFrom.class, BeanTo.class, true, true, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BeanFrom from = createCopyFrom();

    @Benchmark
    public void benchMarkMapStruct() throws Exception {
        BeanTo beanTo4 = new BeanTo();
        beanTo4 = mapperStructConvert.transform(from);

    }

    @Benchmark
    public void benchMarkSpringBeanUtils() throws Exception {
        BeanTo beanTo3 = new BeanTo();
        org.springframework.beans.BeanUtils.copyProperties(from, beanTo3);


    }


    @Benchmark
    public void benchMarkBeanTransformsHandler() throws Exception {

        BeanTo beanTo2 = beanTransFormsHandler.beanTransform(BeanFrom.class,
                from,
                BeanTo.class);
    }


    @Benchmark
    public void benchMarkBeanManual() throws Exception {

        BeanTo manualCopy = manual.transformManual(from);
    }


    @Benchmark
    public void benchMarkSelmaMapper() throws Exception {

        BeanTo selmaCopy = selmaMapper.asCopyTo(from);
    }

    public static void main(String[] args) throws RunnerException {
        //String[] args
        //String[] args
        String docPath = System.getProperty("user.dir")+File.separator+"doc"+File.separator+"benchMarkTest.log";;
        System.out.println("jmh test output path: "+docPath);
        Options options = new OptionsBuilder()
                .include(Jmh.class.getSimpleName())
                .output(docPath)
                .build();
        new Runner(options).run();
    }


    public BeanFrom createCopyFrom() {
        BeanFrom from = new BeanFrom();

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

        Inner[][] innerArray = new Inner[2][1];
        List<List<Inner>> innerDoubleList = new ArrayList<>();
        List<Inner> innerList = new ArrayList<>();
        Inner[] inners = new Inner[1];
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

        inners[0] = innerElement;

        innerArray[0] = inners;
        innerArray[1] = inners;
        from.setInnerarray(innerArray);
        from.setInnerDoubleList(innerDoubleList);
        double d = 8.9;
        from.setDouVar(d);
        return from;
    }


}
