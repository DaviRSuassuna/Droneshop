FROM quay.io/keycloak/keycloak:26.6.4 AS keycloak
FROM docker.io/eclipse-temurin:21-jdk AS compilacao
COPY --from=keycloak /opt/keycloak/lib/lib/main /keycloak
COPY provider /provider
RUN mkdir /classes && javac -cp '/keycloak/*' -d /classes /provider/OtpAeria.java && \
    jar --create --file /otp-aeria.jar -C /classes . -C /provider META-INF

FROM keycloak
COPY --from=compilacao /otp-aeria.jar /opt/keycloak/providers/
