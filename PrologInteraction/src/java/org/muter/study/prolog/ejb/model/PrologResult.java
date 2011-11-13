/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс описывает набор значений аргументов пролог программы
 * которые не являлись литералами на момент ее запуска
 * и как следствие после каждой итерации исполнения программы
 * принимают новое значение.
 * @author muter
 */
public class PrologResult implements Serializable{
    private Map<String, Serializable> _values;

    /**
     * Конструтор копирует значение из указанного map.
     */
    public PrologResult(Map<String, Serializable> values){
        _values = Collections.unmodifiableMap(new LinkedHashMap<String, Serializable>(values));
    }

    /**
     * Конструктор обрыбатывает массив значений подразумевая,
     * что каждое четное значение - имя предиката, а каждое нечетное -
     * его значение.
     * Непарный четный элемент игнорируется.
     * 0 - четное число.
     * @throws ClassCastException если один из четных элементов не строка
     */
    public PrologResult(Serializable... values) throws ClassCastException{
        _values = new LinkedHashMap<String, Serializable>();
        for(int i = 1; i < values.length; i += 2)
            _values.put((String) values[i - 1],values[i]);
        _values = Collections.unmodifiableMap(_values);
    }

    /**
     * Немодифицируемый map имен праметров на их значения.
     */
    public Map<String, Serializable> getValues() {
        return _values;
    }

}
