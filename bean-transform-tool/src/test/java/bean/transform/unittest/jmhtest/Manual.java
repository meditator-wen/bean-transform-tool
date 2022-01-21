package bean.transform.unittest.jmhtest;

import bean.transform.unittest.jmhtest.CopyFrom ;
import bean.transform.unittest.jmhtest.CopyTo ;
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

    public CopyTo transformManual(CopyFrom from) {
        if (from == null) {
            return null;
        } else {
            CopyTo copyTo = new CopyTo();
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

                copyTo.setListElementList(list1);
            }


            copyTo.setRoadId(from.getRoadId());
            copyTo.setRoadLength(from.getRoadLength());
            copyTo.setRoadDirection(from.getRoadDirection());
            copyTo.setLaneNum(from.getLaneNum());
            copyTo.setDistrict(from.getDistrict());
            copyTo.setOtherDistrict(from.getOtherDistrict());
            copyTo.setRoadName(from.getRoadName());
            copyTo.setRoadType(from.getRoadType());
            copyTo.setRoadData(from.getRoadData());
            copyTo.setRoadIn(from.getRoadIn());
            copyTo.setRoadOut(from.getRoadOut());
            copyTo.setDouVar(from.getDouVar());
            copyTo.setCharacterValue(from.getCharacterValue());
            copyTo.setPoi(from.getPoi());
            copyTo.setNodeFrom(from.getNodeFrom());
            copyTo.setNodeTo(from.getNodeTo());
            copyTo.setIntersectionFrom(from.getIntersectionFrom());
            copyTo.setIntersectionTo(from.getIntersectionTo());
            copyTo.setRoadNameFrom(from.getRoadNameFrom());
            copyTo.setRoadNameTo(from.getRoadNameTo());
            copyTo.setLaneWidth(from.getLaneWidth());
            copyTo.setDivider(from.getDivider());
            copyTo.setFfs(from.getFfs());
            copyTo.setStreetId(from.getStreetId());
            copyTo.setThresholdId(from.getThresholdId());
            copyTo.setCarDirection(from.getCarDirection());
            copyTo.setGridId(from.getGridId());


            Inner inner= from.getInner();
            Inner inner1 = new Inner();
            inner1.setPhaseId(inner.getPhaseId());
            inner1.setPhaseName(inner.getPhaseName());
            inner1.setPhaseSeqNo(inner.getPhaseSeqNo());
            inner1.setGreenRatio(inner.getGreenRatio());
            inner1.setRed(inner.getRed());
            inner1.setYellow(inner.getYellow());
            copyTo.setInner(inner1);
            return copyTo;
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
