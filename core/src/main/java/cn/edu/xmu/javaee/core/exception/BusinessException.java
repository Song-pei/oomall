//School of Informatics Xiamen University, GPL-3.0 license
package cn.edu.xmu.javaee.core.exception;


import cn.edu.xmu.javaee.core.model.ReturnNo;


public class BusinessException extends RuntimeException{

    private ReturnNo errno;

    public BusinessException(ReturnNo errno, String message) {
        super(message);
        this.errno = errno;
    }

    public BusinessException(ReturnNo errno) {
        super(errno.getMessage());
        this.errno = errno;
    }
    // 添加这个新的构造函数
// Object... args 代表可以接收任意数量的参数
    public BusinessException(ReturnNo errno, Object... args) {
        // 自动把 args 填入 errno.getMessage() 的 %d 或 %s 里面
        super(String.format(errno.getMessage(), args));
        this.errno = errno;
    }

    public ReturnNo getErrno(){
        return this.errno;
    }
}
