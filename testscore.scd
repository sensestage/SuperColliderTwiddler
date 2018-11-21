s.boot;

(
Ndef( \drone, {
	SinOsc.ar( \freq.kr(500, 4), 0, \amp.kr(0.1))
}).play;
);

Ndef( \drone ).set( \freq, 300 );
Ndef( \drone ).set( \freq, 600 );
Ndef( \drone ).set( \freq, 400 );

(
Ndef( \drone, {
	SinOsc.ar( \freq.kr(500, 4) *
		SinOsc.ar( \fmf.kr(10), 0, \fma.kr(0.5), 1 ),
		0, \amp.kr(0.1)
	)
});
)

Ndef( \drone ).set( \fmf, 20 );
Ndef( \drone ).set( \freq, 400 );
Ndef( \drone ).set( \fmf, 40 );
Ndef( \drone ).set( \freq, 800 );
Ndef( \drone ).set( \fmf, 80 );
Ndef( \drone ).set( \freq, 400 );
