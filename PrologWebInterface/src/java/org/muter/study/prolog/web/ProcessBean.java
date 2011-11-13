/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.web;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.muter.study.prolog.ejb.PrologPerformService;
import org.muter.study.prolog.ejb.model.FlushEvent;
import org.muter.study.prolog.ejb.model.PrologEvent;
import org.muter.study.prolog.ejb.model.PrologRequest;
import org.muter.study.prolog.ejb.model.PrologResponse;
import org.muter.study.prolog.ejb.model.PrologResult;

/**
 * Бин, обрабатываюший запросы сервлету.
 * @author muter
 */
public class ProcessBean {
    private PrologPerformService _service;
    private List<PrologResult> _accuaredResult = new LinkedList<PrologResult>();
    private ProcessState _state = ProcessState.CALL;
    private PrologRequest _lastRequest;

    public ProcessState processQuery(String query, XMLStreamWriter writer) throws XMLStreamException {
        Serializable[] arguments;
        if(_state.equals(ProcessState.CALL)){
            arguments = parseArguments(query);
            if(arguments == null || arguments.length < 2){
                printUsage(writer);
                return _state;
            }
        }else if (_state.equals(ProcessState.RECALL)) {
            Serializable ser = makeArgument(query);
            if(ser == null)
                ser = query;
            arguments = new Serializable[]{ser};
        }else
            arguments = new Serializable[1];
        PrologEvent event;
        switch(_state){
            case CALL:
                event = performCall(arguments);
                break;
            case RECALL:
            case IMMIDIATE:
                event = performRecall(arguments);
                break;
            default:
                throw new IllegalStateException(_state.name());
        }
        printEvent(writer, event);
        if(event instanceof PrologRequest){
            _state = ProcessState.RECALL;
        }else if(event instanceof PrologResponse){
            PrologResponse rp = (PrologResponse) event;
            printLines(writer, "" + rp.getValue());
            printResult(writer);
            _accuaredResult.clear();
            _state = ProcessState.CALL;
        }else
            _state = ProcessState.IMMIDIATE;
        return _state;
    }

    private Serializable[] parseArguments(String query) {
        int pos = 0, start = -1;
        boolean quoted = false, backed = false;
        List<Serializable> arguments = new LinkedList<Serializable>();
        for(; pos < query.length(); pos++){
            if(!quoted && Character.isWhitespace(query.charAt(pos))){
                if(start >= 0)
                    if(arguments.size() < 2)
                        arguments.add(query.substring(start, pos));
                    else{
                        Serializable ser = makeArgument(query.substring(start, pos));
                        if(ser == null)
                            return null;
                        arguments.add(ser);
                    }
                start = -1;
            }else if(query.charAt(pos) == '\'' || query.charAt(pos) == '"'){
                if(arguments.size() < 2)
                    return null;
                if(!backed){
                    quoted = !quoted;
                    if(quoted)
                        if(start < 0)
                            start = pos;
                        else
                            return null;
                    else{
                        String str = query.substring(start, pos + 1);
                        str = str.replace("\\\\", "\\").replace("\\\"", "\"").replace("\\n", "\n").replace("\\t", "\t");
                        arguments.add(str);
                        start = -1;
                    }
                }
            }else if(query.charAt(pos) == '\\'){
                if(arguments.size() < 2 || !quoted)
                    return null;
                if(!backed){
                    backed = true;
                    continue;
                }
            }else if(start < 0)
                start = pos;
            backed = false;
        }
        if(start >= 0){
            if(arguments.size() < 2)
                arguments.add(query.substring(start, pos));
            else{
                Serializable ser = makeArgument(query.substring(start, pos));
                if(ser == null)
                    return null;
                arguments.add(ser);
            }
        }
        return arguments.toArray(new Serializable[arguments.size()]);
    }

    private Serializable makeArgument(String argument) {
        if(argument.matches("[+-]?[0-9]+"))
            return Integer.parseInt(argument);
        if(argument.matches("[+-]?[0-9]+(?:\\.[0-9]+)?"))
            return Double.parseDouble(argument);
        else if(argument.matches("[a-zA-Z_][a-zA-Z_0-9]*"))
            return argument;
        else
            return null;
    }

    private PrologEvent performCall(Serializable[] arguments){
        Serializable[] args = new Serializable[arguments.length - 2];
        for(int i = 2; i < arguments.length; i++)
            args[i - 2] = arguments[i].equals("_") ? null : arguments[i];
        return getService().call((String)arguments[0], (String)arguments[1], new PrologRequest("", null, args));
    }

    private PrologEvent performRecall(Serializable[] arguments){
        return getService().callAgain(new PrologResponse(_lastRequest, null, arguments[0]));
    }

    private PrologPerformService getService() {
        if(_service == null){
            try{
                _service = (PrologPerformService)new InitialContext().lookup(PrologPerformService.class.getName());
            }catch(NamingException ex){
                throw new RuntimeException(ex);
            }
        }
        return _service;
    }

    private void printEvent(XMLStreamWriter writer, PrologEvent event) throws XMLStreamException {
        writer.writeStartElement("message");
        writer.writeAttribute("type", "info");
        if(event.getFlusedData() != null && !event.getFlusedData().isEmpty())
            printLines(writer, event.getFlusedData());
        if(event.getMetainfo() != null && !event.getMetainfo().isEmpty())
            printLines(writer, event.getMetainfo());
        writer.writeEndElement();
        _accuaredResult.addAll(event.getResultList());
    }

    private void printUsage(XMLStreamWriter writer) throws XMLStreamException {
        printLines(writer, "Использование:\n<имя файла без расширения> <имя предиката> [<аргументы>]\n"
                + "аргументы разбираются следующим образом:\nаргументы разбираются следующим образом:\n"
                + "строки должны быть заключены в кавычки, внутри кавычка экранируется обратным слешем;\n"
                + "цифры разбираются как числа;\nнеаключенные в кавычки последовательности интерпретируются как имена "
                + "переменных для вывода\n"
                + "переменная out разворачивает в BackListener\n"
                + "Остальное вызовет это сообщение");
    }

    private void printLines(XMLStreamWriter writer, String line) throws XMLStreamException {
        if(line == null || (line = line.trim()).isEmpty())
            return;
        String[] lines = line.split("\n");
        writer.writeStartElement("message");
        writer.writeAttribute("type", "info");
        for(String str: lines){
            writer.writeStartElement("line");
            writer.writeCharacters(str);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private void printResult(XMLStreamWriter writer) throws XMLStreamException{
        for(PrologResult result: _accuaredResult){
            writer.writeStartElement("message");
            for(Map.Entry<String, Serializable> data: result.getValues().entrySet()){
                writer.writeStartElement("line");
                writer.writeCharacters(data.getKey() + "=" + data.getValue());
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }


    public static enum ProcessState {
        CALL, RECALL, IMMIDIATE
    }
}
