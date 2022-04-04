package bean.transform.unittest.entity;

import com.alibaba.fastjson.JSONObject;

/**
 * @author by wen wang
 * @description TODO
 * @created 2022/3/18 14:46
 */
public class BaseEntity {
    //
    private double douVar;
    //
    private String characterValue;

    private int roadId;
    /**
     * 路段长度
     **/

    private int roadLength;
    /**
     * 路段方向（枚举）
     **/

    private Integer roadDirection = -1;
    /**
     * 车道数
     **/

    private Integer laneNum = -1;
    /**
     * 行政区域
     **/

    private String district = "";
    /**
     * poi区域
     **/

    private Integer poi = -1;
    /**
     * 其他区域划分
     **/

    private Integer otherDistrict = -1;
    /**
     * 路段名称
     **/

    private String roadName = "";
    /**
     * 路段类型
     * 高速路	1
     * 快速路	2
     * 主干路	3
     * 次干路	4
     * 支路	    5
     **/

    private byte roadType;
    /**
     * 道路内点数据
     **/

    private String roadData = "";
    /**
     * 路段前向拓扑
     **/

    private String roadIn = "";
    /**
     * 路段后继拓扑
     **/

    private String roadOut = "";
    /**
     * 网格索引编号
     **/

    private Integer gridId = -1;
    /**
     * 起始节点编号
     **/

    private String nodeFrom = "";
    /**
     * 终止节点编号
     **/

    private String nodeTo = "";
    /**
     * 起始路口编号
     **/

    private String intersectionFrom = "";
    /**
     * 终止路口编号
     **/

    private String intersectionTo = "";
    /**
     * 起始路口名称
     **/

    private String roadNameFrom = "";
    /**
     * 终止路口名称
     **/

    private String roadNameTo = "";
    /**
     * 车道宽度（米）
     **/

    private byte laneWidth;
    /**
     * 分隔带情况
     **/

    private Double divider;
    /**
     * 自由流车速（公里/小时）
     **/

    private long ffs;
    /**
     * 道路编号
     **/

    private String streetId = "";
    /**
     * 阈值序列号
     **/
    private Inner inner;


    private Integer carDirection = -1;

    public double getDouVar() {
        return douVar;
    }

    public void setDouVar(double douVar) {
        this.douVar = douVar;
    }

    public String getCharacterValue() {
        return characterValue;
    }

    public void setCharacterValue(String characterValue) {
        this.characterValue = characterValue;
    }

    public int getRoadId() {
        return roadId;
    }

    public void setRoadId(int roadId) {
        this.roadId = roadId;
    }

    public int getRoadLength() {
        return roadLength;
    }

    public void setRoadLength(int roadLength) {
        this.roadLength = roadLength;
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

    public byte getRoadType() {
        return roadType;
    }

    public void setRoadType(byte roadType) {
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

    public byte getLaneWidth() {
        return laneWidth;
    }

    public void setLaneWidth(byte laneWidth) {
        this.laneWidth = laneWidth;
    }

    public Double getDivider() {
        return divider;
    }

    public void setDivider(Double divider) {
        this.divider = divider;
    }

    public long getFfs() {
        return ffs;
    }

    public void setFfs(long ffs) {
        this.ffs = ffs;
    }

    public String getStreetId() {
        return streetId;
    }

    public void setStreetId(String streetId) {
        this.streetId = streetId;
    }

    public Inner getInner() {
        return inner;
    }

    public void setInner(Inner inner) {
        this.inner = inner;
    }

    public Integer getCarDirection() {
        return carDirection;
    }

    public void setCarDirection(Integer carDirection) {
        this.carDirection = carDirection;
    }
}
