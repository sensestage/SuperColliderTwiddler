s.plotTree;


// color of current line can be a mix of how well it is going. (red/green)
// + color management task in background (now there are delays in the code)
// tutor should skip empty lines (?) and just post commented lines

// + fix number of lines that are displayed ---> or which ones are displayed.
// + insert white space + | voor sublines van evaluated lines
// + at evaluate previous: set selected in listview

// + highlight buttons of character just typed
//      in color of right or wrong

// catch arrow keys and up/down; change the shortcut for updating the next/prev line to type
// catch arrow keys in edit (line) mode, so that we keep track of the cursor
// maybe

// some characters (capitals, special chars) are not recognised right
// + sometimes the key info we get is too late; perhaps then a comparison might be good to see what the new character typed was, before deeming something as wrong.

// + make a function to copy a previous to the edit window
// + also make that a ListView,
// + so that a line can be selected to edit

(
"sounds.scd".resolveRelative.load;

c = TwiddlerConfig.new( "twiddler_cfg_thumbless_1-1.csv".resolveRelative );
c.useShift = false;
// ~config.config
// ~config.typeCodes
//~config2 = TwiddlerConfig.new( "twiddler_layout.csv".resolveRelative );
g = TwiddlerTutor.new( c );
);

(
g.reset; g.updateWindow;
g.loadFile( "f0blog/2007-20-20-23h07-History-stripped_short.scd".resolveRelative );
// g.loadFile( "testscore.scd".resolveRelative; );
);

(
q = ();
q.asciiSpec = [0,127].asSpec;
q.rightFreqSpec = [ 500, 5000, \exp ].asSpec;
q.wrongFreqSpec = [ 250, 2500, \exp ].asSpec;
g.typedRightAction = { |char| Synth.new( \right, [ \trig, 1, \dur, 0.75, \freq, char.ascii.specMap( q.asciiSpec, q.rightFreqSpec ) ] ); };
g.typedWrongAction = { |char| Synth.new( \wrong, [ \trig, 1, \dur, 0.75, \freq, char.ascii.specMap( q.asciiSpec, q.wrongFreqSpec ), \fmf, 60, \fma, 0.25 ] ); };
g.typedBackspaceAction = { Synth.new( \back, [\trig,1, \freq, 1000, \fmf, 40, \fma, 0.1 ] ); };
// Ndef( \back ).set( \trig, 1, \freq, rrand(3,10)*100, \dur, 0.1, \fmf, 40, \fma, 0.1 ); };
);


g.noPrintLines

"	SinOsc.ar( \\freq.kr(500, 4), 0, \\amp.kr(0.1))".at(0).postcs
.find($\t)

g.charsTyped
g.dump
g.linesFromFile.postcs;
g.typedLines.postcs;
g.typedLines.size;
g.evaluatedLines.postcs;
g.linesExecuted

g.calcEvaluatedLinesSize

g.currentLineFromFileIndex.postcs
g.currentLineTyped.postcs
g.findNextChar
g.typedLines.postcs

g.setStringLineTyped

"--- find matching line ---".postln;
"currentTyped".post; g.currentLineTyped.postln;
g.typedLines
(
g.linesFromFile.drop(-1).reverseDo{ |it,i|
	[it,g.linesFromFile.size - 2 - i].postln;
	(g.typedLines.last == it).postln;

};
)

g.evaluatedLines[0].split( $\n ).postcs;



g.loadFile( "stenotutorial.scd".resolveRelative; );


t.push
newline:
t.quelle(\a, { Impulse.ar(LFNoise1.kr(0.1).range(1, 4)) });
t.filter(\b, { |input| RLPF.ar(input, MouseX.kr(200, 10000, 1) + Rand(0, 300), 0.03) });
newline:
t.filter(\c, { |input| DelayC.ar(input, 0.2, SinOsc.ar(Rand(2, 8)).range(0, 0.2)) + input })
--abc;

// problem with evaluating code -> steno uses preprocessor



//// Jack Armitage - stenophone to livecode
/// group: embodiment and livecoding
