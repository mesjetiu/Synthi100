Synthi100 {
	var <>server; // Servidor por defecto

	// Módulos que incluye:
	var prOscillators;
	var <conectionOut;

	// Buses internos de entrada y salida:
	var <audioInBuses;
	var <audioOutBuses;

	// Buses externos de entrada y salida: (se unen uno a uno a los buses internos)
	var <externAudioInBuses;
	var <externAudioOutBuses;

	// Diccionario con los símbolos de cada módulo:
	var prParameterDictionary;

	// Opciones:
	var <numAudioInBuses = 8;
	var <numAudioOutBuses = 8;



	// Métodos de clase //////////////////////////////////////////////////////////////////

	*initClass {
		// Inicializa otras clases antes de esta
		Class.initClassTree(S100_Oscillator);
	}

	*new { |server, audioInBuses, audioOutBuses|
		^super.new.init(server, audioInBuses, audioOutBuses);
	}



	// Métodos de instancia //////////////////////////////////////////////////////////////

	init { arg serv = Server.local, aInBuses, aOutBuses;
		server = serv;
		// Buses de audio de entrada y salida
		audioInBuses = numAudioInBuses.collect({Bus.audio(server, 1)});
		audioOutBuses = numAudioOutBuses.collect({Bus.audio(server, 1)});
		externAudioInBuses = numAudioInBuses.collect({nil});
		externAudioOutBuses = numAudioOutBuses.collect({nil});
		this.setExternAudioInBuses (aInBuses);
		this.setExternAudioOutBuses (aOutBuses);

		// Módulos
		prOscillators = 12.collect({S100_Oscillator.new(serv)});


		// Diccionario de parámetros de la interfaz física del Synthi 100
		prParameterDictionary = Dictionary.newFrom(List[
			"/osc/1/range", {|range| prOscillators[0].setRange(range)},
			"/osc/1/pulse/level", {|level| prOscillators[0].setPulseLevel(level)},
			"/osc/1/pulse/shape", {|shape| prOscillators[0].setPulseShape(shape)},
			"/osc/1/sine/level", {|level| prOscillators[0].setSineLevel(level)},
			"/osc/1/sine/symmetry", {|symmetry| prOscillators[0].setSineSymmetry(symmetry)},
			"/osc/1/trianglelevel", {|level| prOscillators[0].setTriangleLevel(level)},
			"/osc/1/sawtooth/level", {|level| prOscillators[0].setSawtoothLevel(level)},
			"/osc/1/frequency", {|freq| prOscillators[0].setFrequency(freq)},

			"/osc/2/range", {|range| prOscillators[1].setRange(range)},
			"/osc/2/pulse/level", {|level| prOscillators[1].setPulseLevel(level)},
			"/osc/2/pulse/shape", {|shape| prOscillators[1].setPulseShape(shape)},
			"/osc/2/sine/level", {|level| prOscillators[1].setSineLevel(level)},
			"/osc/2/sine/symmetry", {|symmetry| prOscillators[1].setSineSymmetry(symmetry)},
			"/osc/2/triangle/level", {|level| prOscillators[1].setTriangleLevel(level)},
			"/osc/2/sawtooth/level", {|level| prOscillators[1].setSawtoothLevel(level)},
			"/osc/2/frequency", {|freq| prOscillators[1].setFrequency(freq)},

			"/osc/3/range", {|range| prOscillators[2].setRange(range)},
			"/osc/3/pulse/level", {|level| prOscillators[2].setPulseLevel(level)},
			"/osc/3/pulse/shape", {|shape| prOscillators[2].setPulseShape(shape)},
			"/osc/3/sine/level", {|level| prOscillators[2].setSineLevel(level)},
			"/osc/3/sine/symmetry", {|symmetry| prOscillators[2].setSineSymmetry(symmetry)},
			"/osc/3/triangle/level", {|level| prOscillators[2].setTriangleLevel(level)},
			"/osc/3/sawtooth/level", {|level| prOscillators[2].setSawtoothLevel(level)},
			"/osc/3/frequency", {|freq| prOscillators[2].setFrequency(freq)},

			"/osc/4/range", {|range| prOscillators[3].setRange(range)},
			"/osc/4/pulse/level", {|level| prOscillators[3].setPulseLevel(level)},
			"/osc/4/pulse/shape", {|shape| prOscillators[3].setPulseShape(shape)},
			"/osc/4/sine/level", {|level| prOscillators[3].setSineLevel(level)},
			"/osc/4/sine/symmetry", {|symmetry| prOscillators[3].setSineSymmetry(symmetry)},
			"/osc/4/triangle/level", {|level| prOscillators[3].setTriangleLevel(level)},
			"/osc/4/sawtooth/level", {|level| prOscillators[3].setSawtoothLevel(level)},
			"/osc/4/frequency", {|freq| prOscillators[3].setFrequency(freq)},

			"/osc/5/range", {|range| prOscillators[4].setRange(range)},
			"/osc/5/pulse/level", {|level| prOscillators[4].setPulseLevel(level)},
			"/osc/5/pulse/shape", {|shape| prOscillators[4].setPulseShape(shape)},
			"/osc/5/sine/level", {|level| prOscillators[4].setSineLevel(level)},
			"/osc/5/sine/symmetry", {|symmetry| prOscillators[4].setSineSymmetry(symmetry)},
			"/osc/5/triangle/level", {|level| prOscillators[4].setTriangleLevel(level)},
			"/osc/5/sawtooth/level", {|level| prOscillators[4].setSawtoothLevel(level)},
			"/osc/5/frequency", {|freq| prOscillators[4].setFrequency(freq)},

			"/osc/6/range", {|range| prOscillators[5].setRange(range)},
			"/osc/6/pulse/level", {|level| prOscillators[5].setPulseLevel(level)},
			"/osc/6/pulse/shape", {|shape| prOscillators[5].setPulseShape(shape)},
			"/osc/6/sine/level", {|level| prOscillators[5].setSineLevel(level)},
			"/osc/6/sine/symmetry", {|symmetry| prOscillators[5].setSineSymmetry(symmetry)},
			"/osc/6/triangle/level", {|level| prOscillators[5].setTriangleLevel(level)},
			"/osc/6/sawtooth/level", {|level| prOscillators[5].setSawtoothLevel(level)},
			"/osc/6/frequency", {|freq| prOscillators[5].setFrequency(freq)},

			"/osc/7/range", {|range| prOscillators[6].setRange(range)},
			"/osc/7/pulse/level", {|level| prOscillators[6].setPulseLevel(level)},
			"/osc/7/pulse/shape", {|shape| prOscillators[6].setPulseShape(shape)},
			"/osc/7/sine/level", {|level| prOscillators[6].setSineLevel(level)},
			"/osc/7/sine/symmetry", {|symmetry| prOscillators[6].setSineSymmetry(symmetry)},
			"/osc/7/triangle/level", {|level| prOscillators[6].setTriangleLevel(level)},
			"/osc/7/sawtooth/level", {|level| prOscillators[6].setSawtoothLevel(level)},
			"/osc/7/frequency", {|freq| prOscillators[6].setFrequency(freq)},

			"/osc/8/range", {|range| prOscillators[7].setRange(range)},
			"/osc/8/pulse/level", {|level| prOscillators[7].setPulseLevel(level)},
			"/osc/8/pulse/shape", {|shape| prOscillators[7].setPulseShape(shape)},
			"/osc/8/sine/level", {|level| prOscillators[7].setSineLevel(level)},
			"/osc/8/sine/symmetry", {|symmetry| prOscillators[7].setSineSymmetry(symmetry)},
			"/osc/8/triangle/level", {|level| prOscillators[7].setTriangleLevel(level)},
			"/osc/8/sawtooth/level", {|level| prOscillators[7].setSawtoothLevel(level)},
			"/osc/8/frequency", {|freq| prOscillators[7].setFrequency(freq)},

			"/osc/9/range", {|range| prOscillators[8].setRange(range)},
			"/osc/9/pulse/level", {|level| prOscillators[8].setPulseLevel(level)},
			"/osc/9/pulse/shape", {|shape| prOscillators[8].setPulseShape(shape)},
			"/osc/9/sine/level", {|level| prOscillators[8].setSineLevel(level)},
			"/osc/9/sine/symmetry", {|symmetry| prOscillators[8].setSineSymmetry(symmetry)},
			"/osc/9/triangle/level", {|level| prOscillators[8].setTriangleLevel(level)},
			"/osc/9/sawtooth/level", {|level| prOscillators[8].setSawtoothLevel(level)},
			"/osc/9/frequency", {|freq| prOscillators[8].setFrequency(freq)},

			"/osc/10/range", {|range| prOscillators[9].setRange(range)},
			"/osc/10/pulse/level", {|level| prOscillators[9].setPulseLevel(level)},
			"/osc/10/pulse/shape", {|shape| prOscillators[9].setPulseShape(shape)},
			"/osc/10/sine/level", {|level| prOscillators[9].setSineLevel(level)},
			"/osc/10/sine/symmetry", {|symmetry| prOscillators[9].setSineSymmetry(symmetry)},
			"/osc/10/triangle/level", {|level| prOscillators[9].setTriangleLevel(level)},
			"/osc/10/sawtooth/level", {|level| prOscillators[9].setSawtoothLevel(level)},
			"/osc/10/frequency", {|freq| prOscillators[9].setFrequency(freq)},

			"/osc/11/range", {|range| prOscillators[10].setRange(range)},
			"/osc/11/pulse/level", {|level| prOscillators[10].setPulseLevel(level)},
			"/osc/11/pulse/shape", {|shape| prOscillators[10].setPulseShape(shape)},
			"/osc/11/sine/level", {|level| prOscillators[10].setSineLevel(level)},
			"/osc/11/sine/symmetry", {|symmetry| prOscillators[10].setSineSymmetry(symmetry)},
			"/osc/11/triangle/level", {|level| prOscillators[10].setTriangleLevel(level)},
			"/osc/11/sawtooth/level", {|level| prOscillators[10].setSawtoothLevel(level)},
			"/osc/11/frequency", {|freq| prOscillators[10].setFrequency(freq)},

			"/osc/12/range", {|range| prOscillators[11].setRange(range)},
			"/osc/12/pulse/level", {|level| prOscillators[11].setPulseLevel(level)},
			"/osc/12/pulse/shape", {|shape| prOscillators[11].setPulseShape(shape)},
			"/osc/12/sine/level", {|level| prOscillators[11].setSineLevel(level)},
			"/osc/12/sine/symmetry", {|symmetry| prOscillators[11].setSineSymmetry(symmetry)},
			"/osc/12/triangle/level", {|level| prOscillators[11].setTriangleLevel(level)},
			"/osc/12/sawtooth/level", {|level| prOscillators[11].setSawtoothLevel(level)},
			"/osc/12/frequency", {|freq| prOscillators[11].setFrequency(freq)},
		]);
	}


	// Métodos de instancia /////////////////////////////////////////////////////////////////////////
	setExternAudioInBuses {arg audioBuses;
		if(audioBuses.class == Array, {
			numAudioInBuses.do({|i|
				externAudioInBuses[i] = audioBuses[i];
			})
		})
	}

	setExternAudioOutBuses {arg audioBuses;
		if(audioBuses.class== Array, {
			numAudioOutBuses.do({|i|
				externAudioOutBuses[i] = audioBuses[i];
			})
		})
	}


	play {
		// Se conectan provisionalmente las salidas de todos los módulos a los dos primeros buses de salida especificados en externAudioBuses
		conectionOut = prOscillators.collect({|i|
			SynthDef(\conection, {
				//var out1 = externAudioOutBuses[0];
				//var out2 = externAudioOutBuses[1];
				var sig = In.ar(i.outBus);
				Out.ar(0, sig);
				Out.ar(1, sig);
			}).play(server);
		});

		// Se arrancan todos los Synths de todos los módulos (el servidor debe están arrancado)
		prOscillators.do({|i| i.createSynth});
	}

	stop {
		conectionOut.do({|i| i.freeSynth});
		prOscillators.do({|i| i.freeSynth});
	}


	// Setter de los diferentes parámetros de los módulos en formato OSC

	setParameterOSC {|simbol, value|
		prParameterDictionary[simbol].value(value);
	}
}
