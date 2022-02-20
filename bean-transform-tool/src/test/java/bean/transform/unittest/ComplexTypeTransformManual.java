package bean.transform.unittest;

import bean.transform.unittest.entity.CopyFrom;
import bean.transform.unittest.entity.CopyTo;
import bean.transform.unittest.entity.Inner;

import java.util.*;

/**
 * @Classname ComplexTypeTransformManual
 * @Description TODO
 * @Date 2022/2/14 21:36
 * @Created by wen wang
 */
public class ComplexTypeTransformManual {
    public CopyTo beanTransforms(CopyFrom localVar_1_1) throws Exception {
        if (localVar_1_1 == null) {
            return null;
        } else {
            CopyTo localVar_1_0 = new CopyTo();

            Inner[][] localVar_1_2 = localVar_1_1.getInnerarray();

            if (localVar_1_2 != null) {
                int arrayLength = localVar_1_2.length;
                Inner[][] targetVar = new Inner[arrayLength][1];

                for (int index = 0; index < arrayLength; ++index) {
                    Inner[] tempElement = localVar_1_2[index];


                    int arrayLength1 = tempElement.length;
                    Inner[] targetVar1 = new Inner[arrayLength1];

                    for (int index1 = 0; index1 < arrayLength1; ++index1) {
                        Inner tempElement1 = tempElement[index1];
                        targetVar1[index1] = innerTransform(tempElement1);
                    }

                    targetVar[index] = targetVar1;
                }
                // localVar_1_0.setInnerarray(targetVar);

            }


//            List localVar_1_3 = localVar_1_1.getListElementList();
//            localVar_1_0.setListElementList((List)this.listElementList_convert.extensionObjectTransform(localVar_1_3, true));
//            ListElement[][] localVar_1_4 = localVar_1_1.getNestArray();
//            localVar_1_0.setNestList((List)this.nestList_convert.extensionObjectTransform(localVar_1_4, true));
            Map<String, Map<String, Inner>> localVar_1_5 = localVar_1_1.getTwoLayerMap();

            if (localVar_1_5 != null) {
                Map<String, Map<String, Inner>> targetVar = new HashMap(localVar_1_5.size() * 2);
                String tempKey = null;
                Map<String, Inner> tempValue = null;
                Set<String> sourceMapKeySet = localVar_1_5.keySet();
                Iterator<String> keySetIterator = sourceMapKeySet.iterator();

                while (keySetIterator.hasNext()) {
                    tempKey = (String) keySetIterator.next();
                    tempValue = localVar_1_5.get(tempKey);


                    Map<String, Inner> targetVar1 = new HashMap(tempValue.size() * 2);
                    String tempKey1 = null;
                    Inner tempValue1 = null;
                    Set<String> sourceMapKeySet1 = tempValue.keySet();
                    Iterator<String> keySetIterator1 = sourceMapKeySet1.iterator();

                    while (keySetIterator1.hasNext()) {
                        tempKey1 = keySetIterator1.next();
                        tempValue1 = tempValue.get(tempKey1);
                        targetVar1.put(tempKey1, innerTransform(tempValue1));
                    }


                    targetVar.put(tempKey, targetVar1);
                }

                //  localVar_1_0.setTwoLayerMap(targetVar);
            }


            //           localVar_1_0.setTwoLayerMap((Map)this.twoLayerMap_convert.extensionObjectTransform(localVar_1_5, true));
//            Map localVar_1_6 = localVar_1_1.getMapContainList();
//            localVar_1_0.setMapContainList((Map)this.mapContainList_convert.extensionObjectTransform(localVar_1_6, true));
//            List localVar_1_7 = localVar_1_1.getThreeNestList();
//            localVar_1_0.setIntThreeDems((int[][][])this.intThreeDems_convert.extensionObjectTransform(localVar_1_7, true));
//            List localVar_1_8 = localVar_1_1.getThreeNestStringList();
//            localVar_1_0.setDoubleThreeDems((Double[][][])this.doubleThreeDems_convert.extensionObjectTransform(localVar_1_8, true));
//            List localVar_1_9 = localVar_1_1.getThreeNestList();
//            localVar_1_0.setThreeNestList((Set)this.threeNestList_convert.extensionObjectTransform(localVar_1_9, true));
            return localVar_1_0;
        }

    }

    public Inner innerTransform(Inner fromInner) {
        if (fromInner == null) {
            return null;
        } else {
            Inner localVar_1_0 = new Inner();
            localVar_1_0.setPhaseId(fromInner.getPhaseId());
            localVar_1_0.setPhaseName(fromInner.getPhaseName());
            localVar_1_0.setPhaseSeqNo(fromInner.getPhaseSeqNo());
            localVar_1_0.setGreenRatio(fromInner.getGreenRatio());
            localVar_1_0.setRed(fromInner.getRed());
            localVar_1_0.setYellow(fromInner.getYellow());
            return localVar_1_0;
        }
    }
}
