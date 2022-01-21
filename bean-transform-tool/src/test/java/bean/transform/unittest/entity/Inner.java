package bean.transform.unittest.entity;

/**
 * @Classname Nest
 * @Description TODO
 * @Date 2021/11/25 11:51
 * @Created by wen wang
 */
public class Inner {

    private String phaseId = "";

    private String phaseName = "";

    private String phaseSeqNo = "";

    private Integer greenRatio = -1;


    private Integer red = -1;


    private Integer yellow = -1;

    public String getPhaseId() {
        return phaseId;
    }

    public void setPhaseId(String phaseId) {
        this.phaseId = phaseId;
    }

    public String getPhaseName() {
        return phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public String getPhaseSeqNo() {
        return phaseSeqNo;
    }

    public void setPhaseSeqNo(String phaseSeqNo) {
        this.phaseSeqNo = phaseSeqNo;
    }

    public Integer getGreenRatio() {
        return greenRatio;
    }

    public void setGreenRatio(Integer greenRatio) {
        this.greenRatio = greenRatio;
    }

    public Integer getRed() {
        return red;
    }

    public void setRed(Integer red) {
        this.red = red;
    }

    public Integer getYellow() {
        return yellow;
    }

    public void setYellow(Integer yellow) {
        this.yellow = yellow;
    }

    @Override
    public String toString() {
        return "Phase{" +
                "phaseId='" + phaseId + '\'' +
                ", phaseName='" + phaseName + '\'' +
                ", phaseSeqNo='" + phaseSeqNo + '\'' +
                ", greenRatio=" + greenRatio +
                ", red=" + red +
                ", yellow=" + yellow +
                '}';
    }
}
