#!/usr/bin/env bash

docker run --rm -p 3000:3000 \
--name grafana \
grafana/grafana