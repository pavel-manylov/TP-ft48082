/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb.model;

import java.io.Serializable;
import java.util.List;

/**
 * Класс описывает ответ на запрос пролог программы или к пролог программе.
 * @see org.muter.study.prolog.ejb.model.PrologRequest
 * @author muter
 */
public class PrologResponse extends PrologEvent{
    private PrologRequest _request;
    private Serializable _value;

    /**
     * @param request запрос, ответом на который служит данный объект.
     * @param metainfo описание ответа.
     * @param value значение ответа, требуемое в запросе.
     */
    public PrologResponse(PrologRequest request, String metainfo, Serializable value) {
        super(metainfo);
        this._request = request;
        this._value = value;
    }

    /**
     * @param request запрос, ответом на который служит данный объект.
     * @param metainfo описание ответа.
     * @param flusedData данные из выходного потока пролог программы.
     * @param value значение ответа, требуемое в запросе.
     */
    public PrologResponse(PrologRequest request, String metainfo, String flushedData, Serializable value) {
        super(metainfo, flushedData);
        this._request = request;
        this._value = value;
    }
    
    /**
     * @param request запрос, ответом на который служит данный объект.
     * @param metainfo описание ответа.
     * @param flusedData данные из выходного потока пролог программы.
     * @param value значение ответа, требуемое в запросе.
     * @param resultList набор аргументов пролог программы,
     * разрешенных с ее помощью в данной итерации.
     */
    public PrologResponse(PrologRequest request, String metainfo, String flushedData, Serializable value, List<PrologResult> resultList) {
        super(metainfo, flushedData, resultList);
        this._request = request;
        this._value = value;
    }


    /**
     * Запрос, ответом на который служит данный объект.
     */
    public PrologRequest getRequest() {
        return _request;
    }

    /**
     * Значение ответа, требуемое в запросе.
     */
    public Serializable getValue() {
        return _value;
    }


}
