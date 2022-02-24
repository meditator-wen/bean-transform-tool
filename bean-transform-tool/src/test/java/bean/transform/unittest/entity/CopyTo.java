package bean.transform.unittest.entity;

import com.shzz.common.tool.bean.BeanFieldInfo;
import com.shzz.common.tool.code.CommonCode;

import java.util.*;

/**
 * @Classname CompareTo
 * @Description TODO
 * @Date 2021/10/16 13:00
 * @Created by wen wang
 */

public class CopyTo {

//    private double douVar;
//    //
//    private String characterValue ;

    private int roadId;
    /**
     * 路段长度
     **/

//    private int roadLength ;
//    /**
//     * 路段方向（枚举）
//     **/
//
//    private Integer roadDirection = -1;
//    /**
//     * 车道数
//     **/
//
//    private Integer laneNum = -1;
//    /**
//     * 行政区域
//     **/
//
//    private String district = "";
//    /**
//     * poi区域
//     **/
//
//    private Integer poi = -1;
//    /**
//     * 其他区域划分
//     **/
//
//    private Integer otherDistrict = -1;
//    /**
//     * 路段名称
//     **/
//
//    private String roadName = "";
//    /**
//     * 路段类型
//     * 高速路	1
//     * 快速路	2
//     * 主干路	3
//     * 次干路	4
//     * 支路	    5
//     **/
//
//    private byte roadType ;
//    /**
//     * 道路内点数据
//     **/
//
//    private String roadData = "";
//    /**
//     * 路段前向拓扑
//     **/
//
//    private String roadIn = "";
//    /**
//     * 路段后继拓扑
//     **/
//
//    private String roadOut = "";
//    /**
//     * 网格索引编号
//     **/
//
//    private Integer gridId = -1;
//    /**
//     * 起始节点编号
//     **/
//
//    private String nodeFrom = "";
//    /**
//     * 终止节点编号
//     **/
//
//    private String nodeTo = "";
//    /**
//     * 起始路口编号
//     **/
//
//    private String intersectionFrom = "";
//    /**
//     * 终止路口编号
//     **/
//
//    private String intersectionTo = "";
//    /**
//     * 起始路口名称
//     **/
//
//    private String roadNameFrom = "";
//    /**
//     * 终止路口名称
//     **/
//
//    private String roadNameTo = "";
//    /**
//     * 车道宽度（米）
//     **/
//
//    private byte laneWidth;
//    /**
//     * 分隔带情况
//     **/
//
//    private Double divider;
//    /**
//     * 自由流车速（公里/小时）
//     **/
//
//    private long ffs ;
//    /**
//     * 道路编号
//     **/
//
//    private String streetId = "";
//    /**
//     * 阈值序列号
//     **/
//    private Inner inner;
//
//    @BeanFieldInfo(sourceFieldName = "innerDoubleList")
//    Inner[][] innerarray;
//
//    public Inner[][] getInnerarray() {
//        return innerarray;
//    }
//
//    public void setInnerarray(Inner[][] innerarray) {
//        this.innerarray = innerarray;
//    }
//
//    private Integer thresholdId = -1;
//
//    @BeanFieldInfo(sourceFieldName = "dateField", autoTransform = true)
//    private Date dateFiled;
//
//
//    private List<ListElement> listElementList = new ArrayList<>();
//
//    @BeanFieldInfo(userExtend = false, sourceFieldName = "nestArray", autoTransform = true)
//    private List<List<ListElement>> nestList = new ArrayList<>();
//
//    public Map<String, Map<String, Inner>> getTwoLayerMap() {
//        return twoLayerMap;
//    }
//
//    public void setTwoLayerMap(Map<String, Map<String, Inner>> twoLayerMap) {
//        this.twoLayerMap = twoLayerMap;
//    }
//
//    private Map<String, Map<String, Inner>> twoLayerMap;
//
//    public List<Map<String, Map<String, Inner>>> getListContainTwoLayerMap() {
//        return listContainTwoLayerMap;
//    }
//
//    public void setListContainTwoLayerMap(List<Map<String, Map<String, Inner>>> listContainTwoLayerMap) {
//        this.listContainTwoLayerMap = listContainTwoLayerMap;
//    }
//
//    private List<Map<String, Map<String, Inner>>> listContainTwoLayerMap;
//    private List<Inner[][]> listContainArray;
//
//    public List<Inner[][]> getListContainArray() {
//        return listContainArray;
//    }
//
//    public void setListContainArray(List<Inner[][]> listContainArray) {
//        this.listContainArray = listContainArray;
//    }
//
//
//    public List<Map<String, Inner>> getListContainMap() {
//        return listContainMap;
//    }
//
//    public void setListContainMap(List<Map<String, Inner>> listContainMap) {
//        this.listContainMap = listContainMap;
//    }
//
//    private List<Map<String, Inner>> listContainMap;
//
//
//    public Map<String, List<Inner>> getMapContainList() {
//        return mapContainList;
//    }
//
//    public void setMapContainList(Map<String, List<Inner>> mapContainList) {
//        this.mapContainList = mapContainList;
//    }
//
//    private Map<String, List<Inner>> mapContainList;
//
    public char[][][] getIntThreeDems() {
        return intThreeDems;
    }

