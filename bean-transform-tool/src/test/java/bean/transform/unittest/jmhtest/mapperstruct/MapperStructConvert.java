package bean.transform.unittest.jmhtest.mapperstruct;

import bean.transform.unittest.jmhtest.BeanFrom;
import bean.transform.unittest.jmhtest.BeanTo;

import org.mapstruct.Mapper;
import org.mapstruct.control.DeepClone;
import org.mapstruct.factory.Mappers;

/**
 * @Classname MapperStructConvert
 * @Description TODO
 * @Date 2021/12/1 12:07
 * @Created by wen wang
 */
@Mapper(mappingControl = DeepClone.class)
public interface MapperStructConvert {
     MapperStructConvert INSTANCE = Mappers.getMapper(MapperStructConvert.class);

    BeanTo transform(BeanFrom from);
}
