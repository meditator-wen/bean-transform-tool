package bean.transform.unittest.jmhtest.selma;

import bean.transform.unittest.jmhtest.BeanFrom;
import bean.transform.unittest.jmhtest.BeanTo;
import fr.xebia.extras.selma.Mapper;

/**
 * @Classname SelmaMapper
 * @Description TODO
 * @Date 2022/1/28 21:39
 * @Created by wen wang
 */
@Mapper(withIgnoreFields = {"bean.transform.unittest.jmhtest.BeanFrom.innerarray"
        , "bean.transform.unittest.jmhtest.BeanFrom.innerdoublelist",
        "bean.transform.unittest.jmhtest.BeanFrom.nestarray",
        "bean.transform.unittest.jmhtest.BeanFrom.nestlist",
        "bean.transform.unittest.jmhtest.BeanFrom.threenestlist",
        "bean.transform.unittest.jmhtest.BeanTo.datefiled",
        "bean.transform.unittest.jmhtest.BeanFrom.datefield"})
public interface SelmaMapper {
    // This will build a fresh new OrderDto
    BeanTo asCopyTo(BeanFrom in);

}
