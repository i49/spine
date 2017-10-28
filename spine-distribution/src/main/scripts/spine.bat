@echo off
java -p lib --add-modules java.sql --add-opens java.desktop/java.beans=snakeyaml -m spine/io.github.i49.spine.browser.Browser %*
