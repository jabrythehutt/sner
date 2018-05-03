FROM circleci/openjdk:8-jdk-node
RUN mkdir -p /home/circleci/sner
WORKDIR /home/circleci/sner
ADD --chown=circleci . /home/circleci/sner
ENV AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
ENV AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
RUN mvn dependency:resolve
RUN npm install
CMD mvn clean install