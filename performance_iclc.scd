(
c = TwiddlerConfig.new(	"twiddler_cfg_thumbless_reduced.csv".resolveRelative );
g = MBTwiddlerTutor.new( c );
);

(
"sounds.scd".resolveRelative.load;
g.reset; g.gui.updateWindow;
g.loadFile( "f0blog/2007-20-20-23h07-History-stripped_short.scd".resolveRelative );
// g.loadFile( "testscore.scd".resolveRelative; );
);

"dmx.scd".resolveRelative.load;

(
q = ();
q.asciiSpec = [0,127].asSpec;
q.rightFreqSpec = [ 500, 5000, \exp ].asSpec;
q.wrongFreqSpec = [ 250, 2500, \exp ].asSpec;
g.typedRightAction = { |char|
	// char.postln;
	if ( char.notNil ){
		Synth.new( \right, [ \trig, 1, \dur, 0.75, \freq, char.ascii.specMap( q.asciiSpec, q.rightFreqSpec ) ] );
	// }{
	// 	"right!! but char is nil".postln;
	};
};
g.typedWrongAction = { |char|
	// char.postln;
	if ( char.notNil ){
		Synth.new( \wrong, [ \trig, 1, \dur, 0.75, \freq, char.ascii.specMap( q.asciiSpec, q.wrongFreqSpec ), \fmf, 60, \fma, 0.25 ] );
	// }{
	// 	"wrong!! but char is nil".postln;
	};
};
g.typedBackspaceAction = { Synth.new( \back, [\trig,1, \freq, 1000, \fmf, 40, \fma, 0.1 ] ); };
);



// ------------- minibee backup -----------------

"/home/nescivi/git/projects/diy_twiddler/supercollider/minibee2osc.scd".load;

(
m = MBTwiddler.new( c, 10 );
m.action = { |chord|
	[chord.direction, chord.charMod, chord.char, chord.chord].postcs;
	g.typeCharacter( chord.char, chord.charMod, chord.direction, chord.chord );
};
);

(
);

