package com.kidscademy.process;

import java.io.File;

import js.lang.BugError;

public final class Command {
    private Command() {
    }

    public static String format(String format, Object... args) {
	// 0: NONE
	// 1: APPEND
	// 2: WAIT_OPEN_BRACE
	// 3: VARIABLE
	int state = 1;

	StringBuilder valueBuilder = new StringBuilder();
	for (int charIndex = 0, argIndex = 0; charIndex < format.length(); ++charIndex) {
	    char c = format.charAt(charIndex);
	    switch (state) {
	    case 1:
		if (c == '$') {
		    state = 2;
		    break;
		}
		valueBuilder.append(c);
		break;

	    case 2:
		if (c != '{') {
		    throw new BugError("Invalid command format. Missing open brace after variable mark ($).");
		}
		state = 3;

	    case 3:
		if (c == '}') {
		    if (argIndex == args.length) {
			throw new BugError("Not enough arguments provided for given command format.");
		    }
		    Object arg = args[argIndex];
		    // special handling for files argument; always uses absolute path
		    valueBuilder.append(arg instanceof File ? ((File) arg).getAbsolutePath() : arg.toString());
		    ++argIndex;
		    state = 1;
		}
		break;
	    }
	}
	return valueBuilder.toString();
    }
}
