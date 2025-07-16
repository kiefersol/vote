FROM tomcat:10

COPY ./build/libs/vote.war /usr/local/tomcat/webapps/vote.war

COPY ./setenv.sh /usr/local/tomcat/bin/setenv.sh
RUN chmod +x /usr/local/tomcat/bin/setenv.sh

EXPOSE 8080
