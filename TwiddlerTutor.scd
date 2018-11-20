"sounds.scd".resolveRelative.load;
s.plotTree;


// color of current line can be a mix of how well it is going. (red/green)t

// fix number of lines that are displayed ---> or which ones are displayed.

(
~config = TwiddlerConfig.new( "twiddler_cfg_thumbless_1-1.csv".resolveRelative );
~config.useShift = false;
// ~config.config
// ~config.typeCodes
//~config2 = TwiddlerConfig.new( "twiddler_layout.csv".resolveRelative );
g = TwiddlerTutor.new( ~config );
);
g.loadFile( "testscore.scd".resolveRelative; );
);
g.reset

"	SinOsc.ar( \\freq.kr(500, 4), 0, \\amp.kr(0.1))".at(0).postcs
.find($\t)

g.dump
g.linesFromFile.postcs;
g.typedLines.postcs;
g.evaluatedLines.postcs;
g.linesExecuted
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

(
g.typedRightAction = { Ndef( \right ).set( \trig, 1, \freq, rrand(3,10)*100, \dur, 0.75 ); };
g.typedWrongAction = { Ndef( \wrong ).set( \trig, 1, \freq,300, \dur, 0.75, \fmf, 60, \fma, 0.25 ); };
g.typedBackspaceAction = { Ndef( \back ).set( \trig, 1, \freq, rrand(3,10)*100, \dur, 0.1, \fmf, 40, \fma, 0.1 ); };
);


s
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
