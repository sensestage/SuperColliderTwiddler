TwiddlerTutor {

	var <config;

	var <charsTyped = 0;
	var <typingIndex = 0;

	var <cursorPosition = 0;

	var rehearseFile;

	var <evaluatedLines;
	var <linesExecuted;

	var <linesFromFile;
	var <currentLineFromFile;
	var <currentLineFromFileIndex;

	var <typedLines;
	var <currentLineTyped;
	var <currentLineTypedIndex;

	var <killedLine;

	var <codeFunc;


	var <currentMode = \edit; // can be \edit, \selectFromPast, \editTyped, \editTypedLine

	// gui
	var window;
	var <lineToTypeW, <typed;
	var <evaluatedW;
	var spacer1, spacer2;
	var nextCharW, nextCharCodeW, buttonView, buttons;
	var charsTypedW;
	var idsW, idsLabel;
	var lastCharW;

	var hintsView;
	var escV, delV, homeV, pupV, pdnV, endV, leftV, upV, dnV, rightV;
	var modeV;

	var keyUpAction;
	var typedKeyAction;
	var evaluatedKeyAction;
	var toTypeKeyAction;

	var nextToType, typing;

	var <skipjackUpdater;
	var <typedLast = \none;
	var <evaluatedLast = \none;
	var <reevaluatedLast = \none;
	var codeResult;
	var recodeResult = false;
	var recodeLines;

	var defaultHiLiteColor;

	// actions
	var <>typedRightAction;
	var <>typedWrongAction;
	var <>typedBackspaceAction;
	var <>typedEscapeAction;

	*new{ |config|
		^super.new.init( config );
	}

	init{ |cfg|
		defaultHiLiteColor = Color(0.29803921568627, 0.49803921568627, 0.74901960784314);
		config = cfg;
		typingIndex = 0;
		this.reset;
		this.makeWindow;
		this.setTypingAction;
		this.reset;
	}

	reset{
		charsTyped = 0;
		currentLineFromFileIndex = -1;
		currentLineTypedIndex = -1;
		linesExecuted = 0;
		currentLineTyped = "";
		evaluatedLines = [];
		typedLines = [];
		linesFromFile = [];
	}

	makeWindow{
		// gui
		window = Window.new("LivecodeTwiddlerTutor", Rect( 10, 10, 804, 746) );
		window.front;
		window.addFlowLayout( 2@2, 4@4 );


		// just one line
		lineToTypeW = TextField.new( window, Rect( 0,0, 800, 50 ) ).background_( Color.gray(0.95) );
		lineToTypeW.font_( Font.new( "Courier", 22) ).canFocus_( false );


		// textview allows insertion of tab character, but that might need some adjustment to make that work...
		typing = TextField.new( window, Rect( 0,0, 800, 70 ) ).background_( Color.white );
		typing.font_( Font.new( "Courier", 22) );


		/// line with previous char, next char, etc...
		lastCharW = StaticText.new( window, Rect( 0, 0, 95, 86 ) );
		lastCharW.font_( Font.new( "Courier", 24) );
		lastCharW.background_( Color.gray(0.8) ).align_( \center );
		lastCharW.string_( "" );

		charsTypedW = StaticText.new( window, Rect( 0, 0, 75, 86 ) );
		charsTypedW.font_( Font.new( "Courier", 16) );
		charsTypedW.background_( Color.gray(0.8) ).align_( \center );
		charsTypedW.string_( "chars typed\n0" );

		spacer1 = StaticText.new( window, Rect( 0, 0, 5, 86 ) );

		nextCharW = StaticText.new( window, Rect( 0, 0, 150, 86 ) );
		nextCharW.font_( Font.new( "Courier", 64) );
		nextCharW.background_( Color.white ).align_( \center );
		nextCharW.string_( "" );

		nextCharCodeW = StaticText.new( window, Rect( 0, 0, 200, 86 ) );
		nextCharCodeW.font_( Font.new( "Courier", 64) );
		nextCharCodeW.background_( Color.white ).align_( \center );
		nextCharCodeW.string_( "0000" );

		buttonView = CompositeView.new( window, Rect( 0,0, 3*20 + 6, 4*20 + 6 ) );
		buttons = 3.collect{ |i|
			4.collect{ |j|
				Button.new( buttonView, Rect( i*20+2, j*20+2, 21, 21 ) ).states_(
					[
						["",Color.black, Color.white], //
						["",Color.white, Color.black], // to type
						["",Color.white, Color.yellow()], // just typed
						["",Color.white, Color.yellow(0.75)] // just typed and to type
					] ).canFocus_( false );
		} };

		spacer2 = StaticText.new( window, Rect( 0, 0, 5, 86 ) );

		idsLabel = StaticText.new( window, Rect( 0, 0, 100, 86 ) );
		idsLabel.font_( Font.new( "Courier", 16) );
		idsLabel.background_( Color.gray(0.8) ).align_( \right );
		idsLabel.string_( "cursor:\nfrom file:\ntyped:\nevaluated:" );

		idsW = StaticText.new( window, Rect( 0, 0, 70, 86 ) );
		idsW.font_( Font.new( "Courier", 16) );
		idsW.background_( Color.gray(0.8) ).align_( \center );


		/// hints on general shortcuts

		hintsView = CompositeView.new( window, Rect( 0,0, 800, 30 ) );
		escV = StaticText.new( hintsView, Rect( 2, 0, 78, 30 ) ).background_( Color.gray(0.95) ).string_( "ESC: " ++ config.at( \escape ) ).align_( \center );
		// StaticText.new( hintsView, Rect( 52, 0, 48, 15 ) ).background_( Color.gray(0.95) ).string_( "INS: " ++ config.at( \insert ) );
		delV = StaticText.new( hintsView, Rect( 82, 0, 78, 30 ) ).background_( Color.gray(0.95) ).string_( "DEL: " ++ config.at( \delete ) ).align_( \center );

		homeV = StaticText.new( hintsView, Rect( 162, 0, 108, 30 ) ).background_( Color.gray(0.95) ).string_( "HOME: " ++ config.at( \home ) ).align_( \center );
		pupV = StaticText.new( hintsView, Rect( 272, 0, 108, 15 ) ).background_( Color.gray(0.95) ).string_( "PgUp: " ++ config.at( \pageup ) ).align_( \center );
		pdnV = StaticText.new( hintsView, Rect( 272, 15, 108, 15 ) ).background_( Color.gray(0.95) ).string_( "PgDn: " ++ config.at( \pagedown ) ).align_( \center );
		endV = StaticText.new( hintsView, Rect( 382, 0, 108, 30 ) ).background_( Color.gray(0.95) ).string_( "END: " ++ config.at( \end ) ).align_( \center );

		leftV = StaticText.new( hintsView, Rect( 492, 0, 78, 30 ) ).background_( Color.gray(0.95) ).string_( "<: " ++ config.at( \left ) ).align_( \center );
		upV = StaticText.new( hintsView, Rect( 572, 0, 78, 15 ) ).background_( Color.gray(0.95) ).string_( "^: " ++ config.at( \up ) ).align_( \center );
		dnV = StaticText.new( hintsView, Rect( 572, 15, 78, 15 ) ).background_( Color.gray(0.95) ).string_( "v: " ++ config.at( \down ) ).align_( \center );
		rightV = StaticText.new( hintsView, Rect( 652, 0, 78, 30 ) ).background_( Color.gray(0.95) ).string_( ">: " ++ config.at( \right ) ).align_( \center );

		modeV = StaticText.new( hintsView, Rect( 732, 0, 78, 30 ) ).background_( Color.white ).string_( "mode:\n" ++ currentMode ).align_( \center );

		/// typed lines

		typed = ListView.new( window, Rect( 0,0, 800, 240 ) ).background_( Color.gray(0.95) ).selectionMode_( \single );
		typed.font_( Font.new( "Courier", 20) ).canFocus_( false );


		evaluatedW = ListView.new( window, Rect( 0,0, 800, 240 ) ).background_( Color.gray(0.95) ).selectionMode_( \single );
		evaluatedW.font_( Font.new( "Courier", 20) ).hiliteColor_(defaultHiLiteColor).canFocus_( false );

		skipjackUpdater = SkipJack.new( { this.updateColors }, 0.1, { window.isClosed }, "twiddlertutor" );
	}

	updateColors{

		switch( reevaluatedLast,
			0, {
				reevaluatedLast = reevaluatedLast + 1;
				evaluatedW.hiliteColor_(Color.yellow)
			},
			1, { reevaluatedLast = reevaluatedLast + 1; },
			2, { reevaluatedLast = reevaluatedLast + 1; },
			3, {
				reevaluatedLast = reevaluatedLast + 1;
				if ( recodeResult ){
					evaluatedW.hiliteColor_(Color.green)
				}{
					evaluatedW.hiliteColor_(Color.red)
				}
			},
			4, { reevaluatedLast = reevaluatedLast + 1; },
			5, { reevaluatedLast = reevaluatedLast + 1; },
			6, { reevaluatedLast = reevaluatedLast + 1;
				evaluatedW.hiliteColor_(defaultHiLiteColor)
			}
		);

		switch( evaluatedLast,
			0, { evaluatedLast = evaluatedLast + 1;
				typed.background_( Color.yellow );
			},
			1, { evaluatedLast = evaluatedLast + 1; },
			2, { evaluatedLast = evaluatedLast + 1; },
			3, { evaluatedLast = evaluatedLast + 1;
				if ( codeResult ){
					typed.background_( Color.green );
					// typed.string_( "" );
				}{
					typed.background_( Color.red );
				}
			},
			4, { evaluatedLast = evaluatedLast + 1; },
			5, { evaluatedLast = evaluatedLast + 1; },
			6, { evaluatedLast = -1;
				typed.background_( Color.white );
			}
		);

		switch( typedLast,
			\none, { nextCharW.background_( Color.white ); },
			\backspace, { nextCharW.background_( Color.yellow ); typedLast = 0; },
			\delete, { nextCharW.background_( Color.yellow ); typedLast = 0; },
			\escape, { nextCharW.background_( Color.magenta ); typedLast = 0; },
			\navigate, { nextCharW.background_( Color.blue ); typedLast = 0; },
			\right, { nextCharW.background_( Color.green ); typedLast = 0; },
			\wrong, { nextCharW.background_( Color.red ); typedLast = 0; },
			0, { typedLast = typedLast + 1; },
			1, { typedLast = typedLast + 1; },
			2, { typedLast = typedLast + 1; },
			3, {
				typedLast = \none;
				this.unhighlightJustTyped;
			}
		);
	}

	updateWindow {
		charsTypedW.string_( "chars typed\n" ++ charsTyped.asString.padLeft(6,"0") );
		this.setStringLineToType;
		this.setStringLineTyped;
		this.setStringEvaluated;
	}

	setTypingAction {
		/// TYPING VIEW ACTION
		if ( keyUpAction.notNil ){ typing.removeAction( keyUpAction, \keyUpAction ); };
		keyUpAction = { arg field, char, mods, unicode, keycode, key;
			var lastTyped = field.string.last;
			// [char, mods, unicode, keycode, key].postcs;

			charsTyped = charsTyped + 1;
			charsTypedW.string_( "chars typed\n" ++ charsTyped.asString.padLeft(6,"0") );

			// [char, mods].postcs;
			this.checkCharacter( char, mods, lastTyped ); // was this the right character?

			// check mode switch
			if ( char == 27.asAscii ){ // escape
				this.switchMode;
				this.highlightJustTyped( char );
			}{
				switch( currentMode,
					\edit, {
						this.parseCharacterEditMode( char, mods, keycode, lastTyped );
					},
					\editTypedLine, {
						this.parseCharacterEditLineMode( char, mods, keycode, lastTyped );
					}
				);
			};
		};
		typing.addAction( keyUpAction, \keyUpAction );

		/// EVALUATED VIEW WINDOW
		if ( evaluatedKeyAction.notNil ){ evaluatedW.removeAction( evaluatedKeyAction, \keyUpAction ); };
		evaluatedKeyAction = { arg field, char, mods, unicode, keycode, key;
			// ["evaluatedV", char, mods, unicode, keycode, key].postcs;
			if ( char == 27.asAscii ){ // escape
				this.checkCharacter( char, mods ); // was this the right character?
				this.switchMode;
				this.highlightJustTyped( char );
			}{
				if ( currentMode == \selectFromPast ){
					this.parseCharacterSelectFromPastMode( char,mods, keycode );
				};
			};
		};
		evaluatedW.addAction( evaluatedKeyAction, \keyUpAction );


		/// TYPED VIEW WINDOW
		if ( typedKeyAction.notNil ){ typed.removeAction( typedKeyAction, \keyUpAction ); };
		typedKeyAction = { arg field, char, mods,unicode, keycode, key;
			// ["typedV", char, mods, unicode, keycode, key].postcs;
			if ( char == 27.asAscii ){ // escape
				this.checkCharacter( char, mods ); // was this the right character?
				this.switchMode;
				this.highlightJustTyped( char );
			}{
				if ( currentMode == \editTyped ){
					this.parseCharacterEditTyped( char,mods, keycode );
				};
			};
		};
		typed.addAction( typedKeyAction, \keyUpAction );

	}

	switchMode{ |newMode|
		if ( newMode.isNil ){
			switch( currentMode,
				\edit, { currentMode = \selectFromPast; },
				\selectFromPast, { currentMode = \editTyped; },
				\editTyped, { currentMode = \edit; },
				\editTypedLine, { currentMode = \editTyped; }
				// \selectFromFuture, { currentMode = \edit; }
			);
		}{
			currentMode = newMode;
		};
		switch( currentMode,
			\edit, {
				// lineToTypeW.canFocus_(false).background_( Color.gray(0.95) );
				typing.canFocus_(true).focus(true).background_( Color.white );
				typed.canFocus_(false).background_( Color.gray(0.95) );
				evaluatedW.canFocus_(false).background_( Color.gray(0.95) );
				modeV.string_( "mode:\nEDIT" );
			},
			\selectFromPast, {
				// lineToTypeW.canFocus_(false).background_( Color.gray(0.95) );
				typing.canFocus_(false).background_( Color.gray(0.95) );
				typed.canFocus_(false).background_( Color.gray(0.95) );
				evaluatedW.canFocus_(true).focus(true).background_( Color(1,0.9,1) );
				modeV.string_( "mode:\nSELECT\nPAST" );
			},
			// \selectFromFuture, {
			// 	lineToTypeW.canFocus_(true).focus(true).background_( Color(1,0.95,1) );
			// 	typing.canFocus_(false).background_( Color.gray(0.95) );
			// 	typed.canFocus_(false).background_( Color.gray(0.95) );
			// 	evaluatedW.canFocus_(false).background_( Color.gray(0.95) );
			// },
			\editTyped, {
				// lineToTypeW.canFocus_(false).background_( Color.gray(0.95) );
				typing.canFocus_(false).background_( Color.gray(0.95) );
				typed.canFocus_(true).focus(true).background_( Color(1,0.9,1) );
				evaluatedW.canFocus_(false).background_( Color.gray(0.95) );
				modeV.string_( "mode:\nEDIT\nTYPED" );
			},
			\editTypedLine, {
				// lineToTypeW.canFocus_(false).background_( Color.gray(0.95) );
				typing.canFocus_(true).focus(true).background_( Color(1,0.9,1) );
				typed.canFocus_(false).background_( Color.gray(0.95) );
				evaluatedW.canFocus_(false).background_( Color.gray(0.95) );
				modeV.string_( "mode:\nEDIT\nLINE" );
			}
		);


	}

	parseCharacterEditTyped{|char,mods,keycode|
		var index,line;
		var justTyped;
		// "parse character edit typed mode".postln;
		// [char,mods,keycode].postcs;
		if ( mods == 0 ){ // no modifiers
			switch( char,
				$k, {
					">> kill line".postln;
					if ( typed.selection.size > 0 ){
						index = typed.selection.first;
					}{
						index = 0;
					};
					killedLine = typedLines.removeAt( index );
					this.setStringLineTyped;
				},
				$i, {
					">> insert line".postln;
					if ( typed.selection.size > 0 ){
						index = typed.selection.first;
					}{
						index = 0;
					};
					typedLines = typedLines.insert( index, [ typedLines.at(index)[0], "" ] );
					// renumber the lines after
					typedLines.do{ |it,i|
						if ( i > index ){
							it[0] = it[0]+1;
						}
					};
					this.setStringLineTyped;
				},
				$p, {
					">> paste killed line".postln;
					if ( typed.selection.size > 0 ){
						index = typed.selection.first;
					}{
						index = 0;
					};
					typedLines = typedLines.insert( index, [ typedLines.at(index)[0], killedLine[1] ] );
					// renumber the lines after
					typedLines.do{ |it,i|
						if ( i > index ){
							it[0] = it[0]+1;
						}
					};
					this.setStringLineTyped;
				},
				$e, {
					">> edit line".postln;
					// copy line to typing window
					// [ typed.value, typed.selection ].postln;
					if ( typed.selection.size > 0 ){
						index = typed.selection.first;
					}{
						index = 0;
					};
					line = typedLines.at( index );
					typing.string_( line[1] );
					currentLineTyped = typing.string;
					this.setCursorPosition(\end);
					// go to mode: editing typed line
					this.switchMode( \editTypedLine );
			})
		};
		if ( mods == 262144 and: (char == $\r) ){ // ctrl+enter
			// "~~~ ctrl+enter".postln;
			evaluatedLast = 0;
			codeResult = this.evaluateTyped;
			[ codeResult, typedLines ].postln;
			if ( codeResult ){
				this.setStringEvaluated;
				this.setStringLineTyped;
				this.readNextLine;
				this.switchMode( \edit );
			};
		};
		if ( mods == 524288 ){ // ALT
			if ( char == \r ){
				">> evaluate line".postln;
					// evaluate current line
					evaluatedLast = 0;
					codeResult = this.evaluateTypedLine;
					this.setStringEvaluated;
					this.setStringLineTyped;
			};
			// switch( char,
			// 	$\r, { //enter
			// 		">> evaluate line".postln;
			// 		// evaluate current line
			// 		evaluatedLast = 0;
			// 		codeResult = this.evaluateTypedLine;
			// 		this.setStringEvaluated;
			// 		this.setStringLineTyped;
			// 	},
			// 	// $n, {
			// 	// 	// show next line of to type
			// 	// 	this.readNextLine;
			// 	// },
			// 	// $r, {
			// 	// 	// show previous line of to type
			// 	// 	this.readPreviousLine;
			// 	// }
			// );
			switch( keycode,
				65364, {
					this.readNextLine; justTyped = \up;
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				},
				65362, {
					this.readPreviousLine; justTyped = \down;
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				},
				65367, {
					this.gotoLastLine; justTyped = \end;
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				}
			);
		};
	}

	parseCharacterSelectFromPastMode{|char,mods,keycode|
		var index,line,splitstring;
		var justTyped;
		// "parse character select from past mode".postln;
		// [char,mods,keycode].postcs;
		if ( char == $c ){ // copy
			index = evaluatedW.value ? 0; // index of items
			line = evaluatedLines.wrapAt( -1*index - 1 );
			splitstring = line.split( $\n );
			// [index,line,splitstring].postcs;
			splitstring.do{ |it,i|
				typedLines = typedLines.add( [currentLineTypedIndex + i, it ] );
			};
			// typedLines.postln;
			this.setStringLineTyped;
			this.switchMode; // go to edit mode
		};
		if ( mods == 524288 ){ // ALT
			if ( char == \r ){
				">> re-evaluate line".postln;
				index = evaluatedW.value ? 0; // index of items
				this.reevaluateLine( index + 1 );
			};
			switch( keycode,
				65364, {
					this.readNextLine; justTyped = \up;
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				},
				65362, {
					this.readPreviousLine; justTyped = \down;
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				},
				65367, {
					this.gotoLastLine; justTyped = \end;
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				}
			);
		};
	}

	parseCharacterEditMode{ |char,mods,keycode,lastTyped|
		var justTyped, typingstring;
		// [char,mods,keycode,lastTyped].postcs;
		if ( char == 8.asAscii ){ // backspace
			// "typing action backspace".postln;
			this.typedBackspace(true);
			this.setStringLineTyped;
		}{
			if ( char != 0.asAscii ){
				// [ mods, char, lastTyped ].postcs;
				// process character other than backspace
				if ( char == $\r ){
					lastTyped = $\r;
				};
				if ( char == $\n ){
					lastTyped = $\r;
				};
				if ( char == $\t ){
					lastTyped = $\t;
					typingstring = typing.string.insert( cursorPosition, lastTyped );
					typing.string_( typingstring );
					// typing.string_( typing.string.add( lastTyped ) );
				};
				if ( lastTyped.isNil ){
					lastTyped = char;
				};
				lastCharW.string_( lastTyped.asCompileString );

				// this.checkCharacter( lastTyped ); // was this the right character?
				if( char == $\r ){ // enter
					currentLineTyped = typing.string;
					currentLineTypedIndex = currentLineTypedIndex + 1;
					if ( currentLineTyped.size > 0 ){
						typedLines = typedLines.add( [ currentLineTypedIndex, currentLineTyped ] );
						typing.string = "";
						currentLineTyped = "";
						if ( mods == 262144 ){ // ctrl+enter
							// "~~~ ctrl+enter".postln;
							evaluatedLast = 0;
							codeResult = this.evaluateTyped;
							this.setStringEvaluated;
						};
						// just enter
						// "~~~ enter".postln;
						this.readNextLine;
						this.setCursorPosition( \home ); // back to zero
					};
				}{
					if ( mods == 524288 ){ // ALT
						if ( char.isDecDigit ){ // ALT + number
							this.reevaluateLine( char.digit );
							currentLineTyped = typing.string.drop( -1 );
							typing.string_( currentLineTyped );
							this.setStringEvaluated;
						};
						if( char == $n ){ // show next line of to type
							this.readNextLine;
							// remove n again from typing window
							currentLineTyped = typing.string.drop( -1 );
							typing.string_( currentLineTyped );
						};
						if ( char == $r ){ // show previous line of to type
							this.readPreviousLine;
							// remove r again from typing window
							currentLineTyped = typing.string.drop( -1 );
							typing.string_( currentLineTyped );
						};
					}{
						// "~~~ other char".postln;
						currentLineTyped = typing.string;
						this.increaseCursorPosition; // increase cursorposition
					};
				};
				this.setStringLineTyped;
				this.updateNextChar;
				this.highlightJustTyped( char );
			}{ // char == 0.asAscii --> check keycode
				if ( mods == 0 ){
					switch( keycode,
						65361, { this.decreaseCursorPosition; justTyped = \left; },
						65363, { this.increaseCursorPosition; justTyped = \right; },
						65360, { this.setCursorPosition(\home); justTyped = \home; },
						65367, { this.setCursorPosition(\end); justTyped = \end; }
					);
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				};
				if ( mods == 524288 ){
					switch( keycode,
						65364, { this.readNextLine; justTyped = \up; },
						65362, { this.readPreviousLine; justTyped = \down; },
						65367, { this.gotoLastLine; justTyped = \end; }
					);
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				}
			}
		}
	}

	parseCharacterEditLineMode{ |char,mods,keycode,lastTyped|
		var justTyped;
		var typingstring;
		// "parseCharacterEditLineMode".postln;
		// [ char, mods, keycode, lastTyped ].postcs;
		// typedLines.postln;
		if ( char == 8.asAscii ){ // backspace
			// "typing action backspace".postln;
			this.typedBackspace( false );
		}{
			if ( char != 0.asAscii ){
				// [ mods, char, lastTyped ].postcs;
				// process character other than backspace
				if ( char == $\r ){
					lastTyped = $\r;
				};
				if ( char == $\n ){
					lastTyped = $\r;
				};
				if ( char == $\t ){
					lastTyped = $\t;
					typingstring = typing.string.insert( cursorPosition, lastTyped );
					typing.string_( typingstring );
					// typing.string_( typing.string.add( lastTyped ) ); // this might give issues
				};
				if ( lastTyped.isNil ){
					lastTyped = char;
					"lastTyped is nil!!!".post;
					[lastTyped, char, mods, keycode].postln;
				};
				lastCharW.string_( lastTyped.asCompileString );

				// this.checkCharacter( lastTyped ); // was this the right character?

				if( char == $\r and: (mods == 0) ){ // just enter
					// update the line in the typed window and switch to that window again
					var curIndex;
					// update the line we are editing
					currentLineTyped = typing.string;
					if ( currentLineTyped.size > 0 ){
						curIndex = typed.selection.first ? 0;
						/*[ "enter edited line",
							typed.value, typedLines.at( typed.value ),
							typed.selection, curIndex, typedLines.at( curIndex )
						].postln;*/

						typedLines.put(
							curIndex,
							[ typedLines.at( curIndex )[0], currentLineTyped ]
						);
						typed.selection_( typed.selection + 1 ); // advance the line with one
						typing.string = "";
						currentLineTyped = "";
						this.setCursorPosition( \home ); // back to zero
						this.readNextLine;
						this.setStringLineTyped;
						this.switchMode( \editTyped );
					};
				}{
					if ( mods == 524288 ){ // ALT
						if ( char.isDecDigit ){ // ALT + number
							this.reevaluateLine( char.digit );
							currentLineTyped = typing.string.drop( -1 );
							typing.string_( currentLineTyped );
							this.setStringEvaluated;
						};
						/*
						if( char == $n ){ // show next line of to type
							this.readNextLine;
							currentLineTyped = typing.string.drop( -1 );
							typing.string_( currentLineTyped );
						};
						if ( char == $r ){ // show previous line of to type
							this.readPreviousLine;
							currentLineTyped = typing.string.drop( -1 );
							typing.string_( currentLineTyped );
						};
						*/
					}{
						// "~~~ other char".postln;
						currentLineTyped = typing.string;
						this.increaseCursorPosition; // increase cursorposition
					};
				};
				// this.setStringLineTyped; // update the window
				this.updateNextChar; // this needs to be fixed
				this.highlightJustTyped( char );
			}{ // char == 0.asAscii --> check keycode
				if ( mods == 0 ){
					switch( keycode,
						65361, { this.decreaseCursorPosition; justTyped = \left; },
						65363, { this.increaseCursorPosition; justTyped = \right; },
						65360, { this.setCursorPosition(\home); justTyped = \home; },
						65367, { this.setCursorPosition(\end); justTyped = \end; }
					);
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				};
				if ( mods == 524288 ){
					switch( keycode,
						65364, { this.readNextLine; justTyped = \up; },
						65362, { this.readPreviousLine; justTyped = \down; },
						65367, { this.gotoLastLine; justTyped = \end; }
					);
					this.updateIDString;
					this.updateNextChar;
					this.highlightJustTyped( justTyped );
				}

			}
		}
	}

	reevaluateLine{ |index|
		var codeString, codeFunc;
		// index.postln;
		evaluatedW.selection_( [ index - 1 ] );
		codeString = evaluatedLines.wrapAt( -1 * index );

		"---RE-EVALUATING---".postln;
		codeString.postcs;
		"-----------------".postln;
		codeFunc = codeString.compile;
		if ( codeFunc.notNil ){
			recodeResult = true;
			codeFunc.value;
		}{
			recodeResult=false;
		};
		reevaluatedLast = 0;
	}

	increaseCursorPosition{
		cursorPosition = cursorPosition + 1;
		if ( cursorPosition > typing.string.size ){
			cursorPosition = typing.string.size;
			^false;
		}
		^true;
	}

	decreaseCursorPosition{
		cursorPosition = cursorPosition - 1;
		if ( cursorPosition < 0 ){
			cursorPosition = 0;
			^false;
		}
		^true;
	}

	setCursorPosition{ |pos|
		switch( pos,
			\end, { cursorPosition = typing.string.size; },
			\home, { cursorPosition = 0; },
			{ cursorPosition = pos }
		);
	}

	typedBackspace { |skipToPrevious=true|
		var prevTyped = currentLineTyped;
		// "==TYPING backpace: ".postln; currentLineTyped.postcs;
		// "typed lines ".post; typedLines.size.postln; typedLines.postcs;
		// only need to do something when going back a line...

		lastCharW.string_( "<--" );

		this.decreaseCursorPosition;
		if ( currentLineTyped.size == 0 ){
			// get previous line
			if ( typedLines.size != 0 and: skipToPrevious ){ // otherwise nothing to backspace to
				// "go to previous line".postln;
				currentLineFromFileIndex = typedLines.last[0];
				currentLineFromFile = linesFromFile[ currentLineFromFileIndex ];
				currentLineTyped = typedLines.last[1];
				typedLines = typedLines.drop(-1); // drop the last of the typed lines;
			};
			typing.string_( currentLineTyped );
			this.setCursorPosition( \end );
		}{
			currentLineTyped = typing.string;
		};

		this.updateNextChar;
		this.highlightJustTyped( 8.asAscii );
	}

	checkCharacter{ |char, mods, last|
		// [ "checkCharacter", char, mods, last, nextToType ].postcs;
		if ( char == nextToType ){
			typedLast = \right;
			typedRightAction.value( char );
		}{
			switch( char,
				8.asAscii, {
					typedLast = \backspace;
					typedBackspaceAction.value;
				},
				27.asAscii, {
					typedLast = \escape;
					typedEscapeAction.value;
				},
				127.asAscii, {
					typedLast = \delete;
					typedBackspaceAction.value;
				},
				0.asAscii, { // often also sent after doing a shift operation
					typedLast = \none;
				},
				{
					if ( last != nextToType ){
						typedLast = \wrong;
						typedWrongAction.value( char );
					}{
						typedLast = \right;
						typedRightAction.value( char );
					}
				}
			);
		};
		^typedLast;
	}

	findNextChar{
		// var foundIndex;
		// var lastWord;
		var foundChar;
		// "CURRENT line:\t".post; currentLineTyped.postcs;
		// "from file line:\t".post; currentLineFromFile.postcs;
		// "cursor at:\t".post; cursorPosition.postln;

		if ( currentLineFromFile.notNil ){
			if ( cursorPosition >= currentLineFromFile.size ){
				foundChar = $\r;
			}{
				foundChar = currentLineFromFile[cursorPosition];
			}
		};
		^foundChar;

		/*
		if ( currentLineTyped.notNil ){
			lastWord = currentLineTyped;
			// "LAST word: ".post; lastWord.postcs;
			if ( currentLineFromFile.notNil ){
				foundIndex = lastWord.size;
				typingIndex = foundIndex;
				// foundIndex.postln;
				if ( foundIndex == currentLineFromFile.size ){
					foundChar = $\r;
					// foundChar.postcs;
					^foundChar;
				};
				foundChar = currentLineFromFile[typingIndex];
				// foundChar.postcs;
				^foundChar;
			};
		};
		^foundChar;
		*/
	}

	updateNextChar { // |char|
		var code, char;
		char = this.findNextChar;
		if ( config.useShift ){
			code = config.at( char.asString.toLower[0] );
		}{
			code = config.at( char.asString[0] ); // upper case now in chords
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

	highlightJustTyped{ |char|
		var code, oldVal, symbol;
		// char.isKindOf( Char ).postln;
		if ( char.isKindOf( Char ) ){
			if ( config.useShift ){
				code = config.at( char.asString.toLower[0] );
			}{
				code = config.at( char.asString[0] ); // upper case now in chords
			};
		}{
			switch( char,
				8.asAscii, { symbol = \backspace },
				27.asAscii, { symbol = \escape },
				127.asAscii, { symbol = \delete },
				{ symbol = char }
			);
			code = config.at( symbol );
		};
		// [ char, code, symbol ].postln;
		code.do{ |i,id|
			var c = [$R,$M,$L, $O ].indexOf( i );
			if ( c < 3 ){
				oldVal = buttons[c][id].value;
				buttons[c][id].value_( oldVal + 2 );
			}
		};
	}

	unhighlightJustTyped{
		buttons.do{ |butRow|
			butRow.do{ |it|
				it.value_( it.value.mod(2) );
			}
		};
	}

	readPreviousLine {
		var newLine, oldString;
		currentLineFromFileIndex = currentLineFromFileIndex-1;
		if ( currentLineFromFileIndex < linesFromFile.size ){
			newLine = linesFromFile[ currentLineFromFileIndex ];
		};
		if ( newLine.notNil ){
			currentLineFromFile = newLine;
			this.setStringLineToType;
		};
	}

	gotoLastLine {
		var newLine;
		currentLineFromFile = "";
		currentLineFromFileIndex = linesFromFile.size - 1;
		newLine = linesFromFile[ currentLineFromFileIndex ];
		if ( newLine.notNil ){
			currentLineFromFile = newLine;
			this.setStringLineToType;
		};
	}

	readNextLine {
		var newLine;
		currentLineFromFile = "";
		// check if we are at the last line of our linesFromFile
		currentLineFromFileIndex = currentLineFromFileIndex+1;
		if ( currentLineFromFileIndex < linesFromFile.size ){
			newLine = linesFromFile[ currentLineFromFileIndex ];
		}{
			if ( rehearseFile.notNil ){
				newLine = "";
				while( { newLine.size == 0 and: newLine.notNil }){
					newLine = rehearseFile.getLine;
				};
			};
			if ( newLine.notNil ){
				linesFromFile = linesFromFile.add( newLine );
			};
		};

		if ( newLine.notNil ){
			currentLineFromFile = newLine;
			this.setStringLineToType;
		};
	}

	setStringLineToType {
		// lineToTypeW.value_( currentLineFromFile ).selection_( currentLineFromFile );
		lineToTypeW.string_( currentLineFromFile );
	}

	calcEvaluatedLinesSize{
		var size = 0;
		var splitstring;
		evaluatedLines.do{ |it,i|
			splitstring = it.split( $\n );
			splitstring.removeAllSuchThat{ |it| it == "" };
			size = size + splitstring.size;
		};
		^size;
	}

	setStringEvaluated {
		var itemsForView;
		var splitstring;
		var index;
		var itemString;
		var curSelected = evaluatedW.selection;
		var curItemSize = evaluatedW.items.size;
		itemsForView = evaluatedLines.collect{ |it,i|
			splitstring = it.split( $\n );
			index = (evaluatedLines.size - i);
			splitstring.do{ |jt,j|
				if ( j==0 ){
					itemString = index.asString.padLeft(4," ") ++ "|" ++ jt
				}{
					itemString = itemString ++ "\n" ++ "|".padLeft(5," ") ++ jt
				}
			};
			itemString;
		};
		evaluatedW.items_( itemsForView.reverse );
		if ( curItemSize < itemsForView.size ){
			curSelected = curSelected + itemsForView.size - curItemSize;
		};
		evaluatedW.value_( curSelected.first ).selection_( curSelected );
	}

	setStringLineTyped {
		var itemsForView;
		var string;
		var curSelected = typed.selection;
		var curItemSize = typed.items.size;

		itemsForView = typedLines.collect{ |it,i|
			(it[0]+1).asString.padLeft(4," ") ++ "|" ++ it[1]
		};

		if ( currentMode == \edit ){
			// add current line typed
			itemsForView = itemsForView.add( (currentLineFromFileIndex+1).asString.padLeft(4,"*") ++ "|" ++ typing.string );
		};

		typed.items_( itemsForView );
		typed.value_( curSelected.first ).selection_( curSelected );

		this.updateIDString;
	}

	updateIDString{
		var string;
		// ids string
		string = "";
		string = string ++ cursorPosition.asString.padLeft(2) ++ ":";
		string = string ++ typing.string.size.asString.padLeft(2) ++ "\n";
		string = string ++ currentLineFromFileIndex.asString.padLeft(2) ++ ":";
		string = string ++ linesFromFile.size.asString.padLeft(2) ++ "\n";
		string = string ++ currentLineTypedIndex.asString.padLeft(2) ++ ":";
		string = string ++ typedLines.size.asString.padLeft(2) ++ "\n";
		string = string ++ evaluatedLines.size.asString.padLeft(2) ++ ":";
		string = string ++ linesExecuted.asString.padLeft(2);
		idsW.string_( string );
	}

	evaluateTypedLine{
		var result;
		var codeString = "";
		codeString = typedLines.at( typed.selection.first )[1];

		// linesExecuted = currentLineFromFileIndex;
		"----EVALUATING LINE---".postln;
		codeString.postcs;
		"-----------------".postln;
		codeFunc = codeString.compile;
		if ( codeFunc.notNil ){
			result = true;
			evaluatedLines = evaluatedLines.add( codeString );
			linesExecuted = this.calcEvaluatedLinesSize;
			codeFunc.value;
			// remove the line
			typedLines.removeAt( typed.value );

			evaluatedW.selection_( nil ).value_(0); // hopefully this goes to top?
		}{
			result=false;
		};
		^result;
	}


	evaluateTyped{
		var result;
		var codeString = "";
		typedLines.do{ |it,i|
			codeString = codeString ++ it[1];
			if ( i < (typedLines.size-1) ){
				codeString = codeString ++ "\n";
			}
		};
		// linesExecuted = currentLineFromFileIndex;
		"----EVALUATING---".postln;
		codeString.postcs;
		"-----------------".postln;
		codeFunc = codeString.compile;
		if ( codeFunc.notNil ){
			result = true;
			evaluatedLines = evaluatedLines.add( codeString );
			linesExecuted = this.calcEvaluatedLinesSize;
			typedLines = []; // reset
			codeFunc.value;

			evaluatedW.selection_( nil ).value_(0); // hopefully this goes to top?
		}{
			result=false;
		};

		^result;
	}


	loadFile{ |fn|
		this.reset;
		if ( rehearseFile.notNil ){ rehearseFile.close; };
		rehearseFile = File.open( fn, "r" );
		this.readNextLine;
		// typingIndex = 0;
		if ( currentLineFromFile.size > 0 ){
			this.updateNextChar;
		}
	}

}