/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb.controller;

import java.io.Serializable;
import jp.ac.kobe_u.cs.prolog.lang.Term;

/**
 * Интерфейс описвает обработчик запросов из пролог-программы.
 * экземпляр обкъета, реализующего данный интерфейс следует передать в пролог программу.
 * В дальнейшем обращение к нему осуществляется с помощью конструкции java_method,
 * предусмотренной в cafeProlog.
 * @author muter
 */
public interface BackListener extends Serializable{

    /**
     * Метод позволяет выполнить запрос общего вида.
     * @param metadata описание запроса
     * @param data данные по запросу
     * @param className имя класса - результата
     * @return результат выполнения запроса.
     */
    public Object callingBack(String metadata, Serializable data, String className);

    /**
     * Метод позволяет пролог программе записать некоторые данные в выходной поток.
     * Этим методом следует заменить вызовы встроенного предиката <code>write</code> в пролог программе.
     * @param data данные для записи
     */
    public void write(Object data);

    /**
     * Метод позволяет пролог программе прочитать некоторые данные из выходного потока.
     * Этим методом следует заменить вызовы встроенного предиката <code>read</code> в пролог программе.
     * @return результат чтения.
     */
    public Object read();

    /**
     * Метод записывает в выходной поток символ перевода каретки.
     * Этим методом следует заменить вызовы встроенного предиката <code>nl</code> в пролог программе.
     */
    public void nl();

    /**
     * Метод выводит все данные направленные ранее в выходной поток, однако еще не записаные в него,
     * а содержащиеся в буфере.
     * Этим методом следует заменить вызовы встроенного предиката <code>flush_output</code> в пролог программе.
     */
    public void flush();

}
