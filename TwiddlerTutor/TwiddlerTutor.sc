TwiddlerConfig {
	var <config;
	var <specialChars;
	var <typeCodes;
	var <>useShift = true;

	*new{ |config|
		^super.new.init( config );
	}

	init{ |cfg|
		config = TabFileReader.read( cfg, true );
		typeCodes = IdentityDictionary.new;

		specialChars = [
			[ "<Space>", $ ],
			[ "<Return>", $\r ],
			[ "<Tab>", $\t ],
			// [ "<Backspace>", 8.asAscii ],
			// [ "<LeftArrow>", 0.asAscii ],
			// [ "<RightArrow>", 0.asAscii ],
			// "<UpArrow>",
			// "<DownArrow>",
			// "<Left GUI><Backspace></Left GUI>",
			// "<Left Ctrl>c</Left Ctrl>",
			// "<Left Ctrl>v</Left Ctrl>",
			// "<Left Ctrl>x</Left Ctrl>",
			// "<Left Ctrl>z</Left Ctrl>",
			// "<Left Ctrl>z</Left Ctrl>",
			// "<CapsLock>",
			// "<Home>",
			// "<End>",
			// "<PageUp>",
			// "<PageDown>",
			// "<Escape>",
			// "<Insert>",
			// "<Delete>",
		];

		config.do{ |it|
			var char = it.last.first;
			var size = it.last.size;
			if ( size == 1 ){
				typeCodes.put( char, it[2] );
			}{
				specialChars.do{ |jt|
					if ( jt[0].compare( it.last ) == 0 ){
						typeCodes.put( jt[1], it[2] );
					};
				};
			}
		};
		typeCodes.put( $\n, typeCodes.at( $\r ) );
	}
}

