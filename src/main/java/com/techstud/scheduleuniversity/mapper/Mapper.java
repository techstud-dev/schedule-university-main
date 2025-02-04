package com.techstud.scheduleuniversity.mapper;

@FunctionalInterface
public interface Mapper<T, R>{
    R map(T source);
}