    public void setIntThreeDems(char[][][] intThreeDems) {
        this.intThreeDems = intThreeDems;
    }

    @BeanFieldInfo(sourceFieldName = "threeNestStringList")
    private char[][][] intThreeDems;

//
//    public void setDoubleThreeDems(Double[][][] doubleThreeDems) {
//        this.doubleThreeDems = doubleThreeDems;
//    }
//
//    public Double[][][] getDoubleThreeDems() {
//        return doubleThreeDems;
//    }
//
//    @BeanFieldInfo(sourceFieldName = "threeNestStringList")
//    private Double[][][] doubleThreeDems;
//
//    //
//    public Set<List<List<Integer>>> getThreeNestList() {
//        return threeNestList;
//    }
//
//    public void setThreeNestList(Set<List<List<Integer>>> threeNestList) {
//        this.threeNestList = threeNestList;
//    }
//
//    // @BeanFieldInfo(userExtend = false,extensionObjectTransformImplClass = "com.akfd.methodhandle.compare.ListTransforms")
//    private Set<List<List<Integer>>> threeNestList;
//
//    public List<List<ListElement>> getNestList() {
//        return nestList;
//    }
//
//    public void setNestList(List<List<ListElement>> nestList) {
//        this.nestList = nestList;
//    }
//
//    public List<ListElement> getListElementList() {
//        return listElementList;
//    }
//
//    public void setListElementList(List<ListElement> listElementList) {
//        this.listElementList = listElementList;
//    }

    /**
     * 与行车方向关系
     **/

//    private Integer carDirection = -1;

    // @BeanFieldInfo(userExtend = true, sourceFieldName = "commonCode",extensionObjectTransformImplClass = "bean.transform.CommonCodeEnumTransform")
    // private CommonCode commonCodeEnum;


    //    public Integer getRoadDirection() {
//        return roadDirection;
//    }
//
//    public void setRoadDirection(Integer roadDirection) {
//        this.roadDirection = roadDirection;
//    }
//
//    public Integer getLaneNum() {
//        return laneNum;
//    }
//
//    public void setLaneNum(Integer laneNum) {
//        this.laneNum = laneNum;
//    }
//
//    public String getDistrict() {
//        return district;
//    }
//
//    public void setDistrict(String district) {
//        this.district = district;
//    }
//
//    public String getCharacterValue() {
//        return characterValue;
//    }
//
//    public void setCharacterValue(String characterValue) {
//        this.characterValue = characterValue;
//    }
    //
    public int getRoadId() {
        return roadId;
    }

