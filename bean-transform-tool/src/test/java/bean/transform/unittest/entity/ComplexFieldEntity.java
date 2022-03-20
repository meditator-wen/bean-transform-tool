package bean.transform.unittest.entity;

import com.shzz.common.tool.bean.BeanFieldInfo;

import java.util.*;

/**
 * @author by wen wang
 * @description TODO
 * @created 2022/3/18 14:47
 */
public class ComplexFieldEntity {
    @BeanFieldInfo(sourceFieldName = "innerDoubleList")
    Inner[][] innerarray;

    public Inner[][] getInnerarray() {
        return innerarray;
    }

    public void setInnerarray(Inner[][] innerarray) {
        this.innerarray = innerarray;
    }

    private List<ListElement> listElementList = new ArrayList<>();

    @BeanFieldInfo(userExtend = false, sourceFieldName = "nestArray", autoTransform = true)
    private List<List<ListElement>> nestList = new ArrayList<>();


    public Stack<List<ListElement>> getNestSetStack() {
        return nestSetStack;
    }

    public void setNestSetStack(Stack<List<ListElement>> nestSetStack) {
        this.nestSetStack = nestSetStack;
    }

    @BeanFieldInfo(userExtend = false, sourceFieldName = "nestSetStack", autoTransform = true)
    private Stack<List<ListElement>> nestSetStack;


    public Map<String, Map<String, Inner>> getTwoLayerMapCopyTo() {
        return twoLayerMapCopyTo;
    }

    public void setTwoLayerMapCopyTo(Map<String, Map<String, Inner>> twoLayerMapCopyTo) {
        this.twoLayerMapCopyTo = twoLayerMapCopyTo;
    }

    @BeanFieldInfo(userExtend = false, sourceFieldName = "twoLayerMap", autoTransform = true)
    private Map<String, Map<String, Inner>> twoLayerMapCopyTo;

    public List<Map<String, Map<String, Inner>>> getListContainTwoLayerMap() {
        return listContainTwoLayerMap;
    }

    public void setListContainTwoLayerMap(List<Map<String, Map<String, Inner>>> listContainTwoLayerMap) {
        this.listContainTwoLayerMap = listContainTwoLayerMap;
    }

    private List<Map<String, Map<String, Inner>>> listContainTwoLayerMap;
    private List<Inner[][]> listContainArray;
    private Map<Inner, List<String>> mapKeyNotPrimitive;

//    @BeanFieldInfo(sourceFieldName = "threeNestStringList")
//    private char[][][] intThreeDems;

    @BeanFieldInfo(sourceFieldName = "threeNestList")
    private Double[][][] doubleThreeDems;


    public List<ListElement> getListElementList() {
        return listElementList;
    }

    public void setListElementList(List<ListElement> listElementList) {
        this.listElementList = listElementList;
    }

    public List<List<ListElement>> getNestList() {
        return nestList;
    }

    public void setNestList(List<List<ListElement>> nestList) {
        this.nestList = nestList;
    }

    public List<Inner[][]> getListContainArray() {
        return listContainArray;
    }

    public void setListContainArray(List<Inner[][]> listContainArray) {
        this.listContainArray = listContainArray;
    }

    public Map<Inner, List<String>> getMapKeyNotPrimitive() {
        return mapKeyNotPrimitive;
    }

    public void setMapKeyNotPrimitive(Map<Inner, List<String>> mapKeyNotPrimitive) {
        this.mapKeyNotPrimitive = mapKeyNotPrimitive;
    }

//    public char[][][] getIntThreeDems() {
//        return intThreeDems;
//    }
//
//    public void setIntThreeDems(char[][][] intThreeDems) {
//        this.intThreeDems = intThreeDems;
//    }

    public Double[][][] getDoubleThreeDems() {
        return doubleThreeDems;
    }

    public void setDoubleThreeDems(Double[][][] doubleThreeDems) {
        this.doubleThreeDems = doubleThreeDems;
    }
}
