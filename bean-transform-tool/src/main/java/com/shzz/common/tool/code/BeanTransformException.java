package com.shzz.common.tool.code;

/**
 * @Classname TdeBussinessException
 * @Description TODO
 * @Date 2020/6/10 18:21
 * @Created by wen wang
 */
public class BeanTransformException extends Exception implements ErrorEncoder{


    private String errorCode="not specific";
    private String errorOutline="not specific";
    private String errorChainDetail="not specific";

    public BeanTransformException(ErrorEncoder  errorEncoder){
        errorCode=errorEncoder.getErrorCode();
        errorOutline=errorEncoder.getErrorOutline();
        errorChainDetail=errorEncoder.getErrorChainDetail();


    }

    public BeanTransformException(Exception  ex){
        super(ex.getCause());

    }

    public BeanTransformException(String errorCode, String errorOutline, String errorChainDetail){
        this.errorChainDetail=errorChainDetail;
        this.errorCode=errorCode;
        this.errorOutline=errorOutline;

    }
    public BeanTransformException(String errorCode, String errorOutline, String errorChainDetail, Exception ex){
        super(ex.getCause());
        this.errorChainDetail=errorChainDetail;
        this.errorCode=errorCode;
        this.errorOutline=errorOutline;

    }


    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorOutline() {
        return errorOutline;
    }

    @Override
    public String getErrorChainDetail() {
        return errorChainDetail;
    }

    @Override
    public String toString() {
        return "TdeBussinessException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorOutline='" + errorOutline + '\'' +
                ", errorChainDetail='" + errorChainDetail + '\'' +
                '}';
    }
}
