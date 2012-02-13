@echo off
@set GRADLE_OPTS=-Dgroovy.source.encoding=UTF-8 -Dfile.encoding=UTF-8
cmd /c gradlew.bat :docs:docs
