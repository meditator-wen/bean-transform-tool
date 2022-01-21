package bean.transform.unittest.jmhtest.mapperstruct;

import bean.transform.unittest.jmhtest.CopyFrom;
import bean.transform.unittest.jmhtest.CopyTo;

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

    CopyTo transform(CopyFrom from);
}
