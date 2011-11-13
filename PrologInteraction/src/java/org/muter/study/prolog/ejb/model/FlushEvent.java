/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb.model;

import java.util.List;

/**
 * Событие сброса данных записанных пролог программой в буфер.
 * После обработки данного события управление следует вернуть в пролог программу.
 * @author muter
 */
public class FlushEvent extends PrologEvent{

    /**
     * @param metainfo Описательная информация о событии
     */
    public FlushEvent(String metainfo, String flusedData) {
        super(metainfo, flusedData);
    }

    /**
     * @param flusedData данные из выходного потока пролог программы.
     * @param resultList набор аргументов пролог программых
     * разрешенных с ее помощью в данной итерации.
     */
    public FlushEvent(String metainfo, String flusedData, List<PrologResult> resultList) {
        super(metainfo, flusedData, resultList);
    }
    

}