TwiddlerTutor {

	var <>config;

	var <typingIndex = 0;
	var <evaluatedLines;

	var rehearseFile;

	var <typedLines;

	var <linesFromFile;
	var <currentLineFromFileIndex;
	var <linesExecuted;
	var <currentLineFromFile;
	var <currentLineTyped;

	var <codeFunc;

	// gui
	var window;
	var <lineToTypeW, <typed;
	var spacer1;
	var nextCharW, nextCharCodeW, buttonView, buttons;
	var idsW;
	var lastCharW;
	var keyUpAction;

	var nextToType, typing;

	// actions
	var <>typedRightAction;
	var <>typedWrongAction;
	var <>typedBackspaceAction;



	*new{ |config|
		^super.new.init( config );
	}

	init{ |cfg|
		config = cfg;
		typingIndex = 0;
		this.reset;
		this.makeWindow;
		this.setTypingAction;
		this.reset;
	}

	reset{
		currentLineFromFileIndex = -1;
		linesExecuted = 0;
		currentLineTyped = "";
		evaluatedLines = [];
		typedLines = [];
		linesFromFile = [];
	}

	makeWindow{
		// gui
		window = Window.new("LivecodeTwiddlerTutor", Rect( 0, 0, 800, 600) );
		window.front;
		window.addFlowLayout;

		lineToTypeW = StaticText.new( window, Rect( 0,0, 800, 200 ) ).background_( Color.white );
		lineToTypeW.font_( Font.new( "Courier", 24) );

		typed = StaticText.new( window, Rect( 0,0, 800, 200 ) ).background_( Color.white );
		typed.font_( Font.new( "Courier", 24) );

		spacer1 = StaticText.new( window, Rect( 0, 0, 50, 80 ) );

		nextCharW = StaticText.new( window, Rect( 0, 0, 150, 80 ) );
		nextCharW.font_( Font.new( "Courier", 64) );
		nextCharW.background_( Color.white ).align_( \center );
		nextCharW.string_( "H" );

		nextCharCodeW = StaticText.new( window, Rect( 0, 0, 200, 80 ) );
		nextCharCodeW.font_( Font.new( "Courier", 64) );
		nextCharCodeW.background_( Color.white ).align_( \center );
		nextCharCodeW.string_( "00ML" );

		buttonView = CompositeView.new( window, Rect( 0,0, 4*26 + 5, 3*26 + 5 ) );
		buttons = 3.collect{ |i| 4.collect{ |j| Button.new( buttonView, Rect( i*20+2, j*20+2, 21, 21 ) ).states_( [[ "", Color.black, Color.white], ["",Color.white, Color.black]] ).canFocus_( false ); } };

		lastCharW = StaticText.new( window, Rect( 0, 0, 150, 40 ) );
		lastCharW.font_( Font.new( "Courier", 24) );
		lastCharW.background_( Color.yellow(0.8) ).align_( \center );
		lastCharW.string_( "H" );

		idsW = StaticText.new( window, Rect( 0, 0, 100, 40 ) );
		idsW.font_( Font.new( "Courier", 16) );
		idsW.background_( Color.yellow(0.8) ).align_( \center );
		idsW.string_( "0:0:0:0" );

		typing = TextField.new( window, Rect( 0,0, 800, 80 ) ).background_( Color.white );
		typing.font_( Font.new( "Courier", 24) );

		// ~historyV = StaticText.new( window, Rect( 0,0, 800, 240 ) ).background_( Color.grey( 0.9 ) );
	}

	setTypingAction {
		if ( keyUpAction.notNil ){ typing.removeAction( keyUpAction, \keyUpAction ); };
		keyUpAction = { arg field, char, mods;
			var lastTyped = field.string.last;
			var codeResult;
			"TYPING ACTION: ".post; lastTyped.postln;
			if ( char == 8.asAscii ){ // backspace
				"typing action backspace".postln;
				lastCharW.string_( "<--" );
				this.typedBackspace;
				this.setStringLineTyped;
				this.checkCharacter( char ); // was this the right character?
				this.updateNextChar;
			}{
				if ( char != 0.asAscii ){
					[ mods, char, lastTyped ].postcs;
					// process character other than backspace
					if ( char == $\r ){
						lastTyped = $\r;
					};
					if ( char == $\n ){
						lastTyped = $\r;
					};
					if ( char == $\t ){
						lastTyped = $\t;
						typing.string_( typing.string.add( lastTyped ) );
					};
					if ( lastTyped.isNil ){
						lastTyped = char;
					};
					lastCharW.string_( lastTyped.asCompileString );

					this.checkCharacter( lastTyped ); // was this the right character?
					// this.characterTyped( lastTyped );
					if( char == $\r ){ // enter
						currentLineTyped = typing.string;

						if ( currentLineTyped.size > 0 ){
							"from index: ".post; currentLineFromFileIndex.postln;
							typedLines = typedLines.add( [ currentLineFromFileIndex, currentLineTyped ] );
						};
						typing.string = "";
						currentLineTyped = "";
						// char = $\n;

						this.setStringLineTyped;

						if ( mods == 262144 ){ // ctrl+enter
							"~~~ ctrl+enter".postln;
							Routine({
								typed.background_( Color.yellow );
								codeResult = this.evaluateTyped;
								0.2.wait;
								if ( codeResult.not ){
									typed.background_( Color.red );
									0.2.wait;
								}{
									typed.background_( Color.green );
									0.2.wait;
									typed.string_( "" );
									this.readNextLine;
									this.setStringLineTyped;
									this.updateNextChar;
								};
								typed.background_( Color.white );
							}).play(AppClock);

						}{
							// just enter
							"~~~ enter".postln;
							this.readNextLine;
							this.setStringLineTyped;
							this.updateNextChar;
						};
					}{
						"~~~ other char".postln;
						currentLineTyped = typing.string;
						this.setStringLineTyped;
						this.updateNextChar;
					}
				}
			}
		};

		typing.addAction( keyUpAction, \keyUpAction );
	}

	findMatchingLine{
		"--- find matching line ---".postln;
		"currentTyped".post; currentLineTyped.postln;

	}

	typedBackspace {
		var prevTyped = currentLineTyped;
		"==TYPING backpace: ".postln; currentLineTyped.postcs;
		"typed lines ".post; typedLines.size.postln; typedLines.postcs;
		// only need to do something when going back a line...
		if ( currentLineTyped.size == 0 ){
			// get previous line
			/*
			if ( typedLines.size == 0 ){
				currentLineTyped = "";
				currentLineFromFileIndex = linesExecuted;
				currentLineFromFile = linesFromFile[ linesExecuted ];
				"reset to lines executed".postln;
			}
			*/
			if ( typedLines.size != 0 ){ // otherwise nothing to backspace to
				// currentLineFromFileIndex = currentLineFromFileIndex - 2;
				// "index ".post; currentLineFromFileIndex.postln;
				/*
				if ( currentLineFromFileIndex <= linesExecuted ){
					"reset to lines executed".postln;

					currentLineFromFileIndex = linesExecuted;
					currentLineFromFile = linesFromFile[ linesExecuted ];

					"index ".post; currentLineFromFileIndex.postln;
					currentLineFromFile.postln;

					typedLines = []; // reset
					currentLineTyped = "";
				}{*/
				"go to previous line".postln;
				currentLineFromFileIndex = typedLines.last[0];
				currentLineFromFile = linesFromFile[ currentLineFromFileIndex ];
				currentLineTyped = typedLines.last[1];
				// increase it again as it always points to the NEXT line: // ?
				// currentLineFromFileIndex = currentLineFromFileIndex + 1;
				"index ".post; currentLineFromFileIndex.postln;
				currentLineFromFile.postln;
				typedLines = typedLines.drop(-1); // drop the last of the typed lines;
			};
			typing.string_( currentLineTyped );
		}{
			currentLineTyped = typing.string;
		};
	}

	/*
	characterTyped { |char|
		var oldString = typed.string;
		currentLineTyped = typing.string;
		"==TYPING: ".postln; currentLineTyped.postcs;
		// "==TYPED: ".postln; oldString.postcs;
		if ( char != 8.asAscii ){ // backspace
			if( char == $\r ){
				"<newline>".postln;
				if ( currentLineTyped.size > 0 ){
					typedLines = typedLines.add( currentLineTyped );
					// typedLines.postcs;
				};
				typing.string = "";
				currentLineTyped = "";
				char = $\n;
			};
			// if ( typed.string.size == 0 and: (char==$\n) ){
			// 	"typed size = 0, not adding \n".postln;
			// }{
			// 	typed.string = ( oldString ++ char );
			// };
		};
		this.setStringLineTyped;
		// "==TYPED-2: ".postln; oldString.postcs;
	}*/

	checkCharacter{ |char|
		if ( char == nextToType ){
			Routine({
				nextCharW.background_( Color.green );
				typedRightAction.value( char );
				0.2.wait;
				nextCharW.background_( Color.white );
			}).play( AppClock );
		}{
			Routine({
				if( char == 8.asAscii ){
					nextCharW.background_( Color.yellow );
					typedBackspaceAction.value;
				}{
					nextCharW.background_( Color.red );
					typedWrongAction.value( char );
				};
				0.2.wait;
				nextCharW.background_( Color.white );
			}).play( AppClock );
		};
	}

	findNextChar{
		var foundIndex;
		var lastWord;
		var foundChar;
		"CURRENT line:\t".post; currentLineTyped.postcs;
		"from file line:\t".post; currentLineFromFile.postcs;
		if ( currentLineTyped.notNil ){
			// lastWord = currentLineTyped.split($ ).last;
			// "LAST word: ".post; lastWord.postcs;
			// if ( lastWord.size == 0 ){
			lastWord = currentLineTyped;
			"LAST word: ".post; lastWord.postcs;
			// };
			if ( currentLineFromFile.notNil ){
				foundIndex = lastWord.size;
				// foundIndex = currentLineFromFile.findBackwards( lastWord );
				// foundIndex.postln;
				// if ( foundIndex.notNil ){
				// foundIndex = foundIndex + lastWord.size;
				// foundIndex.postln;
			// }{
				// foundIndex = 0;
				// foundIndex.postln;
			// };
				typingIndex = foundIndex;
				foundIndex.postln;
				if ( foundIndex == currentLineFromFile.size ){
					foundChar = $\r;
					foundChar.postcs;
					^foundChar;
				};
				foundChar = currentLineFromFile[typingIndex];
				foundChar.postcs;
				^foundChar;
			};
		};
		^foundChar;
	}

	updateNextChar { // |char|
		var code, char;
		char = this.findNextChar;
		if ( config.useShift ){
			code = config.typeCodes.at( char.asString.toLower[0] );
		}{
			code = config.typeCodes.at( char.asString[0] ); // upper case now in chords
		};

		nextToType = char;
		nextCharW.string_( char.asCompileString );
		nextCharCodeW.string_( code );
		// reset buttons
		buttons.flatten.do{ |it| it.value_(0) };
		code.do{ |i,id|
			var c = [$R,$M,$L, $O ].indexOf( i );
			if ( c < 3 ){
				buttons[c][id].value_(1);
			}
		};
	}

	readNextLine {
		var newLine, oldString;
		var oldNewLines;
		currentLineFromFile = "";
		"read NEXT line from index: ".post; currentLineFromFileIndex.postln;
		// check if we are at the last line of our linesFromFile
		currentLineFromFileIndex = currentLineFromFileIndex+1;
		"to index: ".post; currentLineFromFileIndex.postln;
		if ( currentLineFromFileIndex < linesFromFile.size ){
			newLine = linesFromFile[ currentLineFromFileIndex ];
		}{
			if ( rehearseFile.notNil ){
				newLine = "";
				while( { newLine.size == 0 and: newLine.notNil }){
					newLine = rehearseFile.getLine;
					// newLine.postcs;
					// newLine.size.postln;
				};
			};
			if ( newLine.notNil ){
				linesFromFile = linesFromFile.add( newLine );
			};
			// oldString = lineToTypeW.string;
		};

		// handle multiline newlines...
		/*
		if ( newLine.isNil and: (evaluatedLines.size > 0) ){
			oldNewLines = evaluatedLines.choose;
			oldNewLines = oldNewLines.split( $\n ); // split into lines
			oldNewLines.do{ |it|
				linesFromFile.add( it ); // add each line to the linesFromFile
			};
			newLine = oldNewLines[0];
		};
		*/
		/*
		while( { oldString.first == $\n },{
			oldString = oldString.drop(1);
			oldString.postcs;
		});
		*/
		"read next line - newline: ".post; newLine.postln;

		if ( newLine.notNil ){
			// typingIndex = 0;
			// currentLineFromFileIndex = currentLineFromFileIndex + 1;
			currentLineFromFile = newLine;
			// linesFromFile = linesFromFile.add( currentLineFromFile );
			this.setStringLineToType;
		};
		// lineToTypeW.string_( oldString ++ "\n" ++ newLine );
		"lines: ".post; linesFromFile.size.postln;
	}

	setStringLineToType {
		var string = "";
		linesFromFile.do{ |it,i|
			string = string ++ (i+1).asString.padLeft(4) ++ "|";
			string = string + it;
			string = string ++ "\n";
		};
		lineToTypeW.string_( string );
	}

	setStringLineTyped {
		var string = "";
		var offset = 0;
		var splitstring;
		evaluatedLines.do{ |it,i|
			splitstring = it.split( $\n );
			splitstring.postcs;
			splitstring.do{ |jt,j|
				if ( jt.size > 0 ){
					string = string ++ (offset+1).asString.padLeft(4,"=") ++ "|";
					string = string + jt;
					string = string ++ "\n";
					offset = offset + 1;
				};
			};
		};
		typedLines.do{ |it,i|
			string = string ++ (i+1+offset).asString.padLeft(4,"-") ++ "|";
			string = string + it[1];
			string = string ++ "\n";
		};
		string = string ++ (currentLineFromFileIndex).asString.padLeft(4,"*") ++ "|";
		string = string + typing.string;
		typed.string_( string );

		// ids string
		string = "";
		string = string ++ currentLineFromFileIndex.asString.padLeft(2) ++ ":";
		string = string ++ linesFromFile.size.asString.padLeft(2) ++ ":";
		string = string ++ typedLines.size.asString.padLeft(2) ++ ":";
		string = string ++ evaluatedLines.size.asString.padLeft(2) ++ ":";
		string = string ++ linesExecuted.asString.padLeft(2);
		idsW.string_( string );
	}

	evaluateTyped{
		var result;
		var codeString = "";
		typedLines.do{ |it,i|
			codeString = codeString ++ it[1] ++ "\n";
		};
		linesExecuted = currentLineFromFileIndex;
		"----EVALUATING---".postln;
		codeString.postcs;
		"-----------------".postln;
		codeFunc = codeString.compile;
		if ( codeFunc.notNil ){
			result = true;
			evaluatedLines = evaluatedLines.add( codeString );
			typedLines = []; // reset
			codeFunc.value;
		}{
			result=false;
		};
		^result;
	}


	loadFile{ |fn|
		this.reset;
		if ( rehearseFile.notNil ){ rehearseFile.close; };
		rehearseFile = File.open( fn, "r" );
		// lineToTypeW.string_("");
		this.readNextLine;
		// typingIndex = 0;
		if ( currentLineFromFile.size > 0 ){
			// this.updateNextChar( lineToTypeW.string[typingIndex] );
			// this.updateNextChar( currentLineFromFile[ typingIndex ] );
			this.updateNextChar;
		}
	}

}