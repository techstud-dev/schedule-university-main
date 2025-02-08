package com.techstud.scheduleuniversity.mapper;

import org.springframework.hateoas.CollectionModel;

import java.util.List;

public interface MultipleMapper <T, R> extends Mapper<T, R> {
    CollectionModel<R> mapAll(List<T> source);
}
