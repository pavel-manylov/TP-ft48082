/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.ejb;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Exchanger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import jp.ac.kobe_u.cs.prolog.lang.JavaObjectTerm;

import jp.ac.kobe_u.cs.prolog.lang.PrologControl;
import jp.ac.kobe_u.cs.prolog.lang.Term;
import jp.ac.kobe_u.cs.prolog.lang.VariableTerm;
import org.muter.study.prolog.ejb.controller.BackListener;
import org.muter.study.prolog.ejb.controller.PrologPredicateProvider;
import org.muter.study.prolog.ejb.model.FlushEvent;
import org.muter.study.prolog.ejb.model.PrologEvent;
import org.muter.study.prolog.ejb.model.PrologRequest;
import org.muter.study.prolog.ejb.model.PrologResponse;
import org.muter.study.prolog.ejb.model.PrologResult;

/**
 *
 * @author muter
 */
@Stateful
public class PrologPerformBean implements PrologPerformService {
    private static PrologPredicateProvider _provider = PrologPredicateProvider.createInstance();
    private transient Thread _workingThread;
    private Worker _worker;
    private CallBackProcessor _processor;
    private ResultCollectior _resultCollector;
    private Exchanger<PrologEvent> _exchangeEvent = new Exchanger<PrologEvent>();

    public PrologEvent call(String prologProgramm, String prologPredicate, PrologRequest request) {
        _processor = new CallBackProcessor(request);
        List<Serializable> arguments = new LinkedList<Serializable>(request.getArguments());
        if(arguments.size() > 0 && arguments.get(arguments.size() - 1).equals("out"))
             arguments.set(arguments.size() - 1, _processor);
        Term[] args = _provider.prepareTerms(arguments);
        try{
            _worker = new Worker(_provider.getPrologConfig(prologProgramm, prologPredicate, args), request);
        }catch(RuntimeException ex){
            Logger.getLogger(PrologPerformBean.class.getName()).log(Level.SEVERE, "Incorrent file", ex);
            return new PrologResponse(request, "Файл не существует либо некорректен", ex);
        }
        _resultCollector = new ResultCollectior(args, request.getArguments().toArray(new Serializable[request.getArguments().size()]));
        _workingThread = new Thread(_worker);
        _workingThread.start();
        try {
            return _exchangeEvent.exchange(null);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public PrologEvent callAgain(PrologResponse response) {
        try {
            PrologEvent event = null;
            while(event == null) {
                event = _exchangeEvent.exchange(response);
                response = null;
            }
            return event;
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addPrologFile(String name, byte[] data) {
        _provider.createPrologFile(name, data, true);
    }

    public class Worker implements Runnable, Serializable {
        private PrologControl _control;
        private PrologRequest _request;

        public Worker(PrologControl prologControl, PrologRequest request){
            _control = prologControl;
            _request = request;
        }

        public void run() {
            try {
                for(boolean result = _control.call(); result; result = _control.redo())
                    _resultCollector.performCollection();
                _worker = null;
                PrologEvent ev = new PrologResponse(_request, null, _processor.flushWriter(), _control.in_success(), _resultCollector.flushResult());
                _exchangeEvent.exchange(ev);
            }catch(InterruptedException ex){
                    Logger.getLogger(PrologPerformBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                try{
                    _exchangeEvent.exchange(null);
                }catch(InterruptedException e){
                    Logger.getLogger(PrologPerformBean.class.getName()).log(Level.SEVERE, ex.getMessage(), e);
                }
            } 
        }

        private void stop() {
            _control.stop();
        }
    }

    public class CallBackProcessor implements BackListener {
        private List<PrologEvent> _callbacks = new ArrayList<PrologEvent>();
        private int _number;
        private StringWriter _writer = new StringWriter();

        private CallBackProcessor(PrologRequest request) {
            _callbacks .add(request);
            _number = _callbacks.size();
        }

        public void createRequest(String metadata, Serializable data, String className){
        }
        
        public Object callingBack(String metadata, Serializable data, String className) {
            if(_number >= _callbacks.size()){
                try{
                    PrologEvent event = null;
                    Class cls = className == null ? null : Class.forName(className);
                    PrologEvent request = new PrologRequest(metadata, cls, flushWriter(), _resultCollector.flushResult(), data);
                    while(event == null){
                        event = _exchangeEvent.exchange(request);
                        request = null;
                    }
                    _callbacks.add(event);
                }catch(Exception ex){
                    throw new RuntimeException(ex);
                }
            }
            return ((PrologResponse)_callbacks.get(_number++)).getValue();
        }

        public void write(Object data) {
            _writer.write(String.valueOf(data));
        }

        public void nl(){
           _writer.write("\n");
        }
        

        public Object read() {
            Object value = callingBack(null, null, null);
            return value;
        }

        public String flushWriter(){
            String result = _writer.toString();
            _writer.getBuffer().delete(0, result.length());
            return result;
        }

        public void flush() {
            try {
                PrologEvent request = new FlushEvent(null, flushWriter(), _resultCollector.flushResult());
                _exchangeEvent.exchange(request);
                _exchangeEvent.exchange(null);
            } catch (InterruptedException ex) {
                Logger.getLogger(PrologPerformBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private static class ResultCollectior implements Serializable {
        private Map<String, Term> _termsToCheck;
        private transient List<PrologResult> _result = new LinkedList<PrologResult>();

        private ResultCollectior(Term[] terms, Serializable[] arguments) {
            _termsToCheck = new LinkedHashMap<String, Term>();
            for(int i = 0; i < terms.length; i++)
                if(terms[i] instanceof VariableTerm)
                    _termsToCheck.put((String)arguments[i], terms[i]);
        }

        public PrologResult performCollection() {
            Map<String, Serializable> values = new LinkedHashMap<String, Serializable>(_termsToCheck.size());
            for(Map.Entry<String, Term> term: _termsToCheck.entrySet())
                values.put(term.getKey(), (Serializable) _provider.extractPrologData(term.getValue(), null));
            PrologResult result = new PrologResult(values);
            _result.add(result);
            return result;
        }

        public List<PrologResult> flushResult(){
            List<PrologResult> result = _result;
            _result = new LinkedList<PrologResult>();
            return result;
        }

    }
   

    @PrePassivate
    public void shutdown(){
        if(_workingThread != null){
            _workingThread.interrupt();
            _workingThread = null;
            _worker.stop();
        }
    }

    @PostActivate
    public void setupAgain(){
        if(_worker != null){
            _workingThread = new Thread(_worker);
            _workingThread.start();
        }
    }

}
