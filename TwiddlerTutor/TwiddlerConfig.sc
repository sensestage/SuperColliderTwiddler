TwiddlerConfig {
	var <config;
	var <specialChars;
	var <typeCodes;
	var <chordMap;
	var <>useShift = true;

	*new{ |config|
		^super.new.init( config );
	}

	init{ |cfg|
		config = TabFileReader.read( cfg, true );
		typeCodes = IdentityDictionary.new;
		chordMap = MultiLevelIdentityDictionary.new;

		specialChars = [
			[ "<Space>", $ ],
			[ "<Return>", $\r ],
			[ "<Tab>", $\t ],

			[ "<Escape>", \escape ],
			[ "<UpArrow>", \up ],
			[ "<DownArrow>", \down ],
			[ "<LeftArrow>", \left ],
			[ "<RightArrow>", \right ],
			[ "<Home>", \home ],
			[ "<End>", \end ],
			[ "<PageUp>", \pageup ],
			[ "<PageDown>", \pagedown ],
			[ "<Insert>", \insert ],
			[ "<Delete>", \delete ],
			// [ "<Backspace>", 8.asAscii ],
			[ "<Backspace>", \backspace ],
			// "<Left GUI><Backspace></Left GUI>",
			// "<Left Ctrl>c</Left Ctrl>",
			// "<Left Ctrl>v</Left Ctrl>",
			// "<Left Ctrl>x</Left Ctrl>",
			// "<Left Ctrl>z</Left Ctrl>",
			// "<Left Ctrl>z</Left Ctrl>",
			// "<CapsLock>",
		];

		config.do{ |it|
			var char = it.last.first;
			var size = it.last.size;
			if ( size == 1 ){
				typeCodes.put( char, it[2] );
			}{
				specialChars.do{ |jt|
					if ( jt[0].compare( it.last ) == 0 ){
						char = jt[1];
						typeCodes.put( char, it[2] );
					};
				};
			};

			chordMap.put( it[0].asSymbol, it[2].asSymbol, it.last );

		};
		typeCodes.put( $\n, typeCodes.at( $\r ) );
	}

	at{ |c|
		^typeCodes.at( c );
	}

	findSpecialChar { |charString|
		var foundChar;
		specialChars.do{ |jt|
			if ( jt[0].compare( charString ) == 0 ){
				foundChar = jt[1];
			};
		};
		^foundChar;
	}

	lookup{ |modifier, chord|
		var char, charMod;
		var charModString;
		var specialChar;
		// check if there is a lookup with the modifier
		char = chordMap.at( modifier, chord );
		charMod = modifier;

		if ( char.isNil ){ // no character found with modifier
			"lookup without modifier".postln;
			// check if there is a lookup without the modifier
			char = chordMap.at( 'O', chord );
			// if ( char.notNil ){
			// 	charMod = modifier;
			// };
		};
		if ( char.notNil ){
			"lookup continues...".postln;
			// translate <Left Shift>, <Alt>, <Left Ctrl>
			if ( char.isKindOf( String ) ){
				if ( charMod == 'O' ){
					charModString = "";
				}{
					charModString = charMod.asString;
				};
				if ( char.contains( "<Left Shift>" ) ){
					charModString = charModString ++ "S";
					char = char.replace( "<Left Shift>", "" );
					char = char.replace( "</Left Shift>", "" );
				};
				if ( char.contains( "<Alt>" ) ){
					charModString = charModString ++ "C";
					char = char.replace( "<Alt>", "" );
					char = char.replace( "</Alt>", "" );
				};
				if ( char.contains( "<Left Ctrl>" ) ){
					charModString = charModString ++ "C";
					char = char.replace( "<Left Ctrl>", "" );
					char = char.replace( "</Left Ctrl>", "" );
				};
				specialChar = this.findSpecialChar( char );
				if ( specialChar.notNil ){
					char = specialChar;
				};
				if ( charModString.size == 0 ){
					charMod = 'O';
				}{
					charMod = charModString;
				};
			};
			^[ charMod.asSymbol, char.asSymbol ];
		};
		^nil; // sadly, didn't find anything :(
	}
}