    public void setRoadId(int roadId) {
        this.roadId = roadId;
    }
//
//    public int getRoadLength() {
//        return roadLength;
//    }
//
//    public void setRoadLength(int roadLength) {
//        this.roadLength = roadLength;
//    }
//
//    public byte getRoadType() {
//        return roadType;
//    }
//
//    public void setRoadType(byte roadType) {
//        this.roadType = roadType;
//    }
//
//    public byte getLaneWidth() {
//        return laneWidth;
//    }
//
//    public void setLaneWidth(byte laneWidth) {
//        this.laneWidth = laneWidth;
//    }
//
//    public Double getDivider() {
//        return divider;
//    }
//
//    public void setDivider(Double divider) {
//        this.divider = divider;
//    }
//
//    public void setFfs(long ffs) {
//        this.ffs = ffs;
//    }

//    public CommonCode getCommonCodeEnum() {
//        return commonCodeEnum;
//    }
//
//    public void setCommonCodeEnum(CommonCode commonCodeEnum) {
//        this.commonCodeEnum = commonCodeEnum;
//    }

    //    public Integer getOtherDistrict() {
//        return otherDistrict;
//    }
//
//    public void setOtherDistrict(Integer otherDistrict) {
//        this.otherDistrict = otherDistrict;
//    }
//
//    public String getRoadName() {
//        return roadName;
//    }
//
//    public void setRoadName(String roadName) {
//        this.roadName = roadName;
//    }
//
//
//    public String getRoadData() {
//        return roadData;
//    }
//
//    public void setRoadData(String roadData) {
//        this.roadData = roadData;
//    }
//
//    public String getRoadIn() {
//        return roadIn;
//    }
//
//    public void setRoadIn(String roadIn) {
//        this.roadIn = roadIn;
//    }
//
//    public String getRoadOut() {
//        return roadOut;
//    }
//
//    public void setRoadOut(String roadOut) {
//        this.roadOut = roadOut;
//    }
//
//
//    public double getDouVar() {
//        return douVar;
//    }
//
//    public void setDouVar(double douVar) {
//        this.douVar = douVar;
//    }
//
//
//    public Integer getPoi() {
//        return poi;
//    }
//
//    public void setPoi(Integer poi) {
//        this.poi = poi;
//    }
//
//    public Date getDateFiled() {
//        return dateFiled;
//    }
//
//    public void setDateFiled(Date dateFiled) {
//        this.dateFiled = dateFiled;
//    }
//
//    public String getNodeFrom() {
//        return nodeFrom;
//    }
//
//    public void setNodeFrom(String nodeFrom) {
//        this.nodeFrom = nodeFrom;
//    }
//
//    public String getNodeTo() {
//        return nodeTo;
//    }
//
//    public void setNodeTo(String nodeTo) {
//        this.nodeTo = nodeTo;
//    }
//
//    public String getIntersectionFrom() {
//        return intersectionFrom;
//    }
//
//    public void setIntersectionFrom(String intersectionFrom) {
//        this.intersectionFrom = intersectionFrom;
//    }
//
//    public String getIntersectionTo() {
//        return intersectionTo;
//    }
//
//    public void setIntersectionTo(String intersectionTo) {
//        this.intersectionTo = intersectionTo;
//    }
//
//    public String getRoadNameFrom() {
//        return roadNameFrom;
//    }
//
//    public void setRoadNameFrom(String roadNameFrom) {
//        this.roadNameFrom = roadNameFrom;
//    }
//
//    public String getRoadNameTo() {
//        return roadNameTo;
//    }
//
//    public void setRoadNameTo(String roadNameTo) {
//        this.roadNameTo = roadNameTo;
//    }
//
//
//
//    public float getFfs() {
//        return ffs;
//    }
//
//
//    public String getStreetId() {
//        return streetId;
//    }
//
//    public void setStreetId(String streetId) {
//        this.streetId = streetId;
//    }
//
//    public Integer getThresholdId() {
//        return thresholdId;
//    }
//
//    public void setThresholdId(Integer thresholdId) {
//        this.thresholdId = thresholdId;
//    }
//
//    public Integer getCarDirection() {
//        return carDirection;
//    }
//
//    public void setCarDirection(Integer carDirection) {
//        this.carDirection = carDirection;
//    }
//
//
//    public Integer getGridId() {
//        return gridId;
//    }
//
//    public void setGridId(Integer gridId) {
//        this.gridId = gridId;
//    }
//
//    public Inner getInner() {
//        return inner;
//    }
//
//    public void setInner(Inner inner) {
//        this.inner = inner;
//    }
//
//
//    public void setDouVar(int douVar) {
//        this.douVar = douVar;
//    }


}