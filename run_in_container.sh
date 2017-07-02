#!/usr/bin/env bash

docker build --rm -t test_log_service . &&/
docker run --rm \
 --net host \
 --name test_log_service \
 --log-driver=fluentd \
 test_log_service