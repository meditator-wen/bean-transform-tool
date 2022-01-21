package bean.transform.unittest.entity;

/**
 * @Classname ListElement
 * @Description TODO
 * @Date 2021/11/28 11:43
 * @Created by wen wang
 */
public class ListElement {

    private String listElementField1 = "";

    private Integer listElementField2 = -1;


    private Integer listElementField3 = -1;

//    @BeanFieldInfo
//    List<Inner> inners;


    public String getListElementField1() {
        return listElementField1;
    }

    public void setListElementField1(String listElementField1) {
        this.listElementField1 = listElementField1;
    }

    public Integer getListElementField2() {
        return listElementField2;
    }

    public void setListElementField2(Integer listElementField2) {
        this.listElementField2 = listElementField2;
    }

    public Integer getListElementField3() {
        return listElementField3;
    }

    public void setListElementField3(Integer listElementField3) {
        this.listElementField3 = listElementField3;
    }
}
