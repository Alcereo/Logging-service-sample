FROM java
ADD ./build/libs/test-loggen-service.jar /test-loggen-service.jar
CMD java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1617 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar test-loggen-service.jar --spring.profiles.active=prod