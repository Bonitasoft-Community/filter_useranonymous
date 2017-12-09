# filter_useranonymous
User can access a URL to create a case/execute a task with no login page


1. Principle
-------------
The principle to build a SSO is to build a Filter on Tomcat or on Jboss.
Then, when the URL arrive on the Bonita Portal, the Bonita user must be known.
Bonita Portal check if the user is connected or not, by checking some java object on the session in the Web Manager. If not, the login page is presented, and on login, theses objects are created.
 
 To build a SSO connection, the component must create these object before the URL arrived on the Bonita Portal. To do that, a component, on the Web Server, must be place before the Web Application : this is a Filter.
 
2. Installation
---------------------------
	2.1 copy the  UserAnonymous-1.0.jar to <TOMCAT>/webapps/bonita/WEB-INF/lib
	2.2 register in the web.xml
	
	 register the filter in Tomcat
	To register the filter in Tomcat, edit the file <TOMCAT>/webapps/bonita/WEB-INF/web.xml

	<filter>
		<filter-name>UserAnonymous</filter-name>
		<filter-class>com.bonitasoft.user.UserAnonymousFilter</filter-class>		
		<init-param>
      		<param-name>username</param-name>
      		<param-value>Walter.Bates</param-value>
    	</init-param>   
		<init-param>
      		<param-name>userpassword</param-name>
      		<param-value>bpm</param-value>
    	</init-param>   
	</filter>

	<filter-mapping>
		<filter-name>UserAnonymous</filter-name>
		<url-pattern>/API/*</url-pattern>
	</filter-mapping>
	<!-- Autologin -->
	<filter-mapping>
		<filter-name>UserAnonymous</filter-name>
		<url-pattern>/portal/resource/taskInstance/*</url-pattern>
	</filter-mapping>
	<filter-mapping>
		<filter-name>UserAnonymous</filter-name>
		<url-pattern>/portal/resource/process/*</url-pattern>
	</filter-mapping>
	
4. Use it 
-----------
add "useranonymous=true" in the URL.

Then, to access the process instantiation page, get the ProcessName, ProcessVersion, processID and the URL is
 
	http://localhost:45036/bonita/portal/resource/process/MeteorContract/1.0/content/?id=7383029389091436987&locale=en&mode=app&useranonymous=true

	


	
