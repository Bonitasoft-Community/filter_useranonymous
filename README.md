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
    	<init-param>
      		<param-name>automaticAssignTask</param-name>
      		<param-value>true</param-value>
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


where :
username is the user used to connect the anonymous URL. if the person access after to the Bonita Portal, he's connected with this user
userpassword is the password used
automaticAssignTask is optionnel. When set to true, if a Task is accessed, then an automatic assignement is perform. if the assignement failed, the Bonita Default Policies is used : form is displayed but user will get an error (this is the behavior in 7.6, may be change in a next release)

4. Deactivate the CSRF
----------------------
In 7.6, deactivate the CSRF via the procedure describe here

https://documentation.bonitasoft.com/bonita/7.6/csrf-security
	
	
5. Use it 
-----------
add "useranonymous=true" in the URL.
See https://documentation.bonitasoft.com/bonita/7.6/bonita-bpm-portal-urls
 
  
Then, to access the process instantiation page, get the ProcessName, ProcessVersion, processID and the URL is
 	
http://localhost:8080/bonita/portal/resource/process/mySimpleProcess/1.0/content/?id=6298446565280644531&useranonymous=true
	
To access a task
http://localhost:8080/bonita/portal/resource/taskInstance/mySimpleProcess/1.0/SimpleTask/content/?id=2&useranonymous=true

Attention: the task must be assigned first to the user. Use the automaticAssignTask policy. Else, he can access the form, but on submit, he's get an error


Nota
You can display the log information by adding this properties in the logging.properties file

com.bonitasoft.user.level=INFO
