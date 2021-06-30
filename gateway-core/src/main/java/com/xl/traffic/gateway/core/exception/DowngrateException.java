package com.xl.traffic.gateway.core.exception;

import com.xl.traffic.gateway.core.enums.ExceptionCode;

import static com.xl.traffic.gateway.core.enums.ExceptionCode.DOWNGRADE;

/**
 * 降级后抛的异常
 * <p>
 * Created by manzhizhen on 17/5/1.
 */
public class DowngrateException extends RuntimeException {

    private String point;

    private int code = DOWNGRADE.getCode();

    private String msg = DOWNGRADE.getMsg();

    public DowngrateException() {
        super(DOWNGRADE.getMsg());
    }

    public DowngrateException(String point, int code, String msg) {
        super(msg);

        this.point = point;
        this.code = code;
        this.msg = msg;
    }

    public DowngrateException(int code, String msg) {
        super(msg);

        this.code = code;
        this.msg = msg;
    }

    public DowngrateException(String point) {
        super(DOWNGRADE.getMsg());

        this.point = point;
    }

    public DowngrateException(ExceptionCode downgrade) {
        super(downgrade.getMsg());

        this.code = downgrade.getCode();
        this.msg = downgrade.getMsg();
    }

    public DowngrateException(String point, ExceptionCode downgrade) {
        super(downgrade.getMsg());

        this.point = point;
        this.code = downgrade.getCode();
        this.msg = downgrade.getMsg();
    }


    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SdsException{");
        sb.append("point='").append(point).append('\'');
        sb.append(", code=").append(code);
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
