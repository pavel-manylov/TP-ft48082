/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb.controller;

import java.util.List;
import jp.ac.kobe_u.cs.prolog.lang.PrologControl;
import jp.ac.kobe_u.cs.prolog.lang.Term;

/**
 * Класс описывает утилиты для работы с прологом
 * @author muter
 */
public abstract class PrologPredicateProvider {
    private static final String PROVIDER_INSTANCE_KEY = "org.muter.stugy.prolog.ejb.controller.predicateProviderName";

    /**
     * Метод получает среду исполнения пролог программы по имени файла и имени предиката
     * @param prologName имя пролог файла
     * @param predicateName имя пролог программы
     * @param arguments аргументы предиката.
     * @return среда исполнения пролог программы.
     */
    public abstract PrologControl getPrologConfig(String prologName, String predicateName, Term[] arguments);

    /**
     * Метод транслирует указаный терм пролога в указанный javaClass
     * @param term терм пролога
     * @param valueClass java сlass
     * @return результат трансляции
     */
    public abstract Object extractPrologData(Term term, Class valueClass);

    /**
     * Метод транслирует набор аргументов в термы пролога
     * @param values аргументы
     * @return соответствующие термы пролога
     */
    public abstract Term[] prepareTerms(List<? extends Object> values);

    /**
     * Метод позволяет добавить дополнительный файл пролога
     * @param name имя файла, должно заканчиваться на .pl и не содержать пробелов
     * или иных whitespace символов.
     * @param data содержимое файла.
     * @param canOverride возможно ли переписать файл, если файл с таким именем уже существет
     * @return true, если файл был перезаписан, если был создан новый - false.
     */
    public abstract boolean createPrologFile(String name, byte[] data, boolean canOverride);

    /**
     * Метод создает новую сущность данного класса исходя из системного свойства или возвращает
     * сущность по умолчанию, если свойство не указано
     */
    public static PrologPredicateProvider createInstance(){
        try{
            if(System.getProperty(PROVIDER_INSTANCE_KEY) != null)
                return (PrologPredicateProvider) Class.forName(System.getProperty(PROVIDER_INSTANCE_KEY)).newInstance();
            else
                return new DefaultPrologPredicateProvider();
        }catch(ClassNotFoundException e){
            throw new RuntimeException(e);
        }catch(IllegalAccessException e){
            throw new RuntimeException(e);
        }catch(InstantiationException e){
            throw new RuntimeException(e);
        }
    }

}
