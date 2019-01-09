// s.boot;
/*
(
s.doWhenBooted({
	Ndef( \right, { SinOsc.ar( \freq.kr(800)*[1,5/3,3/2], 0, 0.025/3 * EnvGen.kr( Env.perc, \trig.tr(1), timeScale: \dur.kr(1)*[1,2,3] ) ).sum }).play;
	Ndef( \wrong, { SinOsc.ar( \freq.kr(200)*[1,5/3-0.1,3/2-0.05] * LFPulse.ar( \fmf.kr(10),0,0.5,\fma.kr(0.1), 1 ), 0, 0.025/3 * EnvGen.kr( Env.perc, \trig.tr(1), timeScale: \dur.kr(1)*[3,2,1] ) ).sum }).play;

	Ndef( \back, { SinOsc.ar( \freq.kr(300)*[1,5/3,3/2] * LFTri.ar( \fmf.kr(10),0,\fma.kr(0.1), 1 ), 0, 0.025/3 * EnvGen.kr( Env.perc, \trig.tr(1), timeScale: \dur.kr(1)*[2,3,1] ) ).sum }).play;
});
);
*/
(
SynthDef( \right, {
	Out.ar( \out.kr(0), Pan2.ar(
		SinOsc.ar( \freq.kr(800)*[1,5/3,3/2], 0, 0.025/3 * EnvGen.kr( Env.perc, \trig.tr(1), timeScale: \dur.kr(1)*[1,2,3], doneAction: [2,0,0] ) ).sum
	) );
}).add;

SynthDef( \wrong, {
	Out.ar( \out.kr(0), Pan2.ar(
		SinOsc.ar( \freq.kr(200)*[1,5/3-0.1,3/2-0.05] * LFPulse.ar( \fmf.kr(10),0,0.5,\fma.kr(0.1), 1 ), 0, 0.025/3 * EnvGen.kr( Env.perc, \trig.tr(1), timeScale: \dur.kr(1)*[3,2,1], doneAction: [2,0,0] ) ).sum
	));
}).add;

SynthDef( \back, {
	Out.ar( \out.kr(0), Pan2.ar(
		SinOsc.ar( \freq.kr(300)*[1,5/3,3/2] * LFTri.ar( \fmf.kr(10),0,\fma.kr(0.1), 1 ), 0, 0.025/3 * EnvGen.kr( Env.perc, \trig.tr(1), timeScale: \dur.kr(1)*[2,3,1], doneAction: [0,2,0] ) ).sum
	))
}).add;

SynthDef( \navigate, {
	Out.ar( \out.kr(0), Splay.ar(
		SinOsc.ar( \freq.kr(300) * ( Array.rand(10, 0.96, 1.06 ) ), 0, EnvGen.kr(Env.perc(0.01,0.1), doneAction: 2 )*\amp.kr(0.1) )
	));
}).add;

);

/*
Ndef( \back ).set( \trig, 1, \freq, rrand(3,10)*100, \dur, 0.1, \fmf, 40, \fma, 0.1 );
Ndef( \right ).set( \trig, 1, \freq,600, \dur, 0.75 );
Ndef( \wrong ).set( \trig, 1, \freq,300, \dur, 0.75, \fmf, 60, \fma, 0.25 );
*/
