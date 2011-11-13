/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb;

import java.io.Serializable;
import javax.ejb.Remote;
import org.muter.study.prolog.ejb.model.PrologEvent;
import org.muter.study.prolog.ejb.model.PrologRequest;
import org.muter.study.prolog.ejb.model.PrologResponse;

/**
 * Интерфейс описывает бин, который взаимодействует с пролог программой
 * @author muter
 */
@Remote
public interface PrologPerformService extends Serializable {

    /**
     * Первое обращение к пролог программе
     * @param prologProgramm имя программы
     * @param prologPredicate имя предеката
     * @param request запрос, содержащий аргументы и т.п.
     * @return результат выполнения пролог программы или ее новый запрос.
     */
    public PrologEvent call(String prologProgramm, String prologPredicate, PrologRequest request);

    /**
     * Последующее обращение к пролог программе
     * @param response ответ на запрос пролог-программы.
     * @return результат выполнения пролог программы или ее новый запрос.
     */
    public PrologEvent callAgain(PrologResponse response);

    /**
     * Метод позволяет добавить дополнительный файл пролога
     * @param name имя файла, должно заканчиваться на .pl и не содержать пробелов
     * или иных whitespace символов.
     * @param data содержимое файла.
     */
    public void addPrologFile(String name, byte[] data);
    
}
