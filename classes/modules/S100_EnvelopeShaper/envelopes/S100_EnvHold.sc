S100_EnvHold {

	var <synth;
	var server;
	var <group;
	classvar settings;


	var <running; // true o false: Si el sintetizador está activo o pausado
	var lag;


	// Métodos de clase //////////////////////////////////////////////////////////////////

	*new { |server|
		settings = S100_Settings.get;
		^super.new.init(server);
	}

	*addSynthDef {
		SynthDef(\S100_envHold, {
			arg generalGate,
			signalTrigger,
			inFeedbackSignalTrigger,
			inputBus,
			inFeedbackBus,
			outputBus,
			outputBusVol,
			inDelayVol,
			inFeedbackDelayVol,
			inAttackVol,
			inFeedbackAttackVol,
			inDecayVol,
			inFeedbackDecayVol,
			inSustainVol,
			inFeedbackSustainVol,
			inReleaseVol,
			inFeedbackReleaseVol,
			delayTime,
			attackTime,
			decayTime,
			sustainLevel,
			releaseTime,
			envelopeLevel,
			signalLevel;

			var sig, vol, env, gate;
			gate = In.ar(signalTrigger);
			gate = gate + InFeedback.ar(inFeedbackSignalTrigger);
			sig = In.ar(inputBus);
			sig = sig + InFeedback.ar(inFeedbackBus);


			env = Env(
				levels: [
					0,
					0,
					1,
					sustainLevel,
					0,
				],
				times: [delayTime, attackTime, decayTime, releaseTime],
				releaseNode: 2,
			).ar(0, gate: gate * generalGate);

			// Se aplica la envolvente a la señal
			sig = sig * env * signalLevel; // gate tiene lag, para que cuando se envíe valor 0, no se corte bruscamente.


			// Se aplica la envolvente al voltage
			vol = env * envelopeLevel.lag(0.2); // gate tiene lag, para que cuando se envíe valor 0, no se corte bruscamente.

			Out.ar(outputBus, sig);
			Out.ar(outputBusVol, vol);

		},
		).add;
	}


	// Métodos de instancia //////////////////////////////////////////////////////////////

	init { arg serv = Server.local;
		server = serv;
	}

	createSynth {
		arg
		group,
		signalTrigger,
		inFeedbackSignalTrigger,
		inputBus,
		inFeedbackBus,
		outputBus,
		outputBusVol,
		inDelayVol,
		inFeedbackDelayVol,
		inAttackVol,
		inFeedbackAttackVol,
		inDecayVol,
		inFeedbackDecayVol,
		inSustainVol,
		inFeedbackSustainVol,
		inReleaseVol,
		inFeedbackReleaseVol,
		delayTime,
		attackTime,
		decayTime,
		sustainLevel,
		releaseTime,
		envelopeLevel,
		signalLevel;
		if(synth.isPlaying==false, {
			synth = Synth(\S100_envHold, [
				\generalGate, 1,
				\signalTrigger, signalTrigger,
				\inFeedbackSignalTrigger, inFeedbackSignalTrigger,
				\inputBus, inputBus,
				\inFeedbackBus, inFeedbackBus,
				\outputBus, outputBus,
				\outputBusVol: outputBusVol,
				\inDelayVol: inDelayVol,
				\inFeedbackDelayVol: inFeedbackDelayVol,
				\inAttackVol: inAttackVol,
				\inFeedbackAttackVol: inFeedbackAttackVol,
				\inDecayVol: inDecayVol,
				\inFeedbackDecayVol: inFeedbackDecayVol,
				\inSustainVol: inSustainVol,
				\inFeedbackSustainVol: inFeedbackSustainVol,
				\inReleaseVol: inReleaseVol,
				\inFeedbackReleaseVol: inFeedbackReleaseVol,
				\delayTime, delayTime,
				\attackTime, attackTime,
				\decayTime, decayTime,
				\sustainLevel, sustainLevel,
				\releaseTime, releaseTime,
				\envelopeLevel, envelopeLevel,
				\signalLevel, signalLevel,
			], group).register;
		});
		^synth;
		//	this.synthRun;
	}

	// Pausa o reanuda el Synth
	synthRun {|state|
		if(state==true, {synth.set(\generalGate, 1)}, {synth.set(\generalGate, 0)});
		synth.run(state);
		running = state;
	}

}