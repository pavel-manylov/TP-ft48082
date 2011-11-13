/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.muter.study.prolog.web.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.stream.XMLStreamWriter;
import org.muter.study.prolog.web.ProcessBean;

/**
 *
 * @author muter
 */
public class PrologCallServlet extends HttpServlet {
    private static final String SERVICE_KEY = "prologServiceKey";
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");        
        try {
            XMLStreamWriter out = javax.xml.stream.XMLOutputFactory.newInstance().createXMLStreamWriter(response.getOutputStream(), "UTF-8");
            out.writeStartDocument();
            out.writeStartElement("messageExchange");
            String query = request.getParameter("query");
            response.setHeader("state", getBean(request.getSession()).processQuery(query, out).name());
            out.writeEndElement();
            out.writeEndDocument();
            out.flush();
            out.close();
        }catch(Exception ex){
            response.setHeader("state", "CALL");
            Logger.getLogger(PrologCallServlet.class.getName()).log(Level.SEVERE, "Uncaught exception in thread", ex);
            dropBean(request.getSession());
        } finally {
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private ProcessBean getBean(HttpSession session){
        ProcessBean bean = (ProcessBean) session.getAttribute(SERVICE_KEY);
        if(bean == null){
            bean = new ProcessBean();
            session.setAttribute(SERVICE_KEY, bean);
        }
        return bean;
    }

    private void dropBean(HttpSession session){
        session.setAttribute(SERVICE_KEY, null);
    }

}
