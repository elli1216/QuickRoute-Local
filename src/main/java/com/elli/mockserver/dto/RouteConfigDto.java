package com.elli.mockserver.dto;

public class RouteConfigDto {

    private Integer status;
    private Integer delay;
    private Object body;

    public RouteConfigDto() {
    }

    public RouteConfigDto(Integer status, Integer delay, Object body) {
        this.status = status;
        this.delay = delay;
        this.body = body;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
