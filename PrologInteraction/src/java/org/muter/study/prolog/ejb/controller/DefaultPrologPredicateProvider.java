/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import jp.ac.kobe_u.cs.prolog.lang.DoubleTerm;
import jp.ac.kobe_u.cs.prolog.lang.IntegerTerm;
import jp.ac.kobe_u.cs.prolog.lang.JavaObjectTerm;
import jp.ac.kobe_u.cs.prolog.lang.Predicate;
import jp.ac.kobe_u.cs.prolog.lang.PredicateEncoder;
import jp.ac.kobe_u.cs.prolog.lang.PrologControl;
import jp.ac.kobe_u.cs.prolog.lang.SymbolTerm;
import jp.ac.kobe_u.cs.prolog.lang.Term;
import jp.ac.kobe_u.cs.prolog.lang.VariableTerm;

/**
 *
 * @author muter
 */
public class DefaultPrologPredicateProvider extends PrologPredicateProvider {

    @Override
    public PrologControl getPrologConfig(String prologName, String predicateName, Term[] arguments) {
        ClassLoader cl = makePrologClasses(prologName);
        Class predClass;
        try{
            String name = PredicateEncoder.encode("user", predicateName, arguments.length);
            predClass = cl.loadClass(name);
        }catch(ClassNotFoundException ex){
            try{
                String name = PredicateEncoder.encode("user", predicateName, arguments.length - 1);
                predClass = cl.loadClass(name);
            }catch(ClassNotFoundException e){
                throw new RuntimeException("Could not find not " + predicateName + "/" +
                                            (arguments.length - 1) + " nor " + predicateName + "/" +
                                            arguments.length, ex
                                          );
            }
        }
        Predicate pred;
        try{
            pred = (Predicate) predClass.newInstance();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
        PrologControl prolog = new PrologControl();
        prolog.setPredicate(pred, arguments);
        prolog.engine.setPrintStackTrace("on");
        return prolog;
    }

    private ClassLoader makePrologClasses(String prologName){
        File contanement;
        try {
            //черная магия класс лоадеров.
            //директория найденная по запросу "" не пойдет.
            //Только так.
            contanement = new File(Thread.currentThread().getContextClassLoader().getResource("org/muter/").toURI()).getParentFile().getParentFile();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        File root = new File(contanement, "pldir"), targetDir = new File(root, prologName);
        if(!root.exists())
            root.mkdir();
        if(!root.isDirectory())
            throw new IllegalStateException(prologName + ".pl");
        if(!targetDir.exists())
            findDir(targetDir);
        try {
            return new URLClassLoader(new URL[]{targetDir.toURL()}, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static synchronized void findDir(File directory){
        File target = null;
        for(File fl: directory.getParentFile().listFiles()){
            if(fl.getName().equals(directory.getName() + ".pl") && fl.isFile())
                target = fl;
        }
        if(target == null)
            throw new RuntimeException(new FileNotFoundException(directory.getName() + ".pl"));
        directory.mkdir();
        compileDirectory(target, directory);
    }

    private static void compileDirectory(File target, File directory) {
        jp.ac.kobe_u.cs.prolog.compiler.Compiler compiler = new jp.ac.kobe_u.cs.prolog.compiler.Compiler();
        //Господин Банбара написал, свой компилятор таким образом, что путь к plcafe.jar обязательно
        //должен содржаться в системном classPath. Теперь вы знаете, что делать, если здесь выпала ошибка.
        if(!compiler.prologToJava(target.getAbsolutePath(), directory.getAbsolutePath())){
            for(File fl: directory.listFiles())
                fl.delete();
            directory.delete();
            throw new RuntimeException("Unable to compile");
        }
        List<String> files = new LinkedList<String>();
        files.add("-cp");
        files.add(new File(directory.getParentFile().getParentFile(), "plcafe.jar").getAbsolutePath());
        files.add("-d");
        files.add(directory.getAbsolutePath());
        for(File fl: directory.listFiles())
            if(fl.getName().endsWith(".java"))
                files.add(fl.getAbsolutePath());
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        int compilationResult = javac.run(null, null, System.err, files.toArray(new String[files.size()]));
        if(compilationResult != 0){
            for(File fl: directory.listFiles())
                fl.delete();
            directory.delete();
            throw new RuntimeException("Compilation failed");
        }
    }

    @Override
    public Object extractPrologData(Term term, Class valueClass) {
        if(valueClass == null || term.convertible(valueClass))
            return term.toJava();
        else
            throw new ClassCastException("Not convertable term: " + term + " to " + valueClass.getName());
    }

    @Override
    public Term[] prepareTerms(List<? extends Object> values) {
        Term[] result = new Term[values.size()];
        int i = 0;
        for(Object val : values){
            if(val instanceof String){
                String str = (String) val;
                if((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'")))
                    result[i++] = SymbolTerm.makeSymbol(str.substring(1, str.length() - 1));
                else
                    result[i++] = new VariableTerm();
            }else if(val instanceof Double || val instanceof Float)
                result[i++] = new DoubleTerm(((Number)val).doubleValue());
            else if(val instanceof Integer || val instanceof Short || val instanceof Byte || val instanceof Long)
                result[i++] = new IntegerTerm(((Number)val).intValue());
            else
                result[i++] = new JavaObjectTerm(val);
        }
        return result;
    }

    public boolean createPrologFile(String name, byte[] data, boolean canOverride) {
        try {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Illegal name: empty");
            }
            for (int i = 0; i < name.length(); i++) {
                if (Character.isWhitespace(name.charAt(i))) {
                    throw new IllegalArgumentException("Illegal name: whitespaces");
                }
            }
            if (!name.endsWith(".pl")) {
                throw new IllegalArgumentException("Illegal name: .pl");
            }
            File contanement;
            try {
                contanement = new File(Thread.currentThread().getContextClassLoader().getResource("org/muter/").toURI()).getParentFile().getParentFile();
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
            File root = new File(contanement, "pldir");
            File targetFile = new File(root, name);
            File targetDir = new File(root, name.substring(0, name.length() - 3));
            if (!root.exists())
                root.mkdir();
            boolean exists = targetFile.exists();
            if (exists && !canOverride || targetDir.exists()) //TODO locking on dir and dir override
                return false;
            if (!exists)
                targetFile.createNewFile();
            new FileOutputStream(targetFile).write(data);
            return true;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
