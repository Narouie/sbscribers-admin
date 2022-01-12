#FROM adoptopenjdk/openjdk11
#ARG JAR_FILE=./target/*.jar
#ADD ${JAR_FILE} subscribers-admin.jar
#VOLUME /tmp
#EXPOSE 8038
#ENTRYPOINT ["java", "-jar", "subscribers-admin.jar"]
FROM adoptopenjdk/openjdk11
ARG JAR_FILE=./target/*.jar
ADD ${JAR_FILE} /home/tahbaz/images/target/
VOLUME /tmp
EXPOSE 8038
ENTRYPOINT ["java", "-jar", "/home/tahbaz/images/target/subscribers-admin-0.0.1-SNAPSHOT.jar"]






















