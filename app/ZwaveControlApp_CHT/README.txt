1. update include folder 
	include ':app', ':broker', ':netty_parser', ':parser_commons'

2. Add below commands to settins.gradle

	project(':broker').projectDir = new File(settingsDir, 'moquette/broker/')
	project(':netty_parser').projectDir = new File(settingsDir, 'moquette/netty_parser/')
	project(':parser_commons').projectDir = new File(settingsDir, 'moquette/parser_commons/')

3. Add these dependencies to the gradle

    dependencies{
    	compile 'io.moquette:moquette-netty-parser:0.8.1'
    	compile 'io.moquette:moquette-broker:0.8.1'
    	compile 'io.moquette:moquette-parser-commons:0.8.1'
	}

4. NDK version : v14


