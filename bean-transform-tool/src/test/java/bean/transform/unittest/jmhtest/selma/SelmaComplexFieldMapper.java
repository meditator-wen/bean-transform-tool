package bean.transform.unittest.jmhtest.selma;

import bean.transform.unittest.entity.ComplexFieldEntity;
import bean.transform.unittest.entity.TargetComplexFieldEntity;
import fr.xebia.extras.selma.Mapper;

/**
 * @author by wen wang
 * @description TODO
 * @created 2022/3/18 21:04
 */
@Mapper
public interface SelmaComplexFieldMapper {
    TargetComplexFieldEntity mapper(ComplexFieldEntity complexFieldEntity);
}
