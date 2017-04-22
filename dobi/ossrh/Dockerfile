FROM maven:3.5.0-jdk-8-alpine

RUN apk add -U gnupg1
ADD signing.key .

RUN gpg --batch --yes --import signing.key
RUN rm signing.key
