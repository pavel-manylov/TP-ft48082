/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс описывает некоторое события из цикла сообщения пролог программы и
 * ее интерфейса.
 * @author muter
 */
public abstract class PrologEvent implements Serializable {
    private String _metainfo;
    private String _flusedData;
    private List<PrologResult> _resultList;

    /**
     * @param metainfo Описательная информация о событии.
     */
    public PrologEvent(String metainfo) {
        this._metainfo = metainfo;
    }

    /**
     * @param metainfo описательная информация о событии.
     * @param flusedData данные из выходного потока пролог программы.
     */
    public PrologEvent(String metainfo, String flusedData) {
        this._metainfo = metainfo;
        this._flusedData = flusedData;
    }


    /**
     * @param metainfo описательная информация о событии.
     * @param flusedData данные из выходного потока пролог программы.
     * @param resultList набор аргументов пролог программы,
     * разрешенных с ее помощью в данной итерации.
     */
    public PrologEvent(String metainfo, String flusedData, List<PrologResult> resultList) {
        this._metainfo = metainfo;
        this._flusedData = flusedData;
        this._resultList = resultList;
    }


    /**
     * Описательная информация о событии
     */
    public String getMetainfo() {
        return _metainfo;
    }

    /**
     * Метод получает набор аргументов пролог программы,
     * разрешенных с ее помощью.
     */
    public List<PrologResult> getResultList() {
        if(_resultList == null)
            return Collections.emptyList();
        return _resultList;
    }

    /**
     * Метод возвращает данные из выходного потока пролог программы.
     */
    public String getFlusedData() {
        return _flusedData;
    }



}
