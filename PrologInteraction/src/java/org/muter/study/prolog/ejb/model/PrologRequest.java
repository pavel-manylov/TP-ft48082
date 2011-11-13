/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Метод описывает запрос выполняемый пролог программой или к пролог программе.
 * @author muter
 */
public class PrologRequest extends PrologEvent {
    private Class _valueClass;
    private List<Serializable> _arguments;

    /**
     * @param metainfo описание запроса.
     * @param valueClass предполагаемый класс значения ответа или null, если это не важно.
     * @param arguments аргументы запроса.
     */
    public PrologRequest(String metainfo, Class valueClass, Serializable... arguments) {
        super(metainfo);
        this._valueClass = valueClass;
        _arguments = Collections.unmodifiableList(Arrays.asList(arguments));
    }

    /**
     * @param metainfo описание запроса.
     * @param valueClass предполагаемый класс значения ответа или null, если это не важно.
     * @param flusedData данные из выходного потока пролог программы.
     * @param arguments аргументы запроса.
     */
    public PrologRequest(String metainfo, Class valueClass, String flushedData, Serializable... arguments) {
        super(metainfo, flushedData);
        this._valueClass = valueClass;
        _arguments = Collections.unmodifiableList(Arrays.asList(arguments));
    }


    /**
     * @param metainfo описание запроса.
     * @param valueClass предполагаемый класс значения ответа или null, если это не важно.
     * @param flusedData данные из выходного потока пролог программы.
     * @param resultList набор аргументов пролог программы,
     * разрешенных с ее помощью в данной итерации.
     * @param arguments аргументы запроса.
     */
    public PrologRequest(String metainfo, Class valueClass, String flushedData, List<PrologResult> resultList, Serializable... arguments) {
        super(metainfo, flushedData, resultList);
        this._valueClass = valueClass;
        _arguments = Collections.unmodifiableList(Arrays.asList(arguments));
    }


    /**
     * Предполагаемый класс значения ответа или null, если это не важно
     */
    public Class getValueClass() {
        return _valueClass;
    }

    /**
     * Аргументы запроса.
     */
    public List<Serializable> getArguments() {
        return _arguments;
    }


}
