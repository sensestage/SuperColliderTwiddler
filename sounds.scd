// s.boot;

(
s.doWhenBooted({
	Ndef( \right, { SinOsc.ar( \freq.kr(400)*[1,5/3,3/2], 0, 0.025/3 * EnvGen.kr( Env.perc, \trig.tr(1), timeScale: \dur.kr(1)*[1,2,3] ) ).sum }).play;
	Ndef( \wrong, { SinOsc.ar( \freq.kr(400)*[1,5/3-0.1,3/2-0.05] * LFPulse.ar( \fmf.kr(10),0,0.5,\fma.kr(0.1), 1 ), 0, 0.025/3 * EnvGen.kr( Env.perc, \trig.tr(1), timeScale: \dur.kr(1)*[3,2,1] ) ).sum }).play;

	Ndef( \back, { SinOsc.ar( \freq.kr(400)*[1,5/3,3/2] * LFTri.ar( \fmf.kr(10),0,\fma.kr(0.1), 1 ), 0, 0.025/3 * EnvGen.kr( Env.perc, \trig.tr(1), timeScale: \dur.kr(1)*[2,3,1] ) ).sum }).play;
});
);

/*
Ndef( \back ).set( \trig, 1, \freq, rrand(3,10)*100, \dur, 0.1, \fmf, 40, \fma, 0.1 );
Ndef( \right ).set( \trig, 1, \freq,600, \dur, 0.75 );
Ndef( \wrong ).set( \trig, 1, \freq,300, \dur, 0.75, \fmf, 60, \fma, 0.25 );
*/
