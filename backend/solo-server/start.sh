#!/bin/bash
# Solo Life OS - 生产环境启动脚本（JVM 调优版）
#
# 用法:
#   ./start.sh              # 前台运行
#   ./start.sh --daemon     # 后台运行
#
# JVM 调优说明:
#   -Xms128m -Xmx256m     堆内存 128-256MB（MVP 阶段足够，按实际调整）
#   -XX:MaxMetaspaceSize=128m  限制 Metaspace 防止类加载泄漏
#   -XX:+UseSerialGC      单核/小内存最优 GC（2 核以上可改 -XX:+UseG1GC）
#   -Xss256k              线程栈 256k（默认 1M，Spring Boot ~200 线程省 ~150MB）
#   -XX:+HeapDumpOnOutOfMemoryError  OOM 时自动 dump
#
# 预期效果: RSS ~150-200MB（默认配置约 300-500MB）

JAR_PATH="target/solo-server-0.0.1-SNAPSHOT.jar"
PROFILE="${SPRING_PROFILES_ACTIVE:-dev}"

JVM_OPTS="-Xms128m -Xmx256m \
  -XX:MaxMetaspaceSize=128m \
  -XX:+UseSerialGC \
  -Xss256k \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/solo-server-heapdump.hprof \
  -Dspring.profiles.active=${PROFILE}"

if [ ! -f "$JAR_PATH" ]; then
  echo "JAR not found, building..."
  mvn package -DskipTests -q
fi

if [ "$1" = "--daemon" ]; then
  nohup java $JVM_OPTS -jar "$JAR_PATH" > /tmp/solo-server.log 2>&1 &
  echo "Started in background, PID: $!  Log: /tmp/solo-server.log"
else
  exec java $JVM_OPTS -jar "$JAR_PATH"
fi
