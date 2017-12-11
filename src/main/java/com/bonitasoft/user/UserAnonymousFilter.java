package com.bonitasoft.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.login.HttpServletRequestAccessor;
import org.bonitasoft.console.common.server.login.LoginFailedException;
import org.bonitasoft.console.common.server.utils.PermissionsBuilder;
import org.bonitasoft.console.common.server.utils.PermissionsBuilderAccessor;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.util.APITypeManager;
import org.bonitasoft.web.rest.model.user.User;

public class UserAnonymousFilter implements Filter {

    private final String currentUserName = "walter.bates";
    private final String bannerHeader = "--------------------- FiltreUserAnonymous V1.5: ";

    public Logger logger = Logger.getLogger(UserAnonymousFilter.class.getName());

    FilterConfig filterConfig = null;
    String userName = null;
    String userPassword = null;
    boolean automaticAssignTask=false;
    

    public void init(final FilterConfig filterConfig) throws ServletException {
        userName = filterConfig.getInitParameter("username");
        userPassword = filterConfig.getInitParameter("userpassword");
        String automaticAssignSt = filterConfig.getInitParameter("automaticAssignTask");
        if (automaticAssignSt!=null && "TRUE".equalsIgnoreCase(automaticAssignSt))
        	automaticAssignTask=true;
        logger.info("--------------------- filtre UserAnonymous userName[" + userName + "] automaticAssignTask["+automaticAssignTask+"]");
        this.filterConfig = filterConfig;
    }

    /**
     * Each URL come
     */
    public void doFilter(final ServletRequest request,
            final ServletResponse servletResponse, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        final String url = httpRequest.getRequestURL().toString();
        // logger.info("--------------------- filtre V1.4  URL=[" + url + "] userName[" + userName + "]");

        final HttpServletRequestAccessor requestAccessor = new HttpServletRequestAccessor(httpRequest);
        if (requestAccessor != null) {
            final APISession apiSession = requestAccessor.getApiSession();
            if (apiSession != null) {
                // logger.info(bannerHeader+"Already connected");
                chain.doFilter(httpRequest, servletResponse);
                return;
            }

        }

        // http://localhost:8080/bonita/API/form/mapping?c=10&p=0&f=processDefinitionId%3D9090055435485955288&f=type%3DPROCESS_START&useranonymous=true
        final String action = request.getParameter("useranonymous");

        // Capture an autologin URL http://localhost:8080/bonita/portal/resource/taskInstance/ProgramManagerPool/1.2/Enter%20Program%20Manager/content/?id=687015&locale=en&ui=form&autologin=ProgramManagerPool--1.2
        final String autologin = request.getParameter("autologin");

        boolean doLog = false;

        if (action != null && url != null && action.indexOf("true") != -1) {
            doLog = true;
        }
        if (autologin != null && autologin.trim().length() > 0) {
            doLog = true;
        }

        if (!doLog) {
            logger.info(bannerHeader + " URL=[" + url + "] action[" + action + "] autologin[" + autologin + "] noLogin");
        } else
        {
            logger.info(bannerHeader + " URL=[" + url + "] action[" + action + "] autologin[" + autologin + "] log with user[" + userName + "] url=["
                    + httpRequest.getProtocol() + ":" + httpRequest.getLocalName() + ":"
                    + httpRequest.getLocalPort() + "]");

            // let connect on this server
            final Map<String, String> map = new HashMap<String, String>();
            APITypeManager.setAPITypeAndParams(ApiAccessType.LOCAL, map);

            try {
                final LoginAPI loginAPI = TenantAPIAccessor.getLoginAPI();

                // log in to the tenant to create a session
                final APISession apiSession = loginAPI.login(userName, userPassword);
                // set the session in the TomcatSession
                logger.info("Connection success with[" + userName + "]");

                final HttpSession httpSession = httpRequest.getSession();
                final User user = new User(userName, Locale.ENGLISH.getDisplayName());
                final PermissionsBuilder permissionsBuilder = PermissionsBuilderAccessor.createPermissionBuilder(apiSession);
                final Set<String> permissions = permissionsBuilder.getPermissions();
                SessionUtil.sessionLogin(user, apiSession, permissions, httpSession);

            } catch (final BonitaHomeNotSetException e) {
                logger.severe(bannerHeader + "NoBonitaHome setted");
            } catch (final ServerAPIException e) {
                logger.severe(bannerHeader + "ServerAPIException [" + e + "]");
            } catch (final UnknownAPITypeException e) {
                logger.severe(bannerHeader + "UnknownAPITypeException [" + e + "]");
            } catch (final LoginException e) {
                logger.severe(bannerHeader + "User[" + userName + "] not referenced in Bonita :" + e.toString());
            } catch (final LoginFailedException e) {
                logger.severe(bannerHeader + "User[" + userName + "] not referenced in Bonita:" + e.toString());
            }
        }
        
        // it is a TaskAccess ? 
        if (automaticAssignTask && url.indexOf("portal/resource/taskInstance")!=-1)
        {
        	// do an automatic assignement
        	String taskId=null; 
        	try
        	{
        	   taskId= request.getParameter("id");
        	   final APISession apiSession = requestAccessor.getApiSession();
        	   final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(apiSession);
        	   processAPI.assignUserTask(Long.valueOf( taskId ), apiSession.getUserId()); 
        	   logger.info("Assignement of taskId["+taskId+"] done with success to userId[" +  apiSession.getUserId() + "]");

        	}
        	catch (final Exception e) {
        		logger.severe(bannerHeader + "Can't realize the automatic assignement ["+taskId+"] "+e.toString());
        	}
        }
        
        // else chain
        chain.doFilter(httpRequest, servletResponse);
        return;

    }

    public void destroy() {

    }

}
