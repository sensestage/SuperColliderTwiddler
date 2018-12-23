s.plotTree;


// color of current line can be a mix of how well it is going. (red/green)
// + color management task in background (now there are delays in the code)
// tutor should skip empty lines (?) and just post commented lines

// + fix number of lines that are displayed ---> or which ones are displayed.
// + insert white space + | voor sublines van evaluated lines
// + at evaluate previous: set selected in listview

// + highlight buttons of character just typed
//      in color of right or wrong

// + catch arrow keys and up/down; change the shortcut for updating the next/prev line to type
// + catch arrow keys in edit (line) mode, so that we keep track of the cursor
// + fix cursorPosition when editing new line from typed window.

// + line from file index is sometimes fucked up. Make a "go to end option"
// + perhaps use ALT-up, ALT-down, ALT-end for navigation

// + when entering line in edittypedline mode, then also advance line to next
// + when executing code, reset the selected, so that the scroll is at the top of evaluated window

// sonification of navigation

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

// home: 65360, 16777232
// end:  65367, 16777233
// pageup: 65365, 16777238
// pagedown: 65366, 16777239
// left: 65361, 16777234
// right: ,65363, 16777236
// down: 65364, 16777237
// up: 65362, 16777235

// esc: 65307, 16777216
// backspace: 65288, 16777219
// delete: 127, 65535, 16777223



//// Jack Armitage - stenophone to livecode
/// group: embodiment and livecoding
