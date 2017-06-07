(
// gui
w = Window.new("LivecodeTwiddlerTutor", Rect( 0, 0, 800, 600) );
w.front;
w.addFlowLayout;

~lineToTypeW = StaticText.new(w, Rect( 0,0, 800, 200 ) ).background_( Color.white );
~lineToTypeW.font_( Font.new( "Courier", 24) );

~typed = StaticText.new( w, Rect( 0,0, 800, 200 ) ).background_( Color.white );
~typed.font_( Font.new( "Courier", 24) );

~spacer1 = StaticText.new( w, Rect( 0, 0, 200, 80 ) );

~nextCharW = StaticText.new( w, Rect( 0, 0, 150, 80 ) );
~nextCharW.font_( Font.new( "Courier", 64) );
~nextCharW.background_( Color.white ).align_( \center );
~nextCharW.string_( "H" );

~nextCharCodeW = StaticText.new( w, Rect( 0, 0, 200, 80 ) );
~nextCharCodeW.font_( Font.new( "Courier", 64) );
~nextCharCodeW.background_( Color.white ).align_( \center );
~nextCharCodeW.string_( "00ML" );

~buttonView = CompositeView.new( w, Rect( 0,0, 4*26 + 5, 3*26 + 5 ) );
~buttons = 3.collect{ |i| 4.collect{ |j| Button.new( ~buttonView, Rect( i*20+2, j*20+2, 21, 21 ) ).states_( [[ "", Color.black, Color.white], ["",Color.white, Color.black]] ).canFocus_( false ); } };


~typing = TextField.new( w, Rect( 0,0, 800, 80 ) ).background_( Color.white );
~typing.font_( Font.new( "Courier", 24) );

// ~historyV = StaticText.new( w, Rect( 0,0, 800, 240 ) ).background_( Color.grey( 0.9 ) );
~typingIndex = 0;
);

(
//~typing.action = { arg field; field.string.postln; };
~typing.addAction( { arg field, char, mods;
	var lastTyped = field.string.last;
	if ( char == 8.asAscii ){ // backspace
		~characterTyped.value( char );
		~typingIndex = (~typingIndex-1).max(0);
		~updateNextChar.value( ~lineToTypeW.string[~typingIndex] );
	}{
		if ( char != 0.asAscii ){
			[ mods, char, lastTyped ].postcs;
			if ( char == $\r ){
				lastTyped = char
			};
			if ( lastTyped.isNil ){
				lastTyped = char;
			};
			~characterTyped.value( lastTyped );
			if( char == $\r ){ // enter
				if ( mods == 262144 ){ // ctrl+enter
					Routine({
						~typed.background_( Color.yellow );
						~codeFunc = ~typed.string.compile;
						if ( ~codeFunc.notNil ){
							~codeFunc.value;
						};
						0.2.wait;
						if ( ~codeFunc.isNil ){
							~typed.background_( Color.red );
							0.2.wait;
						}{
							~typed.background_( Color.green );
							0.2.wait;
							~typed.string_( "" );
							~lineToTypeW.string_("");
							~readNextLine.value;
							~typingIndex = 0;
							~updateNextChar.value( ~lineToTypeW.string[~typingIndex] );
						};
						~typed.background_( Color.white );
					}).play(AppClock);

				}{ // just enter
					~readNextLine.value;
					~typingIndex = ~typingIndex+1;
					~updateNextChar.value( ~lineToTypeW.string[~typingIndex] );
				};
			}{
				~typingIndex = ~typingIndex+1;
				~updateNextChar.value( ~lineToTypeW.string[~typingIndex] );
			}

		}
	}
}, \keyUpAction );
);

~config = TabFileReader.read( "twiddler_layout.csv".resolveRelative, true );
~typeCodes = IdentityDictionary.new;

(
~specialChars = [
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
);

(
~config.do{ |it|
	var char = it.last.first;
	var size = it.last.size;
	if ( size == 1 ){
		~typeCodes.put( char, it[2] );
	}{
		~specialChars.do{ |jt|
			if ( jt[0].compare( it.last ) == 0 ){
				~typeCodes.put( jt[1], it[2] );
			};
		};
	}
};
~typeCodes.put( $\n, ~typeCodes.at( $\r ) );
);

(
~updateNextChar = { |char|
	var code = ~typeCodes.at( char.asString.toLower[0] );
	~nextToType = char;
	~nextCharW.string_( char.asCompileString );
	~nextCharCodeW.string_( code );
	// reset buttons
	~buttons.flatten.do{ |it| it.value_(0) };
	code.do{ |i,id|
		var c = [$R,$M,$L, $O ].indexOf( i );
		if ( c < 3 ){
			~buttons[c][id].value_(1);
		}
	};
};
);

(
~readNextLine = {
	var newLine = ~fileRead.getLine;
	var oldString = ~lineToTypeW.string;
	~lineToTypeW.string_( oldString ++ "\n" ++ newLine );
};
);

~nextToType = $l
(
~characterTyped = { |char|
	var oldString = ~typed.string;
	// "typed".postln;
	if ( char == 8.asAscii ){ // backspace
		~typed.string = oldString.drop(-1);
	}{
		if( char == $\r ){
			~typing.string = "";
			char = $\n;
		};
		~typed.string = ( oldString ++ char );
	};
	if ( char == ~nextToType ){
		Routine({
			~nextCharW.background_( Color.green );
			0.2.wait;
			~nextCharW.background_( Color.white );
		}).play( AppClock );
	}{
		Routine({
			if( char == 8.asAscii ){
				~nextCharW.background_( Color.yellow );
			}{
				~nextCharW.background_( Color.red );
			};
			0.2.wait;
			~nextCharW.background_( Color.white );
		}).play( AppClock );
	};
}
);


// ~rehearseFile = "/home/nescivi/Programming/SuperCollider/soundslikesoup/drones.scd";
~rehearseFile = "testscore.scd".resolveRelative;
~fileRead = File.open( ~rehearseFile, "r" );

(
~lineToTypeW.string_("");
~readNextLine.value;
~typingIndex = 1;
~updateNextChar.value( ~lineToTypeW.string[~typingIndex] );
);

//
~fileRead.close;