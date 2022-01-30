package bean.transform.unittest.jmhtest;

import bean.transform.unittest.entity.Inner;
import bean.transform.unittest.entity.ListElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Classname Manual
 * @Description TODO
 * @Date 2022/1/11 14:29
 * @Created by wen wang
 */
public class Manual {

    public BeanTo transformManual(BeanFrom from) {
        if (from == null) {
            return null;
        } else {
            BeanTo beanTo = new BeanTo();
            List<ListElement> list=from.getListElementList();
            if (list == null) {
                return null;
            } else {
                List<ListElement> list1 = new ArrayList(list.size()*2);
                Iterator var3 = list.iterator();

                while(var3.hasNext()) {
                    ListElement listElement = (ListElement)var3.next();
                    if (listElement != null) {
                        ListElement listElement1 = new ListElement();
                        listElement1.setListElementField1(listElement.getListElementField1());
                        listElement1.setListElementField2(listElement.getListElementField2());
                        listElement1.setListElementField3(listElement.getListElementField3());
                        list1.add(listElement1);
                    }

                }

                beanTo.setListElementList(list1);
            }


            beanTo.setRoadId(from.getRoadId());
            beanTo.setRoadLength(from.getRoadLength());
            beanTo.setRoadDirection(from.getRoadDirection());
            beanTo.setLaneNum(from.getLaneNum());
            beanTo.setDistrict(from.getDistrict());
            beanTo.setOtherDistrict(from.getOtherDistrict());
            beanTo.setRoadName(from.getRoadName());
            beanTo.setRoadType(from.getRoadType());
            beanTo.setRoadData(from.getRoadData());
            beanTo.setRoadIn(from.getRoadIn());
            beanTo.setRoadOut(from.getRoadOut());
            beanTo.setDouVar(from.getDouVar());
            beanTo.setCharacterValue(from.getCharacterValue());
            beanTo.setPoi(from.getPoi());
            beanTo.setNodeFrom(from.getNodeFrom());
            beanTo.setNodeTo(from.getNodeTo());
            beanTo.setIntersectionFrom(from.getIntersectionFrom());
            beanTo.setIntersectionTo(from.getIntersectionTo());
            beanTo.setRoadNameFrom(from.getRoadNameFrom());
            beanTo.setRoadNameTo(from.getRoadNameTo());
            beanTo.setLaneWidth(from.getLaneWidth());
            beanTo.setDivider(from.getDivider());
            beanTo.setFfs(from.getFfs());
            beanTo.setStreetId(from.getStreetId());
            beanTo.setThresholdId(from.getThresholdId());
            beanTo.setCarDirection(from.getCarDirection());
            beanTo.setGridId(from.getGridId());


            Inner inner= from.getInner();
            Inner inner1 = new Inner();
            inner1.setPhaseId(inner.getPhaseId());
            inner1.setPhaseName(inner.getPhaseName());
            inner1.setPhaseSeqNo(inner.getPhaseSeqNo());
            inner1.setGreenRatio(inner.getGreenRatio());
            inner1.setRed(inner.getRed());
            inner1.setYellow(inner.getYellow());
            beanTo.setInner(inner1);
            return beanTo;
        }
    }

    protected ListElement listElementToListElement(ListElement listElement) {
        if (listElement == null) {
            return null;
        } else {
            ListElement listElement1 = new ListElement();
            listElement1.setListElementField1(listElement.getListElementField1());
            listElement1.setListElementField2(listElement.getListElementField2());
            listElement1.setListElementField3(listElement.getListElementField3());
            return listElement1;
        }
    }

    protected List<ListElement> listElementListToListElementList(List<ListElement> list) {
        if (list == null) {
            return null;
        } else {
            List<ListElement> list1 = new ArrayList(list.size());
            Iterator var3 = list.iterator();

            while(var3.hasNext()) {
                ListElement listElement = (ListElement)var3.next();
                list1.add(this.listElementToListElement(listElement));
            }

            return list1;
        }
    }

    protected Inner innerToInner(Inner inner) {
        if (inner == null) {
            return null;
        } else {
            Inner inner1 = new Inner();
            inner1.setPhaseId(inner.getPhaseId());
            inner1.setPhaseName(inner.getPhaseName());
            inner1.setPhaseSeqNo(inner.getPhaseSeqNo());
            inner1.setGreenRatio(inner.getGreenRatio());
            inner1.setRed(inner.getRed());
            inner1.setYellow(inner.getYellow());
            return inner1;
        }
    }


}
