set home=%~dp0..
set cp=%home%/target/classes
set logprop=%home%/etc/logging.properties
set cfg=%home%/etc/ErsatzspielerCheck.properties
java -cp "%cp%" -Djava.util.logging.config.file=%logprop% bwbv.ersatzspielercheck.gui.Application %cfg%

