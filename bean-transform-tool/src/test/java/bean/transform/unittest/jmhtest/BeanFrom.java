package bean.transform.unittest.jmhtest;

import bean.transform.unittest.entity.Inner;
import bean.transform.unittest.entity.ListElement;
import fr.xebia.extras.selma.Mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Classname CompareFrom
 * @Description TODO
 * @Date 2021/10/16 13:00
 * @Created by wen wang
 */


public class BeanFrom {

    private double douVar;

    private char characterValue='u';

    private String roadId = "";
    /** 路段长度
     *
     **/

    private float roadLength = -1f;
    /** 路段方向（枚举）
     *
     **/

    private Integer roadDirection = -1;
    /** 车道数
     *
     **/

    private Integer laneNum = -1;
    /** 行政区域
     *
     **/

    private String district = "";
    /** poi区域
     *
     **/

    private Integer poi = -1;
    /** 其他区域划分
     *
     **/

    private Integer otherDistrict = -1;
    /** 路段名称
     *
     **/

    private String roadName = "";

    private Integer roadType = -1;


    private String roadData ="";


    private String roadIn ="";


    private String roadOut ="";


    private Integer gridId = -1;


    private String nodeFrom = "";
    /** 终止节点编号
     *
     **/

    private String nodeTo ="";


    private String intersectionFrom = "";
    /** 终止路口编号
     *
     **/

    private String intersectionTo = "";
    /** 起始路口名称
     *
     **/

    private String roadNameFrom = "";
    /** 终止路口名称
     *
     **/

    private String roadNameTo = "";
    /** 车道宽度（米）
     *
     **/

    private Double laneWidth = -1.0;
    /** 分隔带情况
     *
     **/

    private Integer divider = -1;
    /** 自由流车速（公里/小时）
     *
     **/

    private float ffs =-1f;
    /** 道路编号
     *
     **/

    private String streetId ="";
    /** 阈值序列号
     *
     **/
    private Inner inner=new Inner();

    private Integer thresholdId =-1;
    /** 与行车方向关系
     *
     **/

    private Integer carDirection =-1;

    public Inner[][] getInnerarray() {
        return innerarray;
    }

    public void setInnerarray(Inner[][] innerarray) {
        this.innerarray = innerarray;
    }

    Inner[][] innerarray;

    public List<List<Inner>> getInnerDoubleList() {
        return innerDoubleList;
    }

    public void setInnerDoubleList(List<List<Inner>> innerDoubleList) {
        this.innerDoubleList = innerDoubleList;
    }

    List<List<Inner>> innerDoubleList;

    public Date getDateField() {
        return dateField;
    }

    public void setDateField(Date dateField) {
        this.dateField = dateField;
    }

    private Date dateField=new Date() ;


    public List<ListElement> getListElementList() {
        return listElementList;
    }

    public List<List<ListElement>> getNestList() {
        return nestList;
    }

    public List<List<List<Double>>> getThreeNestList() {
        return threeNestList;
    }

    public void setThreeNestList(List<List<List<Double>>> threeNestList) {
        this.threeNestList = threeNestList;
    }

    public void setNestList(List<List<ListElement>> nestList) {
        this.nestList = nestList;
    }

    private List<List<ListElement>>  nestList=new ArrayList<>();

    public ListElement[][] getNestArray() {
        return nestArray;
    }

    public void setNestArray(ListElement[][] nestArray) {
        this.nestArray = nestArray;
    }

    private ListElement[][]  nestArray=new ListElement[1][1];

    private List<List<List<Double>>>  threeNestList=new ArrayList<>();

    public void setListElementList(List<ListElement> listElementList) {
        this.listElementList = listElementList;
    }

    private List<ListElement> listElementList=new ArrayList<>();

    public String getRoadId() {
        return roadId;
    }

    public void setRoadId(String roadId) {
        this.roadId = roadId;
    }



    public Integer getRoadDirection() {
        return roadDirection;
    }

    public void setRoadDirection(Integer roadDirection) {
        this.roadDirection = roadDirection;
    }

    public Integer getLaneNum() {
        return laneNum;
    }

    public void setLaneNum(Integer laneNum) {
        this.laneNum = laneNum;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Integer getPoi() {
        return poi;
    }

    public void setPoi(Integer poi) {
        this.poi = poi;
    }

    public Integer getOtherDistrict() {
        return otherDistrict;
    }

    public void setOtherDistrict(Integer otherDistrict) {
        this.otherDistrict = otherDistrict;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public Integer getRoadType() {
        return roadType;
    }

    public void setRoadType(Integer roadType) {
        this.roadType = roadType;
    }

    public String getRoadData() {
        return roadData;
    }

    public void setRoadData(String roadData) {
        this.roadData = roadData;
    }

    public String getRoadIn() {
        return roadIn;
    }

    public void setRoadIn(String roadIn) {
        this.roadIn = roadIn;
    }

    public String getRoadOut() {
        return roadOut;
    }

    public void setRoadOut(String roadOut) {
        this.roadOut = roadOut;
    }

    public Integer getGridId() {
        return gridId;
    }

    public void setGridId(Integer gridId) {
        this.gridId = gridId;
    }

    public String getNodeFrom() {
        return nodeFrom;
    }

    public void setNodeFrom(String nodeFrom) {
        this.nodeFrom = nodeFrom;
    }

    public String getNodeTo() {
        return nodeTo;
    }



    public void setNodeTo(String nodeTo) {
        this.nodeTo = nodeTo;
    }

    public String getIntersectionFrom() {
        return intersectionFrom;
    }

    public void setIntersectionFrom(String intersectionFrom) {
        this.intersectionFrom = intersectionFrom;
    }

    public String getIntersectionTo() {
        return intersectionTo;
    }

    public void setIntersectionTo(String intersectionTo) {
        this.intersectionTo = intersectionTo;
    }

    public String getRoadNameFrom() {
        return roadNameFrom;
    }

    public void setRoadNameFrom(String roadNameFrom) {
        this.roadNameFrom = roadNameFrom;
    }

    public String getRoadNameTo() {
        return roadNameTo;
    }

    public void setRoadNameTo(String roadNameTo) {
        this.roadNameTo = roadNameTo;
    }

    public Double getLaneWidth() {
        return laneWidth;
    }

    public void setLaneWidth(Double laneWidth) {
        this.laneWidth = laneWidth;
    }

    public Integer getDivider() {
        return divider;
    }

    public void setDivider(Integer divider) {
        this.divider = divider;
    }

    public float getFfs() {
        return ffs;
    }

    public void setFfs(float ffs) {
        this.ffs = ffs;
    }

    public String getStreetId() {
        return streetId;
    }

    public void setStreetId(String streetId) {
        this.streetId = streetId;
    }

    public Integer getThresholdId() {
        return thresholdId;
    }

    public void setThresholdId(Integer thresholdId) {
        this.thresholdId = thresholdId;
    }

    public Integer getCarDirection() {
        return carDirection;
    }

    public void setCarDirection(Integer carDirection) {
        this.carDirection = carDirection;
    }


    public Inner getInner() {
        return inner;
    }

    public void setInner(Inner inner) {
        this.inner = inner;
    }

    public float getRoadLength() {
        return roadLength;
    }

    public void setRoadLength(float roadLength) {
        this.roadLength = roadLength;
    }

    public double getDouVar() {
        return douVar;
    }

    public void setDouVar(double douVar) {
        this.douVar = douVar;
    }

    public char getCharacterValue() {
        return characterValue;
    }

    public void setCharacterValue(char characterValue) {
        this.characterValue = characterValue;
    }



}
